package com.navinfo.mapspotter.foundation.io.mysql;

import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 2016/1/3.
 */
public class MysqlTest {

    private DataBase db = null;
    private DBConnection conn = null;

    @Before
    public void setup() throws Exception{
        db = DataBase.getDatabase(DataBase.DataBaseType.MySql);

        ConnectParams prop = new ConnectParams();
        prop.setHost("localhost");
        prop.setPort(3306);
        prop.setDb("reynold");

        db.open(prop);

        conn = db.getConnection("root", "1qaz");
    }

    @After
    public void teardown() throws Exception{
        conn.close();
        db.close();
    }

    @Test
    public void testExecute() throws Exception {
        int res = conn.execute("Create table testTable (ID int, " +
                                                        "LastName varchar(255), " +
                                                        "FirstName varchar(255))");
        assertEquals(0, res);

        int res1 = conn.execute("Insert Into testTable Values(1, \'Json\', \'Zhao\')");
        assertEquals(0, res1);

        res1 = conn.execute("Insert Into testTable Values(2, \'April\', \'Sun\')");
        assertEquals(0, res1);

        int res2 = conn.execute("Delete From testTable Where ID=1");
        assertEquals(0, res2);

        int res3 = conn.execute("Drop Table testTable");
        assertEquals(0, res3);
    }

    @Test
    public void testQuery() throws Exception {
        DBCursor cursor = conn.query("Select * From TestName");

        assertTrue(cursor.next());

        assertEquals(1000.0, cursor.getDouble("Salary"), 0.001);

        assertEquals("Bob", cursor.getString("Name"));

        cursor.close();
    }

    @Test
    public void testAddGeometry() throws Exception{
        String sql = "INSERT INTO testname(HomeLink, Gender, Name, Job, Salary) " +
                "VALUES(GeomFromText(\'%1$s\'), ?, ?, ?, ?)";


        List<Object> row = new ArrayList<>();
        row.add(true);
        row.add("May");
        row.add("Teacher");
        row.add(2050.5);

        GeometryFactory factory = new GeometryFactory(new PrecisionModel(100000));

        Coordinate[] coords = new Coordinate[]{new Coordinate(0, 0), new Coordinate(10, 15)};
        LineString line = factory.createLineString(coords);

        WKTWriter wktWriter = new WKTWriter();
        sql = String.format(sql, wktWriter.write(line));

        List<List<Object>> many = new ArrayList<>();
        many.add(row);
        conn.executeMany(sql, many);
    }

    @Test
    public void testGetGeometry() throws Exception{
        DBCursor cursor = conn.query("Select Name, Job, Salary, AsText(HomeLink) as Link From TestName " +
                                    "Where ID=2");

        assertTrue(cursor.next());

        Geometry geo = cursor.getGeometry("Link");

        assertEquals("LineString", geo.getGeometryType());

        cursor.close();
    }

    @Test(expected = SQLException.class)
    public void testClose() throws Exception {
        DBConnection db2 = db.getConnection("root", "1qaz");

        db2.close();

        db2.query("Select * From TestName");
    }
}