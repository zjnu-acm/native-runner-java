package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public class Judgement {

    private final WinNT.HANDLE hProcess;
    private boolean halted;
    private int haltCode;

    public Judgement(WinNT.HANDLE hProcess) {
        this.hProcess = hProcess;
    }

    public synchronized void terminate(int errorCode) {
        if (!halted) {
            halted = true;
            haltCode = errorCode;
            if (hProcess != null && !WinBase.INVALID_HANDLE_VALUE.equals(hProcess)) {
                Kernel32.INSTANCE.TerminateProcess(hProcess, 1);
            }
        }
    }

    public long getMemory() {
        return PsapiUtil.GetProcessMemoryInfo(hProcess).PeakWorkingSetSize.longValue();
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
        if (!Kernel32.INSTANCE.GetProcessTimes(hProcess, ftCreateTime, temp, temp, temp)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return ftCreateTime.toLong();
    }

    public long getTime() {
        WinBase.FILETIME ftCreateTime = new WinBase.FILETIME();
        WinBase.FILETIME ftExitTime = new WinBase.FILETIME();
        WinBase.FILETIME temp = new WinBase.FILETIME();
        if (!Kernel32.INSTANCE.GetProcessTimes(hProcess, ftCreateTime, ftExitTime, temp, temp)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return ftExitTime.toLong() - ftCreateTime.toLong();
    }

    public int getHaltCode() {
        return haltCode;
    }

    public int getExitCode() {
        IntByReference dwExitCode = new IntByReference();
        if (!Kernel32.INSTANCE.GetExitCodeProcess(hProcess, dwExitCode)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return dwExitCode.getValue();
    }

}
