package com.navinfo.mapspotter.foundation.io.hbase;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HbaseDatabaseTest {
    HbaseDatabase hbasedatabase;

    @Before
    public void init() throws IOException {
        hbasedatabase = new HbaseDatabase();
    }

    @Test
    public void getConnection() throws IOException {
        Connection connection = hbasedatabase.getConn();
        assertNotNull(connection);
    }

    @Test
    public void getTable() throws IOException {
        Table table = hbasedatabase.getTable("road_fusion_new");
        assertNotNull(table);
    }

    @Test
    public void getResultScanner() throws IOException {
        ResultScanner resultScanner = hbasedatabase.getResultScanner("road_fusion_new", 100, false);
        assertNotNull(resultScanner);
    }

    @After
    public void close() {
        int i = hbasedatabase.closeConnetcion();
        assertEquals(i, 0);
    }
}
