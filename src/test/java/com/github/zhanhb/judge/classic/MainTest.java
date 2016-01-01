/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.zhanhb.judge.classic;

import org.junit.Test;

/**
 *
 * @author zhanhb
 */
public class MainTest {

    private static boolean isRuntimeError(int x) {
        return x != 0 && (-512 <= x && x < 512);
    }

    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() throws Exception {
        for (int i : new int[]{
            0, 103, Integer.MAX_VALUE, Integer.MIN_VALUE,
            1, 0, -1, 9999999}) {
            System.out.printf("%d %s%n", i, isRuntimeError(i));
        }
    }

}
