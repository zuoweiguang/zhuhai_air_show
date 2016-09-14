package com.navinfo.mapspotter.foundation.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cuiliang on 2015/12/31.
 */
public class CoordinateUtilTest {

    @Test
    public void testBd_encrypt() throws Exception {
        CoordinateUtil.bd_encrypt(119,39);
    }

    @Test
    public void testBd_decrypt() throws Exception {
        double[] a = CoordinateUtil.bd_decrypt(119,39);
        System.out.println(a[0]);
        System.out.println(a[1]);
    }
}