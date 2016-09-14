package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.vividsolutions.jts.geom.Geometry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Oracle连接测试用例
 * Created by SongHuiXing on 2015/12/31.
 */
public class OracleTest {
    private OracleDatabase db = null;

    @Before
    public void setUp() throws Exception {
        db = new OracleDatabase();

        DataSourceParams params = new DataSourceParams();
        params.setHost("192.168.4.166");
        params.setPort(1521);
        params.setDb("sales");
        params.setUser("gdb_16sum");
        params.setPassword("PaNgU7Star");

        db.open(params);
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testExecute() throws Exception {
        int res = db.execute("Create table testTable (ID int, " +
                                                        "LastName varchar(255), " +
                                                        "FirstName varchar(255))");
        assertEquals(0, res);

        int res1 = db.execute("Insert Into testTable Values(1, \'Json\', \'Zhao\')");
        assertEquals(0, res1);

        int res2 = db.execute("Delete From testTable Where ID=1");
        assertEquals(0, res2);

        int res3 = db.execute("Drop Table testTable");
        assertEquals(0, res3);
    }

    @Test
    public void testQuery() throws Exception {
        OracleCursor cursor =  (OracleCursor) db.query("select province_separate_py from META_16SUM.CP_MESHLIST where scale = '2.5' and admincode < '700000' group by province_separate_py");

        assertNotNull(cursor);

        while (cursor.next()) {
//            String province = cursor.getString("province_separate_py");
//
//            OracleCursor cursor2 = (OracleCursor) db.query("select l.* from RD_LINK l left join META_16SUM.CP_MESHLIST m on l.mesh_id = m.mesh where m.province_separate_py='" + province + "'");
//
//            BufferedWriter bw = new BufferedWriter(new FileWriter("E:/" + province + ".csv"));
//
//            while (cursor2.next()) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("\"" + cursor2.getString("LINK_PID") + "\",");
//                sb.append("\"" + cursor2.getWKTGeometry("GEOMETRY").toText() + "\",");
//                sb.append("\"" + cursor2.getString("MESH_ID") + "\",");
//                sb.append("\"" + cursor2.getString("KIND") + "\",");
//                sb.append("\"" + cursor2.getString("DIRECT") + "\",");
//                sb.append("\"" + cursor2.getString("APP_INFO") + "\",");
//                sb.append("\"" + cursor2.getString("TOLL_INFO") + "\",");
//                sb.append("\"" + cursor2.getString("MULTI_DIGITIZED") + "\",");
//                sb.append("\"" + cursor2.getString("SPECIAL_TRAFFIC") + "\",");
//                sb.append("\"" + cursor2.getString("FUNCTION_CLASS") + "\",");
//                sb.append("\"" + cursor2.getString("LANE_NUM") + "\",");
//                sb.append("\"" + cursor2.getString("LANE_LEFT") + "\",");
//                sb.append("\"" + cursor2.getString("LANE_RIGHT") + "\",");
//                sb.append("\"" + cursor2.getString("IS_VIADUCT") + "\",");
//                sb.append("\"" + cursor2.getString("PAVE_STATUS") + "\"\n");
//                bw.write(sb.toString());
//            }
//
//            cursor2.close();
//
//            bw.flush();
//            bw.close();
        }

        cursor.close();
    }

    @Test
    public void testGet() throws Exception {
        OracleCursor cursor = (OracleCursor) db.query("select * from rd_link where link_pid in (462068,462067)");

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
        OracleCursor cursor = (OracleCursor) db.query("select Geometry from rd_link where link_pid in (462068,462067)");

        if (cursor.next()){

            Geometry geo = cursor.getWKBGeometry(0);
            assertNotNull(geo);

            String geoType = geo.getGeometryType();
            assertEquals("LineString", geoType);
        }

        assertTrue(cursor.next());

        cursor.close();
    }

    @Test
    public void testReset() throws Exception {
        OracleCursor cursor = (OracleCursor) db.query("select link_pid from rd_link where link_pid in (462068,462067)");

        while (cursor.next()){
        }

        cursor.reset();
        assertTrue(cursor.next());

        cursor.close();
    }
}