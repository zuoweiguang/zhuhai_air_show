package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
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

    private MysqlDatabase db = null;

    @Before
    public void setup() throws Exception{
        db = new MysqlDatabase();

        DataSourceParams params = new DataSourceParams();
        params.setHost("localhost");
        params.setPort(3306);
        params.setDb("reynold");
        params.setUser("root");
        params.setPassword("1qaz");

        db.open(params);
    }

    @After
    public void teardown() throws Exception{
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

        res1 = db.execute("Insert Into testTable Values(2, \'April\', \'Sun\')");
        assertEquals(0, res1);

        int res2 = db.execute("Delete From testTable Where ID=1");
        assertEquals(0, res2);

        int res3 = db.execute("Drop Table testTable");
        assertEquals(0, res3);
    }

    @Test
    public void testQuery() throws Exception {
        SqlCursor cursor = db.query("Select * From TestName");

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
        db.executeMany(sql, many);
    }

    @Test
    public void testGetGeometry() throws Exception{
        SqlCursor cursor = db.query("Select Name, Job, Salary, AsText(HomeLink) as Link From TestName " +
                                    "Where ID=2");

        assertTrue(cursor.next());

        Geometry geo = cursor.getWKTGeometry("Link");

        assertEquals("LineString", geo.getGeometryType());

        cursor.close();
    }

    @Test(expected = SQLException.class)
    public void testClose() throws Exception {
        db.close();
        db.query("Select * From TestName");
    }
}