package com.github.zhanhb.judge.jna;

import com.sun.jna.platform.win32.WinNT;
import java.util.Arrays;

/**
 *
 * @author zhanhb
 */
public class Advapi32Util {

    public static WinNT.PSID newPSID(Advapi32.SID_IDENTIFIER_AUTHORITY pIdentifierAuthority,
            int... dwSubAuthorities) {
        int nSubAuthorityCount = dwSubAuthorities.length;
        if (nSubAuthorityCount > 8 || nSubAuthorityCount == 0) {
            throw new IllegalArgumentException();
        }
        int[] copy = Arrays.copyOf(dwSubAuthorities, 8);
        WinNT.PSIDByReference psidByReference = new WinNT.PSIDByReference();
        Kernel32Util.assertTrue(Advapi32.INSTANCE.AllocateAndInitializeSid(pIdentifierAuthority,
                (byte) nSubAuthorityCount,
                copy[0], copy[1], copy[2], copy[3],
                copy[4], copy[5], copy[6], copy[7],
                psidByReference));
        return psidByReference.getValue();
    }

}
