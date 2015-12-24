package com.github.zhanhb.judge.jna;

import com.github.zhanhb.judge.jna.Advapi32.SID_IDENTIFIER_AUTHORITY;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author zhanhb
 */
public class Advapi32Test {

    @Test
    public void testSID_IDENTIFIER_AUTHORITY() {
        assertEquals(6, new SID_IDENTIFIER_AUTHORITY().size());
    }

}
