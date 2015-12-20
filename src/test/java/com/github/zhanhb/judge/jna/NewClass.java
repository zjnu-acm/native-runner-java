package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.Advapi32.SID_IDENTIFIER_AUTHORITY;
import org.junit.Test;

public class NewClass {

    @Test
    public void test() {
        SID_IDENTIFIER_AUTHORITY sid_identifier_authority = new SID_IDENTIFIER_AUTHORITY();
        System.out.println(sid_identifier_authority.size());
    }

}
