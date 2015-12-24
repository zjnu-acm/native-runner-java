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

    public static WinNT.HANDLE createRestrictedToken(
            WinNT.HANDLE existingTokenHandle,
            int /*DWORD*/ flags,
            Advapi32.SID_AND_ATTRIBUTES[] SidsToDisable,
            WinNT.LUID_AND_ATTRIBUTES[] PrivilegesToDelete,
            Advapi32.SID_AND_ATTRIBUTES[] SidsToRestrict
    ) {
        WinNT.HANDLEByReference NewTokenHandle = new WinNT.HANDLEByReference();
        Kernel32Util.assertTrue(Advapi32.INSTANCE.CreateRestrictedToken(
                existingTokenHandle, // ExistingTokenHandle
                flags, // Flags
                SidsToDisable != null ? SidsToDisable.length : 0, // DisableSidCount
                SidsToDisable, // SidsToDisable
                PrivilegesToDelete != null ? PrivilegesToDelete.length : 0, // DeletePrivilegeCount
                PrivilegesToDelete, // PrivilegesToDelete
                SidsToRestrict != null ? SidsToRestrict.length : 0, // RestrictedSidCount
                SidsToRestrict, // SidsToRestrict
                NewTokenHandle // NewTokenHandle
        ));
        return NewTokenHandle.getValue();
    }

    public static WinNT.HANDLE openProcessToken(WinNT.HANDLE processHandle, int desiredAccess) {
        WinNT.HANDLEByReference tokenHandle = new WinNT.HANDLEByReference();
        Kernel32Util.assertTrue(Advapi32.INSTANCE.OpenProcessToken(
                processHandle,
                desiredAccess,
                tokenHandle));
        return tokenHandle.getValue();
    }

}
