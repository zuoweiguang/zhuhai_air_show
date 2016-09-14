package com.navinfo.mapspotter.foundation.io;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HbaseTest {
    private Hbase hbasedatabase;

    private OracleDatabase db = null;

    @Before
    public void init() throws IOException {
        hbasedatabase = (Hbase) DataSource.getDataSource(
                                    IOUtil.makeHBaseParam("Master.Hadoop:2181"));

        db = (OracleDatabase)DataSource.getDataSource(
                                IOUtil.makeOracleParams("192.168.4.166",
                                                        1521, "ORCL_166","gdb_16sum", "PaNgU7Star"));
    }

    @Test
    public void testOpen() throws IOException {
        int r = hbasedatabase.open(null);
        assertEquals(r, 0);
    }

    @Test
    public void getTable() throws IOException {
        Table table = hbasedatabase.getTable("road_fusion_new");
        assertNotNull(table);
    }

    @Test
    public void writeMeshInfo2HBase(){
        try(Table table = hbasedatabase.getTable("mesh_province")){
            ArrayList<Put> puts = new ArrayList<>();

            String sql = "SELECT MESH_ID, ADMIN_ID, ADMIN_NAME FROM ni_meshlist_for_dn";

            byte[] colFamily = Bytes.toBytes("information");
            byte[] meshidCol = Bytes.toBytes("meshid");
            byte[] adminidCol = Bytes.toBytes("adminid");
            byte[] adminnameCol = Bytes.toBytes("adminname");

            try(OracleCursor cursor = (OracleCursor) db.query(sql)) {

                while (cursor.next()) {
                    int meshid = cursor.getInteger(1);
                    int adminid = cursor.getInteger(2);
                    String adminname = cursor.getString(3);

                    Put pt = new Put(Bytes.toBytes(String.format("%d_%d", meshid, adminid)));

                    pt.addColumn(colFamily, meshidCol, Bytes.toBytes(meshid));
                    pt.addColumn(colFamily, adminidCol, Bytes.toBytes(adminid));
                    pt.addColumn(colFamily, adminnameCol, Bytes.toBytes(adminname));
                }

            } catch (SQLException sqlE){
                sqlE.printStackTrace();
            }

            table.put(puts);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void close() {
        hbasedatabase.close();
    }
}
