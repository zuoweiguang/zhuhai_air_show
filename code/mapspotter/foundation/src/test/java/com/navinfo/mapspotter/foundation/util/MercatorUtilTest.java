package com.navinfo.mapspotter.foundation.util;

import com.vividsolutions.jts.geom.Coordinate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by cuiliang on 2015/12/30.
 */
public class MercatorUtilTest {


    MercatorUtil util = new MercatorUtil(256, 12);
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLonLat2MCode() throws Exception {
        System.out.println(util.lonLat2MCode(new Coordinate(116.36115, 39.931176)));
    }

    @Test
    public void testLonLat2MCodeList() throws Exception {
        System.out.println(util.lonLat2MCodeList(new Coordinate(119, 39), 1));
    }

    @Test
    public void testMeterBound() throws Exception {

    }

    @Test
    public void testLonLat2Meter() throws Exception {
        Coordinate mercator = MercatorUtil.lonLat2Meters(new Coordinate(119, 39));
        System.out.println(mercator.x + "," + mercator.y);
    }

    @Test
    public void testMeter2LonLat() throws Exception {

        Coordinate mercator = MercatorUtil.lonLat2Meters(new Coordinate(119, 39));
        Coordinate lonLat = MercatorUtil.meters2LonLat(mercator);
        System.out.println(lonLat.x + "," + lonLat.y);
    }

    @Test
    public void testMeter2Tile() throws Exception {

    }
}