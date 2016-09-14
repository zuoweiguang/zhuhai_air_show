package com.navinfo.mapspotter.foundation.io.oracle;

import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Oracle连接测试用例
 * Created by SongHuiXing on 2015/12/31.
 */
public class OracleConnectionTest {
    private DataBase db = null;
    private DBConnection conn = null;

    @Before
    public void setUp() throws Exception {
        db = DataBase.getDatabase(DataBase.DataBaseType.Oracle);

        ConnectParams prop = new ConnectParams();
        prop.setHost("192.168.3.151");
        prop.setPort(1521);
        prop.setDb("orcl");

        db.open(prop);

        conn = db.getConnection("IDBG_15SUM_0705_BJ", "1");
    }

    @After
    public void tearDown() throws Exception {
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

        int res2 = conn.execute("Delete From testTable Where ID=1");
        assertEquals(0, res2);

        int res3 = conn.execute("Drop Table testTable");
        assertEquals(0, res3);
    }

    @Test
    public void testQuery() throws Exception {
        DBCursor cursor =  conn.query("Select * From testTable");

        assertNotNull(cursor);

        cursor.close();
    }
}