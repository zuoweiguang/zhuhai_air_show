package com.navinfo.mapspotter.foundation.io;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Created by SongHuiXing on 5/12 0012.
 */
public class PostGISTest {

    private PostGISDatabase postGISDatabase;

    private OracleDatabase oracleDatabase;

    @Before
    public void setup() throws Exception{
        DataSourceParams params = IOUtil.makePostGISParam("localhost", 8202, "postgis_22_sample", "reynold", "zaq1");

        postGISDatabase = (PostGISDatabase)DataSource.getDataSource(params);

        DataSourceParams oraParam = IOUtil.makeOracleParams("192.168.4.166", 1521, "sales", "gdb_16sum2", "zaq1");
        oracleDatabase = (OracleDatabase)DataSource.getDataSource(oraParam);
    }

    @After
    public void cleanup(){
        if (postGISDatabase != null) {
            postGISDatabase.close();
        }
    }

    @Test
    public void testQuery(){

        try(SqlCursor cursor = postGISDatabase.query("SELECT link_pid, ST_AsEWKT(geom), ST_AsEWKT(ST_Buffer(geom, 0.2)) FROM base_road WHERE gid=3281")) {

            cursor.next();

            int pid = cursor.getInteger(1);
            String wkt = cursor.getString(2);
            String buffer = cursor.getString(3);

            System.out.println("PID = " + pid);
            System.out.println("WKT = " + wkt);
            System.out.println("BUFFER = " + buffer);
        } catch (SQLException e){

        }
    }

    @Test
    public void testInsert(){

        String querySql = "select LINK_PID, KIND, DIRECT, FUNCTION_CLASS, GEOMETRY from RD_LINK " +
                            " where LINK_PID=340";
        try(OracleCursor oraCursor = (OracleCursor) oracleDatabase.query(querySql)){

            PreparedStatement stmt =
                    postGISDatabase.prepare("Insert into road(pid, kind, direct, functionclass, geom) VALUES(?, ?, ?, ?, ST_GeomFromWKB(?))");

            while (oraCursor.next()){
                postGISDatabase.excute(stmt,
                                        oraCursor.getInteger(1),
                                        oraCursor.getInteger(2),
                                        oraCursor.getInteger(3),
                                        oraCursor.getInteger(4),
                                        oraCursor.getWellKnownBytes(5));
            }

            stmt.close();
            stmt = null;

        } catch (Exception e){

        }
    }

    @Test
    public void getWKTFromGEOM() throws SQLException {
        try(SqlCursor cursor = postGISDatabase.query("SELECT ST_AsText(geom) FROM road where pid = 340")) {

            cursor.next();

            String wkt = cursor.getString(1);

            System.out.println("WKT = " + wkt);
        } catch (SQLException e){

        }
    }

    @Test
    public void getWKBFromGEOM() throws SQLException {
        try(SqlCursor cursor = postGISDatabase.query("SELECT ST_AsBinary(geom) FROM road where pid=340")) {

            cursor.next();

            byte[] wkb = cursor.getBytes(1);

            System.out.println("WKB = " + wkb);
        } catch (SQLException e){

        }
    }
}
