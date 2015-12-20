package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.WinNT;
import java.util.Arrays;

/**
 *
 * @author zhanhb
 */
public class Advapi32Util {

    public static WinNT.PSID newPSID(Advapi32.SID_IDENTIFIER_AUTHORITY pIdentifierAuthority,
            int... dwSubAuthority) {
        int nSubAuthorityCount = dwSubAuthority.length;
        if (nSubAuthorityCount > 8 || nSubAuthorityCount == 0) {
            throw new IllegalArgumentException();
        }
        int[] copyOf = Arrays.copyOf(dwSubAuthority, 8);
        WinNT.PSIDByReference psidByReference = new WinNT.PSIDByReference();
        Kernel32Util.assertTrue(Advapi32.INSTANCE.AllocateAndInitializeSid(pIdentifierAuthority,
                (byte) nSubAuthorityCount,
                copyOf[0], copyOf[1], copyOf[2], copyOf[3],
                copyOf[4], copyOf[5], copyOf[6], copyOf[7],
                psidByReference));
        return psidByReference.getValue();
    }

}
