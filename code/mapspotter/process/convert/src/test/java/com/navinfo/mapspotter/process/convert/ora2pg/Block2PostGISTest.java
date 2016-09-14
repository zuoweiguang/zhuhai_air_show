package com.navinfo.mapspotter.process.convert.ora2pg;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 7/21 0021.
 */
public class Block2PostGISTest {

    private Block2PostGIS block2PostGIS =
            new Block2PostGIS("192.168.4.104",
                            5440,
                            "navinfo",
                            "postgres",
                            "navinfo1!pg");

    @Before
    public void setUp() throws Exception {
        block2PostGIS.open();
    }

    @After
    public void tearDown() throws Exception {
        block2PostGIS.close();
    }

    @Test
    public void testImportFromGeojson() throws Exception {
        int count = block2PostGIS.importFromGeojson(new File("E:\\WorkSpace\\data\\历史Block"));

        System.out.print(count);

        assertTrue(count > 0);
    }
}