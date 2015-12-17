package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.PsapiEx.PROCESS_MEMORY_COUNTERS;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;

public class PsapiUtil {

    public static PROCESS_MEMORY_COUNTERS GetProcessMemoryInfo(WinNT.HANDLE hProcess) {
        PROCESS_MEMORY_COUNTERS ppsmemCounters = new PROCESS_MEMORY_COUNTERS();
        boolean success = PsapiEx.INSTANCE.GetProcessMemoryInfo(hProcess, ppsmemCounters, ppsmemCounters.cb);
        if (!success) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return ppsmemCounters;
    }

}
