package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import java.util.Arrays;

/**
 *
 * @author zhanhb
 */
public class Advapi32Util {

    public static WinNT.PSID newPSID(Advapi32.SID_IDENTIFIER_AUTHORITY pIdentifierAuthority,
            int... dwSubAuthority) {
        if (dwSubAuthority.length > 8 || dwSubAuthority.length <= 0) {
            throw new IllegalArgumentException();
        }
        byte nSubAuthorityCount = (byte) dwSubAuthority.length;
        int[] copyOf = Arrays.copyOf(dwSubAuthority, 8);
        WinNT.PSIDByReference psidByReference = new WinNT.PSIDByReference();
        if (!Advapi32.INSTANCE.AllocateAndInitializeSid(
                pIdentifierAuthority,
                nSubAuthorityCount,
                copyOf[0], copyOf[1], copyOf[2], copyOf[3],
                copyOf[4], copyOf[5], copyOf[6], copyOf[7],
                psidByReference)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        return psidByReference.getValue();
    }

}
