package com.navinfo.mapspotter.foundation.model;

import org.junit.Test;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 2016/4/11.
 */
public class CarTrackPointTest {

    @Test
    public void testCompareTo() throws Exception {
        ConcurrentSkipListSet sortTrack = new ConcurrentSkipListSet<>();

        CarTrackPoint pt1 = new CarTrackPoint(1231414, 119, 40, 12, 32, null);
        sortTrack.add(pt1);

        CarTrackPoint pt2 = new CarTrackPoint(1231416, 119, 50, 12, 32, null);
        sortTrack.add(pt2);

        CarTrackPoint pt3 = new CarTrackPoint(1231415, 119, 60, 12, 32, null);
        sortTrack.add(pt3);

        assertEquals(sortTrack.size(), 3);
    }
}