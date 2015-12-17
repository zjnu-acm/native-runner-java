package com.github.zhanhb.judge.jdk.lang;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProcessImplTest {

    /**
     * Test of createCommandLine method, of class ProcessImpl.
     */
    @Test
    public void testCreateCommandLine() {
        System.out.println("createCommandLine");
        String[] cmd = {"notepad", "aa aa"};
        String expResult = "notepad \"aa aa\"";
        String result = ProcessImpl.createCommandLine(cmd);
        assertEquals(expResult, result);
    }

}
