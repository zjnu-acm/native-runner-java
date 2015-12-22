package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.Psapi.PROCESS_MEMORY_COUNTERS;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class JudgeProcess {

    private final WinNT.HANDLE hProcess;
    private final AtomicBoolean halted = new AtomicBoolean();
    private int haltCode;

    public JudgeProcess(WinNT.HANDLE hProcess) {
        this.hProcess = hProcess;
    }

    public void terminate(int errorCode) {
        if (halted.compareAndSet(false, true)) {
            haltCode = errorCode;
            if (hProcess != null && !WinBase.INVALID_HANDLE_VALUE.equals(hProcess)) {
                // don't check the return value, maybe the process has already exited.
                Kernel32.INSTANCE.TerminateProcess(hProcess, 1);
            }
        }
    }

    public long getMemory() {
        PROCESS_MEMORY_COUNTERS ppsmemCounters = new PROCESS_MEMORY_COUNTERS();
        Kernel32Util.assertTrue(Psapi.INSTANCE.GetProcessMemoryInfo(hProcess, ppsmemCounters, ppsmemCounters.cb));
        return ppsmemCounters.PeakWorkingSetSize.longValue();
    }

    private boolean join0(int millis) {
        return Kernel32.INSTANCE.WaitForSingleObject(hProcess, millis) == 0;
    }

    public boolean join(long millis) {
        return join0(millis > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) millis);
    }

    public long getStartTime() {
        WinBase.FILETIME ftCreateTime = new WinBase.FILETIME();
        WinBase.FILETIME temp = new WinBase.FILETIME();
        Kernel32Util.assertTrue(Kernel32.INSTANCE.GetProcessTimes(hProcess, ftCreateTime, temp, temp, temp));
        return ftCreateTime.toLong();
    }

    public long getTime() {
        return getTime(TimeUnit.MILLISECONDS);
    }

    public long getTime(TimeUnit timeUnit) {
        WinBase.FILETIME ftCreateTime = new WinBase.FILETIME();
        WinBase.FILETIME ftExitTime = new WinBase.FILETIME();
        WinBase.FILETIME temp = new WinBase.FILETIME();
        Kernel32Util.assertTrue(Kernel32.INSTANCE.GetProcessTimes(hProcess, ftCreateTime, ftExitTime, temp, temp));
        long exscaped = (ftExitTime.dwHighDateTime - ftCreateTime.dwHighDateTime + 0L) << 32
                | (ftExitTime.dwLowDateTime - ftCreateTime.dwLowDateTime & 0xffffffffL);
        return timeUnit == TimeUnit.NANOSECONDS
                ? ((exscaped << 6) + (exscaped << 5) + (exscaped << 2)) // multiply by 100
                : timeUnit.convert(exscaped / 10, TimeUnit.MICROSECONDS);
    }

    public int getHaltCode() {
        return haltCode;
    }

    public int getExitCode() {
        IntByReference dwExitCode = new IntByReference();
        Kernel32Util.assertTrue(Kernel32.INSTANCE.GetExitCodeProcess(hProcess, dwExitCode));
        return dwExitCode.getValue();
    }

}
