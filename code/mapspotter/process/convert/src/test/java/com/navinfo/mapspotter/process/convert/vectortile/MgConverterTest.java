package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/21 0021.
 */
public class MgConverterTest {

    private MongoDB db = null;

    private MgConverter converter;

    @Before
    public void setUp() throws Exception {
        db = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams("192.168.4.128", 27017, "warehouse"));
        converter = new MgConverter(db);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testGetProtobuf() throws Exception {
        byte[] pbf = converter.getProtobuf(15, 26629, 12649, WarehouseDataType.SourceType.Dig);

        assertTrue(pbf.length > 0);
    }

    @Test
    public void testGetGeojson() throws Exception {
        long t1 = System.currentTimeMillis();

        String json = converter.getGeojson(116.32133, 39.90713, 116.38658, 39.95051, WarehouseDataType.LayerType.PoiDayEditHeatmap);

        long t2 = System.currentTimeMillis();

        System.out.println("jackson:" + (t2-t1));

        assertTrue(json.length() > 0);
    }
}