package com.navinfo.mapspotter.process.convert.ora2mongo;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 7/20 0020.
 */
public class ExtractOracle2MongoTest {

    @Test
    public void testInitPoiKindCode() throws Exception {
        ExtractOracle2Mongo ex = new ExtractOracle2Mongo(null, 0, null, null, null,
                null, 0, null);
    }
}