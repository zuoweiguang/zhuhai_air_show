package com.navinfo.mapspotter.foundation.io.oracle;

import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 2016/1/3.
 */
public class OracleCursorTest {

    private DataBase oradb = null;
    private DBConnection oraConn = null;

    @Before
    public void setUp() throws Exception {
        oradb = DataBase.getDatabase(DataBase.DataBaseType.Oracle);

        ConnectParams prop = new ConnectParams();
        prop.setHost("192.168.3.151");
        prop.setPort(1521);
        prop.setDb("orcl");

        oradb.open(prop);

        oraConn = oradb.getConnection("IDBG_15SUM_0705_BJ", "1");
    }

    @After
    public void tearDown() throws Exception {
        oraConn.close();
        oraConn = null;

        oradb.close();
        oradb = null;
    }

    @Test
    public void testGet() throws Exception {
        DBCursor cursor = oraConn.query("select * from rd_link where link_pid in (462068,462067)");

        assertTrue(cursor.next());

        int id = cursor.getInteger(0);
        assertEquals(462068, id);

        double len = cursor.getDouble("LENGTH");
        assertEquals(64.251, len, 0.001);

        assertTrue(cursor.next());

        cursor.close();
    }

    @Test
    public void testGetGeometry() throws Exception {
        DBCursor cursor = oraConn.query("select Geometry from rd_link where link_pid in (462068,462067)");

        if (cursor.next()){

            Geometry geo = cursor.getGeometry(0);
            assertNotNull(geo);

            String geoType = geo.getGeometryType();
            assertEquals("LineString", geoType);
        }

        assertTrue(cursor.next());

        cursor.close();
    }

    @Test
    public void testReset() throws Exception {
        DBCursor cursor = oraConn.query("select link_pid from rd_link where link_pid in (462068,462067)");

        while (cursor.next()){
        }

        cursor.reset();
        assertTrue(cursor.next());

        cursor.close();
    }
}