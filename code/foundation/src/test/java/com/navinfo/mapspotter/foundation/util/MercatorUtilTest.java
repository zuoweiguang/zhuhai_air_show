package com.navinfo.mapspotter.foundation.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cuiliang on 2015/12/30.
 */
public class MercatorUtilTest {


    MercatorUtil util = new MercatorUtil(14);
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLonLat2MCode() throws Exception {
        System.out.println(util.lonLat2MCode(119, 39));
    }

    @Test
    public void testLonLat2MCodeList() throws Exception {
        System.out.println(util.lonLat2MCodeList(119, 39, 1));
    }

    @Test
    public void testMeterBound() throws Exception {

    }

    @Test
    public void testLonLat2Meter() throws Exception {
        double[] mercator = MercatorUtil.lonLat2Meter(119, 39);
        System.out.println(mercator[0] + "," + mercator[1]);
    }

    @Test
    public void testMeter2LonLat() throws Exception {
        double[] mercator = MercatorUtil.lonLat2Meter(119, 39);
        double[] lonLat = MercatorUtil.meter2LonLat(mercator[0], mercator[1]);
        System.out.println(lonLat[0] + "," + lonLat[1]);
    }

    @Test
    public void testMeter2Tile() throws Exception {

    }
}