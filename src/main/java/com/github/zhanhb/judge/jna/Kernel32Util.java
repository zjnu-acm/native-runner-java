package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.Win32Exception;

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

}
