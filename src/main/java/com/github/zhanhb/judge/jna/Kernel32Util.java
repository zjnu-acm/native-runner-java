package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

/**
 *
 * @author zhanhb
 */
public class Kernel32Util {

    public static void assertTrue(boolean test) {
        if (!test) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
    }

    public WinNT.HANDLE[] CreatePipe(WinBase.SECURITY_ATTRIBUTES lpPipeAttributes, int nSize) {
        WinNT.HANDLEByReference hReadPipe = new WinNT.HANDLEByReference();
        WinNT.HANDLEByReference hWritePipe = new WinNT.HANDLEByReference();
        assertTrue(Kernel32.INSTANCE.CreatePipe(hReadPipe, hWritePipe, lpPipeAttributes, nSize));
        return new WinNT.HANDLE[]{hReadPipe.getValue(), hWritePipe.getValue()};
    }

}
