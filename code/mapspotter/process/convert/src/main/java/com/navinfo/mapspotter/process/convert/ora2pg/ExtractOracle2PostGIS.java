package com.navinfo.mapspotter.process.convert.ora2pg;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKB;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

/**
 * 从Oracle提取定义的信息到PostgreSQL
 * Created by SongHuiXing on 5/16 0016.
 * updated by weiguang on 2016.6.7
 */
public class ExtractOracle2PostGIS {

    private final DataSourceParams oraParams;
    private final DataSourceParams pgParams;

    private final String username = "gdb_16aut";
    private final String password = "zaq1";
    private final String ip = "192.168.4.166";
    private final int port = 1521;
    private final String serviceName = "orcl";
    private final String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + serviceName;


    public ExtractOracle2PostGIS(String orahost, int oraPort, String oraDb, String oraUser, String oraPwd,
                                 String pghost, int pgPort, String pgDb, String pgUser, String pgPwd){
        oraParams = IOUtil.makeOracleParams(orahost, oraPort, oraDb, oraUser, oraPwd);
        pgParams = IOUtil.makePostGISParam(pghost, pgPort, pgDb, pgUser, pgPwd);
    }

    public void execute_road () {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_road = null;
        int total = 0;
        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE road_form DROP CONSTRAINT fk_roadlink_form";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            dropReferences = "ALTER TABLE road_name DROP CONSTRAINT fk_roadname_form";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS road";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table road(" +
                    "pid int not null," +
                    "snodeid int," +
                    "enodeid int," +
                    "kind smallint," +
                    "direct smallint," +
                    "appinfo smallint," +
                    "tollinfo smallint," +
                    "functionclass smallint," +
                    "lane_left smallint," +
                    "lane_right smallint," +
                    "is_viaduct boolean," +
                    "center_divider smallint," +
                    "geom geometry(LINESTRING)," +
                    "constraint PK_ROAD_PID primary key (pid)" +
                    ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            System.out.println("extract oracle rd_link to postgre road......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM RD_LINK t";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            stmt_road = pgDb.prepare("Insert into road(pid, snodeid, enodeid, kind, direct, appinfo, tollinfo, functionclass, " +
                    "lane_left, lane_right, is_viaduct, center_divider, geom) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, st_geomfromewkb(?))");

//            String sql = "SELECT /*+ parallel(t,50) */ LINK_PID, S_NODE_PID, E_NODE_PID, KIND, DIRECT, APP_INFO, " +
//                    "TOLL_INFO, FUNCTION_CLASS, " +
//                    "LANE_LEFT, LANE_RIGHT, IS_VIADUCT, CENTER_DIVIDER, GEOMETRY FROM temp_rd_link_for_export";

            String sql = "SELECT LINK_PID, S_NODE_PID, E_NODE_PID, KIND, DIRECT, APP_INFO, TOLL_INFO, FUNCTION_CLASS, " +
                    "LANE_LEFT, LANE_RIGHT, IS_VIADUCT, CENTER_DIVIDER, GEOMETRY FROM rd_link";
            stmt = conn.createStatement();
            stmt.setQueryTimeout(60 * 60 * 4);
            resultSet = stmt.executeQuery(sql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;

            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("LINK_PID");
                    Integer snode_pid = resultSet.getInt("S_NODE_PID");
                    Integer enode_pid = resultSet.getInt("E_NODE_PID");
                    Integer kind = resultSet.getInt("KIND");
                    Integer direct = resultSet.getInt("DIRECT");
                    Integer appinfo = resultSet.getInt("APP_INFO");
                    Integer tollinfo = resultSet.getInt("TOLL_INFO");
                    Integer functionclass = resultSet.getInt("FUNCTION_CLASS");
                    Integer lane_left = resultSet.getInt("LANE_LEFT");
                    Integer lane_right = resultSet.getInt("LANE_RIGHT");
                    Integer is_viaduct = resultSet.getInt("IS_VIADUCT");
                    Boolean viaduct = false;
                    if (is_viaduct == 1) {
                        viaduct = true;
                    }
                    Integer center_divider = resultSet.getInt("CENTER_DIVIDER");
//                    String geom_wkt = resultSet.getString("GEOMETRY");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    //保存road
                    pgDb.excute(stmt_road, link_pid, snode_pid, enode_pid, kind, direct, appinfo, tollinfo, functionclass,
                            lane_left, lane_right, viaduct, center_divider, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "road [total:%s | counter:%s] [use time:%s:%s:%s]";
                        System.out.println(String.format(print_info, total, counter, hour, min, sec));
                    }
                    if (commit_counter % 5000 == 0) {
                        pgDb.execute("commit");
                        pgDb.execute("begin");
                        commit_counter = 0;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //创建索引
            String createIndex = "CREATE INDEX IDX_ROAD_PID ON road (pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_ROAD_GEOM ON road USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_road != null) {stmt_road.close();stmt_road = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }


    public void execute_road_form () {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_road_form = null;
        int total = 0;
        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE road_form DROP CONSTRAINT fk_roadlink_form";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS road_form";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table road_form(" +
                                        "link_pid int not null," +
                                        "form smallint" +
                                    ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            System.out.println("extract oracle rd_link_form to postgre road_form......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM rd_link_form t";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            stmt_road_form = pgDb.prepare("Insert into road_form(link_pid, form) VALUES (?, ?)");

            oraSql = "SELECT COUNT(1) FROM rd_link_form";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}
            //抽取 form 语句
            oraSql = "SELECT link_pid, form_of_way FROM rd_link_form";
            stmt = conn.createStatement();
            stmt.setQueryTimeout(60 * 60 * 3);
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
                    Integer form = resultSet.getInt("form_of_way");
                    pgDb.excute(stmt_road_form, link_pid, form);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "road_form [total:%s | counter:%s] [use time:%s:%s:%s]";
                        System.out.println(String.format(print_info, total, counter, hour, min, sec));
                    }
                    if (commit_counter % 5000 == 0) {
                        pgDb.execute("commit");
                        pgDb.execute("begin");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pgDb.execute("COMMIT");
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //创建索引
            String createIndex = "create index IDX_ROADFORM_ID on road_form (link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE road_form ADD CONSTRAINT fk_roadlink_form FOREIGN KEY (link_pid) " +
                    " REFERENCES road (pid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_road_form != null) {stmt_road_form.close();stmt_road_form = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }


    public void execute_road_name () {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_road_name = null;
        int total = 0;
        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE road_name DROP CONSTRAINT fk_roadname_form";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS road_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table road_name(" +
                    "link_pid int not null," +
                    "nametype smallint," +
                    "nameclass smallint," +
                    "name varchar(255)" +
                    ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            System.out.println("extract oracle rd_link_name to postgre road_name......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM rd_link_name t";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            stmt_road_name = pgDb.prepare("Insert into road_name(link_pid, nametype, nameclass, name) VALUES (?, ?, ?, ?)");

            oraSql = "SELECT COUNT(a.LINK_PID) from rd_link_name a " +
                    "JOIN RD_NAME b ON a.name_groupid=b.name_groupid " +
                    "WHERE b.LANG_CODE='CHI' AND a.SEQ_NUM=1 AND a.name_class=1";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}
            //抽取 name 语句
            oraSql = "SELECT a.LINK_PID, a.NAME_TYPE, a.NAME_CLASS, b.NAME from rd_link_name a " +
                    "JOIN RD_NAME b ON a.name_groupid=b.name_groupid " +
                    "WHERE b.LANG_CODE='CHI' AND a.SEQ_NUM=1 AND a.name_class=1";
            stmt = conn.createStatement();
            stmt.setQueryTimeout(60 * 60 * 3);
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("LINK_PID");
                    Integer nametype = resultSet.getInt("NAME_TYPE");
                    Integer nameclass = resultSet.getInt("NAME_CLASS");
                    String name = resultSet.getString("NAME");
                    pgDb.excute(stmt_road_name, link_pid, nametype, nameclass, name);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "road_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        System.out.println(String.format(print_info, total, counter, hour, min, sec));
                    }
                    if (commit_counter % 5000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //创建索引
            String createIndex = "create index IDX_ROADNAME_ID on road_name (link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);

            //重新创建外键
            String createReferences = "ALTER TABLE road_name ADD CONSTRAINT fk_roadname_form FOREIGN KEY (link_pid) " +
                    " REFERENCES road (pid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_road_name != null) {stmt_road_name.close();stmt_road_name = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }




    public void execute_railway() {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_railway = null;
        PreparedStatement stmt_railway_form = null;
        PreparedStatement stmt_railway_name = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE railway_form DROP CONSTRAINT FK_RAILWAY_FORM";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            dropReferences = "ALTER TABLE railway_name DROP CONSTRAINT FK_ROADNAME_FORM";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS railway";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS railway_form";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS railway_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "CREATE TABLE railway(" +
                                    "lineid INT NOT NULL," +
                                    "fid int NOT NULL," +
                                    "snodeid INT," +
                                    "enodeid INT," +
                                    "kind SMALLINT," +
                                    "form SMALLINT," +
                                    "geom GEOMETRY(LINESTRING)," +
                                    "CONSTRAINT PK_RAILWAY_PID PRIMARY KEY (lineid)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table railway_form(" +
                                "lineid int not null," +
                                "form smallint" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table railway_name(" +
                                "lineid int not null," +
                                "nametype smallint," +
                                "nameclass smallint," +
                                "name varchar(255)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_railway = pgDb.prepare("INSERT INTO railway(lineid, fid, snodeid, enodeid, kind, geom) " +
                                            "VALUES (?, ?, ?, ?, ?, st_geomfromewkb(?))");
            stmt_railway_form = pgDb.prepare("INSERT INTO railway_form(lineid, form) VALUES (?, ?)");
            stmt_railway_name = pgDb.prepare("INSERT INTO railway_name(lineid, nametype, nameclass, name) " +
                                                "VALUES (?, ?, ?, ?)");

            System.out.println("extract oracle rw_link to postgre railway, railway_form......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM rw_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 rw_link 语句
            oraSql = "SELECT link_pid, feature_pid, s_node_pid, e_node_pid, kind, form, " +
                    "geometry FROM rw_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer lineid = resultSet.getInt("link_pid");
                    Integer fid = resultSet.getInt("feature_pid");
                    Integer snodeid = resultSet.getInt("s_node_pid");
                    Integer enodeid = resultSet.getInt("e_node_pid");
                    Integer kind = resultSet.getInt("kind");
                    Integer form = resultSet.getInt("form");
//                    String geom = resultSet.getString("geometry");

                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_railway, lineid, fid, snodeid, enodeid, kind, geom_wkb);

                    //保存railway_form
                    pgDb.excute(stmt_railway_form, lineid, form);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "railway [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 5000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            oraSql = "SELECT COUNT(a.link_pid) FROM rw_link_name a JOIN RD_NAME b ON a.name_groupid=b.name_groupid " +
                    "WHERE b.LANG_CODE='CHI'";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}
            //抽取SQL语句
            oraSql = "SELECT a.link_pid, b.NAME FROM rw_link_name a JOIN RD_NAME b ON a.name_groupid=b.name_groupid " +
                        "WHERE b.LANG_CODE='CHI'";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer lineid = resultSet.getInt("link_pid");
                    String name = resultSet.getString("NAME");
                    pgDb.excute(stmt_railway_name, lineid, 0, 1, name);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "railway_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 5000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //创建索引
            String createIndex = "CREATE INDEX IDX_RAILWAY_LINEID ON railway (lineid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAY_FEATUREID on railway(fid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAY_GEOM ON railway USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAYFORM_LINEID ON railway_form (lineid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAYNAME_LINEID ON railway_name (lineid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE railway_form ADD CONSTRAINT FK_RAILWAY_FORM FOREIGN KEY (lineid) " +
                                        " REFERENCES railway (lineid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);
            createReferences = "ALTER TABLE railway_name ADD CONSTRAINT FK_ROADNAME_FORM FOREIGN KEY (lineid) " +
                    " REFERENCES railway (lineid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_railway != null) { stmt_railway.close();stmt_railway = null;}
                if (stmt_railway_form != null) { stmt_railway_form.close();stmt_railway_form = null;}
                if (stmt_railway_name != null) { stmt_railway_name.close();stmt_railway_name = null;}
                if (conn != null) { conn.close();conn = null;}
                if (stmt != null) { stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");
    }


    public void execute_railway_point() {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_railway_point = null;
        PreparedStatement stmt_railway_point_form = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE railway_point_form DROP CONSTRAINT FK_RAILWAY_FORM";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS railway_point";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS railway_point_form";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table railway_point(" +
                                        "pointid int not null," +
                                        "geom geometry(POINT)," +
                                        "constraint PK_RAILWAYPOINT_PID primary key (pointid)" +
                                    ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table railway_point_form(" +
                                "pointid int not null," +
                                "form smallint" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            pgDb.execute("BEGIN TRANSACTION");
            //灌库SQL语句
            stmt_railway_point = pgDb.prepare("INSERT INTO railway_point(pointid, geom) VALUES (?, st_geomfromewkb(?))");
            stmt_railway_point_form = pgDb.prepare("INSERT INTO railway_point_form(pointid, form) VALUES (?, ?)");

            System.out.println("extract oracle rw_node to postgre railway_point, railway_point_form......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM rw_node";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 rw_node 语句
            oraSql = "SELECT node_pid, form, geometry FROM rw_node";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer pointid = resultSet.getInt("node_pid");
                    Integer form = resultSet.getInt("form");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_railway_point, pointid, geom_wkb);

                    pgDb.excute(stmt_railway_point_form, pointid, form);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "railway_point [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //创建索引
            String createIndex = "CREATE INDEX IDX_RAILWAYPOINT_POINTID ON railway_point(pointid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAYPOINT_GEOM ON railway_point USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_RAILWAYPOINTFORM_POINTID ON railway_point_form(pointid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE railway_point_form ADD CONSTRAINT FK_RAILWAY_FORM FOREIGN KEY (pointid) " +
                    "REFERENCES railway_point (pointid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_railway_point != null) {stmt_railway_point.close();stmt_railway_point = null;}
                if (stmt_railway_point_form != null) {stmt_railway_point_form.close();stmt_railway_point_form = null;}
                if (conn != null) { conn.close();conn = null;}
                if (stmt != null) { stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");
    }

    public void execute_region_flagpoint_name_face() {
        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_region_flagpoint = null;
        PreparedStatement stmt_region_name = null;
        PreparedStatement stmt_region_face = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE region_name DROP CONSTRAINT FK_REGION_NAME_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS region_flagpoint";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS region_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS region_face";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table region_flagpoint(" +
                                    "regionid int not null," +
                                    "adminid int," +
                                    "extendid int," +
                                    "parent_regionid int," +
                                    "admin_type float," +
                                    "capital smallint," +
                                    "population real," +
                                    "geom geometry(POINT)," +
                                    "constraint PK_REGIONFLAG_PID primary key (regionid)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table region_name(" +
                                "regionid int not null," +
                                "group_id int not null," +
                                "langcode varchar(10)," +
                                "nameclass smallint," +
                                "name varchar(255)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table region_face(" +
                                "faceid int not null," +
                                "regionid int not null," +
                                "boundary varchar(255)," +
                                "geom geometry(POLYGON)," +
                                "constraint PK_REGION_FACE_ID primary key (faceid)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_region_flagpoint = pgDb.prepare("INSERT INTO region_flagpoint(regionid, adminid, extendid, " +
                                "admin_type, capital, population, geom) VALUES (?, ?, ?, ?, ?, ?, st_geomfromewkb(?))");
            stmt_region_name = pgDb.prepare("INSERT INTO region_name(regionid, group_id, langcode, nameclass, name)" +
                                            "VALUES (?, ?, ?, ?, ?)");
            stmt_region_face = pgDb.prepare("INSERT INTO region_face(faceid, regionid, geom)" +
                    "VALUES (?, ?, st_geomfromewkb(?))");

            System.out.println("extract oracle ad_admin, ad_admin_name, ad_face to postgre region_flagpoint, region_name, region_face......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM ad_admin";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 ad_admin 语句
            oraSql = "SELECT region_id, admin_id, extend_id, " +
                    "admin_type, capital, population, geometry FROM ad_admin";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer regionid = resultSet.getInt("region_id");
                    Integer adminid = resultSet.getInt("admin_id");
                    Integer extendid = resultSet.getInt("extend_id");
                    Double admin_type = resultSet.getDouble("admin_type");
                    Integer capital = resultSet.getInt("capital");
                    Integer population = resultSet.getInt("population");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_region_flagpoint, regionid, adminid, extendid, admin_type, capital, population, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "region_flagpoint [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            //查询抽取数据的总量
            oraSql = "SELECT COUNT(1) FROM ad_admin_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //抽取 ad_admin_name 语句
            oraSql = "SELECT region_id, name_groupid, lang_code, name_class, name FROM ad_admin_name " +
                        "WHERE lang_code='CHI' AND name_class=1";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer regionid = resultSet.getInt("region_id");
                    Integer group_id = resultSet.getInt("name_groupid");
                    String langcode = resultSet.getString("lang_code");
                    Integer nameclass = resultSet.getInt("name_class");
                    String name = resultSet.getString("name");
                    pgDb.excute(stmt_region_name, regionid, group_id, langcode, nameclass, name);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "region_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            //查询抽取数据的总量
            oraSql = "SELECT COUNT(1) FROM ad_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();}
            resultSet = null;
            if (stmt != null) {stmt.close();}
            stmt = null;

            //抽取 ad_admin_name 语句
            oraSql = "SELECT region_id, face_pid, geometry FROM ad_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer regionid = resultSet.getInt("region_id");
                    Integer faceid = resultSet.getInt("face_pid");
//                    String face_geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_region_face, faceid, regionid, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "region_face [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_REGIONFLAGPOINT_REGIONID ON region_flagpoint(regionid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGIONFLAG_PARENTID ON region_flagpoint(parent_regionid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGIONFLAGPOINT_GEOM ON region_flagpoint USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGION_FACE_REGIONID ON region_face(regionid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGIONFACE_GEOM ON region_face USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGION_NAME_ID ON region_name(regionid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGION_NAME_GROUPID ON region_name(group_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE region_name ADD CONSTRAINT FK_REGION_NAME_ID FOREIGN KEY (regionid) " +
                                    " REFERENCES region_flagpoint (regionid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_region_flagpoint != null) {stmt_region_flagpoint.close();stmt_region_flagpoint = null;}
                if (stmt_region_name != null) {stmt_region_name.close();stmt_region_name = null;}
                if (stmt_region_face != null) {stmt_region_face.close();stmt_region_face = null;}
                if (conn != null) { conn.close();conn = null;}
                if (stmt != null) { stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }


    public void execute_region_link() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_region_link = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除表
//            String dropSql = "DROP TABLE IF EXISTS region_link";
//            pgDb.execute(dropSql);
//            System.out.println(dropSql);
//            //重建表
//            String createTable = "create table region_link(" +
//                                    "link_pid int not null," +
//                                    "kind smallint," +
//                                    "form smallint," +
//                                    "geom geometry(LINESTRING)," +
//                                    "constraint PK_REGIONLINK_ID primary key (link_pid)" +
//                                ")";
//            pgDb.execute(createTable);
//            System.out.println(createTable);

            //灌库SQL语句
            stmt_region_link = pgDb.prepare("INSERT INTO region_link(link_pid, kind, form, geom) VALUES (?, ?, ?, st_geomfromewkb(?))");

            System.out.println("extract oracle ad_link to postgre region_link......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM ad_link_100w";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 ad_link 语句
            oraSql = "SELECT link_pid, kind, form, geometry FROM ad_link_100w";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
                    Integer kind = resultSet.getInt("kind");
                    Integer form = resultSet.getInt("form");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);
                    pgDb.excute(stmt_region_link, link_pid, kind, form, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "region_link [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_REGIONLINK_LINKID ON region_link(link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_REGIONLINK_GEOM ON region_link USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_region_link != null) {stmt_region_link.close();stmt_region_link = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();pgDb = null;}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }

    public void execute_landcover_name() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_landcover = null;
        PreparedStatement stmt_landcover_name = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE landcover_name DROP CONSTRAINT FK_LANDCOVER_NAME_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS landcover";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS landcover_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table landcover(" +
                                    "id int not null," +
                                    "kind smallint," +
                                    "form smallint," +
                                    "boundary varchar(255)," +
                                    "geom geometry(POLYGON)," +
                                    "constraint PK_LANDCOVER_ID primary key(id)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table landcover_name(" +
                                "id  int not null," +
                                "group_id int not null," +
                                "langcode varchar(10)," +
                                "name varchar(255)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_landcover = pgDb.prepare("INSERT INTO landcover(id, kind, form, geom) VALUES (?, ?, ?, st_geomfromewkb(?))");
            stmt_landcover_name = pgDb.prepare("INSERT INTO landcover_name(id, group_id, langcode, name) VALUES (?, ?, ?, ?)");

            System.out.println("extract oracle lc_face, lc_face_name to postgre landcover, landcover_name......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM lc_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 lc_face 语句
            oraSql = "SELECT face_pid, kind, form, geometry FROM lc_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer id = resultSet.getInt("face_pid");
                    Integer kind = resultSet.getInt("kind");
                    Integer form = resultSet.getInt("form");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_landcover, id, kind, form, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "landcover_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            oraSql = "SELECT COUNT(1) from lc_face_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}
            //抽取 lc_face 语句
            oraSql = "SELECT face_pid, name_groupid, lang_code, name from lc_face_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer id = resultSet.getInt("face_pid");
                    Integer group_id = resultSet.getInt("name_groupid");
                    String langcode  = resultSet.getString("lang_code");
                    String name  = resultSet.getString("name");
                    pgDb.excute(stmt_landcover_name, id, group_id, langcode, name);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "landcover_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_LANDCOVER_ID ON landcover(id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDCOVER_GEOM ON landcover USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDCOVER_NAME_ID ON landcover_name(id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDCOVER_NAME_GROUPID ON landcover_name(group_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE landcover_name ADD CONSTRAINT FK_LANDCOVER_NAME_ID FOREIGN KEY (id) " +
                    " REFERENCES landcover (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_landcover != null) {stmt_landcover.close();stmt_landcover = null;}
                if (stmt_landcover_name != null) {stmt_landcover_name.close();stmt_landcover_name = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }

    public void execute_landcover_link_kind() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_landcover_link = null;
        PreparedStatement stmt_landcover_link_kind = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE landcover_link_kind DROP CONSTRAINT FK_LANDCOVER_LINK_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS landcover_link";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS landcover_link_kind";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table landcover_link(" +
                                    "link_pid int not null," +
                                    "geom geometry(LINESTRING)," +
                                    "constraint PK_LANDCOVER_LINK_ID primary key (link_pid)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table landcover_link_kind(" +
                                "link_pid int not null," +
                                "kind smallint," +
                                "form smallint" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_landcover_link = pgDb.prepare("INSERT INTO landcover_link(link_pid, geom) VALUES (?, st_geomfromewkb(?))");
            stmt_landcover_link_kind = pgDb.prepare("INSERT INTO landcover_link_kind(link_pid, kind, form) VALUES (?, ?, ?)");

            System.out.println("extract oracle lc_link, lc_link_kind to postgre landcover_link, landcover_link_kind......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM lc_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取 lc_link 语句
            oraSql = "SELECT link_pid, geometry FROM lc_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_landcover_link, link_pid, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "landcover_link [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            oraSql = "SELECT COUNT(1) from lc_link_kind";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}
            //抽取 lc_link_kind 语句
            oraSql = "SELECT link_pid, kind, form from lc_link_kind";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
                    Integer kind = resultSet.getInt("kind");
                    Integer form  = resultSet.getInt("form");
                    pgDb.excute(stmt_landcover_link_kind, link_pid, kind, form);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "landcover_link [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_LANDCOVER_LINK_ID ON landcover_link(link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDCOVER_LINK_GEOM ON landcover_link USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDCOVER_LINKKIND_ID ON landcover_link_kind(link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE landcover_link_kind ADD CONSTRAINT FK_LANDCOVER_LINK_ID FOREIGN KEY (link_pid) " +
                    " REFERENCES landcover_link (link_pid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_landcover_link != null) {stmt_landcover_link.close();stmt_landcover_link = null;}
                if (stmt_landcover_link_kind != null) {stmt_landcover_link_kind.close();stmt_landcover_link_kind = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }



    public void execute_landuse() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_landuse = null;
        PreparedStatement stmt_landuse_name = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE landuse_name DROP CONSTRAINT FK_LANDUSE_NAME_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS landuse";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS landuse_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table landuse(" +
                                    "id int not null," +
                                    "kind smallint," +
                                    "boundary varchar(255)," +
                                    "geom geometry(POLYGON)," +
                                    "constraint PK_LANDUSE_ID primary key(id)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table landuse_name(" +
                                "id int not null," +
                                "group_id int not null," +
                                "langcode varchar(10)," +
                                "name varchar(255)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            pgDb.execute("BEGIN TRANSACTION");
            //灌库SQL语句
            stmt_landuse = pgDb.prepare("INSERT INTO landuse(id, kind, geom) VALUES (?, ?, st_geomfromewkb(?))");
            stmt_landuse_name = pgDb.prepare("INSERT INTO landuse_name(id, group_id, langcode, name) VALUES (?, ?, ?, ?)");

            System.out.println("extract oracle lu_face, lu_face_name to postgre landuse, landuse_name......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM lu_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取SQL语句
            oraSql = "SELECT face_pid, kind, geometry FROM lu_face";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer face_pid = resultSet.getInt("face_pid");
                    Integer kind = resultSet.getInt("kind");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_landuse, face_pid, kind, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "landuse [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            //查询抽取数据的总量
            oraSql = "SELECT COUNT(1) FROM lu_face_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //抽取SQL语句
            oraSql = "SELECT face_pid, name_groupid, lang_code, name from lu_face_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer face_pid = resultSet.getInt("face_pid");
                    Integer groupId = resultSet.getInt("name_groupid");
                    String langCode  = resultSet.getString("lang_code");
                    String name = resultSet.getString("name");
                    pgDb.excute(stmt_landuse_name, face_pid, groupId, langCode, name);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "landuse_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN TRANSACTION");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_LANDUSE_ID ON landuse(id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDUSE_GEOM ON landuse USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDUSE_NAME_ID ON landuse_name(id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDUSE_NAME_GROUPID ON landuse_name(group_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE landuse_name ADD CONSTRAINT FK_LANDUSE_NAME_ID FOREIGN KEY (id) " +
                    " REFERENCES landuse (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_landuse != null) {stmt_landuse.close();stmt_landuse = null;}
                if (stmt_landuse_name != null) {stmt_landuse_name.close();stmt_landuse_name = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }

    public void execute_landuse_link() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_landuse_link = null;
        PreparedStatement stmt_landuse_link_kind = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE landuse_link_kind DROP CONSTRAINT FK_LANDUSE_LINK_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS landuse_link";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS landuse_link_kind";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table landuse_link(" +
                                    "link_pid int not null," +
                                    "geom geometry(LINESTRING)," +
                                    "constraint PK_LANDUSE_LINK_ID primary key (link_pid)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table landuse_link_kind(" +
                                "link_pid int not null," +
                                "kind smallint" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_landuse_link = pgDb.prepare("INSERT INTO landuse_link(link_pid, geom) VALUES (?, st_geomfromewkb(?))");
            stmt_landuse_link_kind = pgDb.prepare("INSERT INTO landuse_link_kind(link_pid, kind) VALUES (?, ?)");

            System.out.println("extract oracle lu_link, lu_link_kind to postgre landuse_link, landuse_link_kind......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM lu_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            //抽取SQL语句
            oraSql = "SELECT link_pid, geometry FROM lu_link";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_landuse_link, link_pid, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "landuse_link [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");
            //查询抽取数据的总量
            oraSql = "SELECT COUNT(1) FROM lu_link_kind";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            //抽取SQL语句
            oraSql = "SELECT link_pid, kind from lu_link_kind";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
                    Integer kind = resultSet.getInt("kind");
                    pgDb.excute(stmt_landuse_link_kind, link_pid, kind);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "landuse_link_kind [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_LANDUSE_LINK_ID ON landuse_link(link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDUSE_LINK_GEOM ON landuse_link USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_LANDUSE_LINKKIND_ID ON landuse_link_kind(link_pid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE landuse_link_kind ADD CONSTRAINT FK_LANDUSE_LINK_ID FOREIGN KEY (link_pid) " +
                    " REFERENCES landuse_link (link_pid) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_landuse_link != null) {stmt_landuse_link.close();stmt_landuse_link = null;}
                if (stmt_landuse_link_kind != null) {stmt_landuse_link_kind.close();stmt_landuse_link_kind = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }



    public void execute_city() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_city_model = null;
        PreparedStatement stmt_city_model_face = null;
        PreparedStatement stmt_city_model_name = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除外键
            String dropReferences = "ALTER TABLE city_model_face DROP CONSTRAINT FK_CITYMODELFACE_CM_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            dropReferences = "ALTER TABLE city_model_name DROP CONSTRAINT FK_CITYMODELNAME_CM_ID";
            pgDb.execute(dropReferences);
            System.out.println(dropReferences);
            //删除表
            String dropSql = "DROP TABLE IF EXISTS city_model";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS city_model_face";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            dropSql = "DROP TABLE IF EXISTS city_model_name";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table city_model(" +
                                    "cm_id int not null," +
                                    "kind smallint," +
                                    "constraint PK_CITYMODEL_ID primary key (cm_id)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table city_model_face(" +
                                "faceid int not null," +
                                "cm_id int," +
                                "massing boolean," +
                                "height real," +
                                "height_acuracy real," +
                                "wall_material smallint," +
                                "geom geometry(POLYGON), " +
                                "constraint PK_CITYMODELFACE_ID primary key (faceid)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);
            createTable = "create table city_model_name(" +
                                "name_id int not null," +
                                "cm_id int," +
                                "langcode varchar(20)," +
                                "fullname varchar(500)," +
                                "basename varchar(200)," +
                                "build_number varchar(100)," +
                                "constraint PK_CITYMODELNAME_ID primary key (name_id)" +
                            ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            //灌库SQL语句
            stmt_city_model = pgDb.prepare("INSERT INTO city_model(cm_id, kind) VALUES (?, ?)");
            stmt_city_model_face = pgDb.prepare("INSERT INTO city_model_face(faceid, cm_id, massing, height, height_acuracy, " +
                    "wall_material, geom) VALUES (?, ?, ?, ?, ?, ?, st_geomfromewkb(?))");
            stmt_city_model_name = pgDb.prepare("INSERT INTO city_model_name(name_id, cm_id, langcode, fullname, basename, " +
                    "build_number) VALUES (?, ?, ?, ?, ?, ?)");

            System.out.println("extract oracle cmg_building, cmg_buildface, cmg_building_name " +
                                    "to postgre city_model, city_model_face, city_model_name......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM cmg_building";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取SQL语句
            oraSql = "SELECT pid, kind FROM cmg_building";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer pid = resultSet.getInt("pid");
                    Integer kind = resultSet.getInt("kind");
                    pgDb.excute(stmt_city_model, pid, kind);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "city_model [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 1000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}


            oraSql = "SELECT COUNT(1) FROM cmg_buildface t";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取SQL语句
            oraSql = "SELECT building_pid, face_pid, massing, height, height_acuracy, " +
                    "wall_material, geometry FROM cmg_buildface";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer pid = resultSet.getInt("building_pid");
                    Integer face_pid = resultSet.getInt("face_pid");
                    Integer massing_int = resultSet.getInt("massing");
                    Boolean massing = false;
                    if (massing_int == 1) {
                        massing = true;
                    }
                    Double height = resultSet.getDouble("height");
                    Double height_acuracy = resultSet.getDouble("height_acuracy");
                    Integer wall_material = resultSet.getInt("wall_material");
//                    String geom = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_city_model_face, face_pid, pid, massing, height, height_acuracy, wall_material, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "city_model_face [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 1000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");
            end = System.currentTimeMillis();
            sec = (int)((end - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
            System.out.println(print_info);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}


            oraSql = "SELECT COUNT(1) FROM cmg_buildface t";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取SQL语句
            oraSql = "SELECT building_pid, name_id, lang_code, full_name, base_name, build_number " +
                    "FROM cmg_building_name";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            counter = 0;
            commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer pid = resultSet.getInt("building_pid");
                    Integer name_id = resultSet.getInt("name_id");
                    String langCode = resultSet.getString("lang_code");
                    String fullName = resultSet.getString("full_name");
                    String baseName = resultSet.getString("base_name");
                    String buildNumber = resultSet.getString("build_number");
                    pgDb.excute(stmt_city_model_name, name_id, pid, langCode, fullName, baseName, buildNumber);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "city_model_name [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 1000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_CITY_MODEL_ID ON city_model(cm_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_CITY_MODEL_FACE_ID ON city_model_face(faceid)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_CITY_MODEL_FACE_GEOM ON city_model_face USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_CITY_MODEL_NAME_ID ON city_model_name(name_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            //重新创建外键
            String createReferences = "ALTER TABLE city_model_face ADD CONSTRAINT FK_CITYMODELFACE_CM_ID FOREIGN KEY (cm_id) " +
                    " REFERENCES city_model (cm_id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);
            createReferences = "ALTER TABLE city_model_name ADD CONSTRAINT FK_CITYMODELNAME_CM_ID FOREIGN KEY (cm_id) " +
                    " REFERENCES city_model (cm_id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
            pgDb.execute(createReferences);
            System.out.println(createReferences);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt_city_model != null) {stmt_city_model.close();stmt_city_model = null;}
                if (stmt_city_model_face != null) {stmt_city_model_face.close();stmt_city_model_face = null;}
                if (stmt_city_model_name != null) {stmt_city_model_name.close();stmt_city_model_name = null;}
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");


    }

    public void execute_city_link() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        PostGISDatabase pgDb = null;
        PreparedStatement stmt_city_model_link = null;
        int total = 0;

        try {
            pgDb = (PostGISDatabase)DataSource.getDataSource(this.pgParams);
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, username, password);

            //删除表
            String dropSql = "DROP TABLE IF EXISTS city_model_link";
            pgDb.execute(dropSql);
            System.out.println(dropSql);
            //重建表
            String createTable = "create table city_model_link(" +
                                    "link_id int not null," +
                                    "kind smallint," +
                                    "geom geometry(LINESTRING)," +
                                    "constraint PK_CITYMODELLINK_ID primary key (link_id)" +
                                ")";
            pgDb.execute(createTable);
            System.out.println(createTable);

            pgDb.execute("BEGIN TRANSACTION");
            //灌库SQL语句
            stmt_city_model_link = pgDb.prepare("INSERT INTO city_model_link(link_id, kind, geom) VALUES (?, ?, st_geomfromewkb(?))");

            System.out.println("extract oracle cmg_buildlink to postgre city_model_link......");
            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(1) FROM cmg_buildlink";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            pgDb.execute("BEGIN");

            //抽取SQL语句
            oraSql = "SELECT link_pid, kind, geometry FROM cmg_buildlink";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            while (resultSet.next()) {
                try {
                    Integer link_pid = resultSet.getInt("link_pid");
                    Integer kind = resultSet.getInt("kind");
//                    String geometry = resultSet.getString("geometry");
                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    byte[] geom_wkb = new WKB().fromJGeometry(sdoGeo);

                    pgDb.excute(stmt_city_model_link, link_pid, kind, geom_wkb);

                    counter++;
                    commit_counter++;
                    if (counter % 1000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "city_model_link [total:%s | counter:%s] [use time:%s:%s:%s]";
                        print_info = String.format(print_info, total, counter, hour, min, sec);
                        System.out.println(print_info);
                    }
                    if (commit_counter % 2000 == 0) {
                        pgDb.execute("COMMIT");
                        pgDb.execute("BEGIN");
                        commit_counter = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pgDb.execute("COMMIT");

            //创建索引
            String createIndex = "CREATE INDEX IDX_CITYMODEL_LINK_ID ON city_model_link(link_id)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);
            createIndex = "CREATE INDEX IDX_CITYMODEL_LINK_GEOM ON city_model_link USING gist(geom)";
            pgDb.execute(createIndex);
            System.out.println(createIndex);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) {resultSet.close();resultSet = null;}
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (pgDb != null) {pgDb.close();}
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }

    //获取配置文件数据库信息
    public static JSONObject getProperties() {

        File directory = new File("db.properties");
        String filePath = directory.getAbsolutePath();
        Properties props = new Properties();
        InputStream in = null;
        JSONObject propObj = null;

        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            String oraHost = props.getProperty("oraHost");
            int oraPort = Integer.valueOf(props.getProperty("oraPort"));
            String oraDb = props.getProperty("oraDb");
            String oraUser = props.getProperty("oraUser");
            String oraPwd = props.getProperty("oraPwd");

            String pgHost = props.getProperty("pgHost");
            int pgPort = Integer.valueOf(props.getProperty("pgPort"));
            String pgDb = props.getProperty("pgDb");
            String pgUser = props.getProperty("pgUser");
            String pgPwd = props.getProperty("pgPwd");

            propObj = new JSONObject();
            propObj.put("oraHost", oraHost);
            propObj.put("oraPort", oraPort);
            propObj.put("oraDb", oraDb);
            propObj.put("oraUser", oraUser);
            propObj.put("oraPwd", oraPwd);

            propObj.put("pgHost", pgHost);
            propObj.put("pgPort", pgPort);
            propObj.put("pgDb", pgDb);
            propObj.put("pgUser", pgUser);
            propObj.put("pgPwd", pgPwd);

//            System.out.println(oraHost +":"+oraPort+":"+oraDb+":"+oraUser+":"+oraPwd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {in.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
            return propObj;
        }
    }

    public static JSONObject getDatahub() {
        String urlStr = "http://192.168.4.188:8000/service/datahub/db/getonlybytype/?type=nationRoad";
        URL datahubUrl = null;
        StringBuffer bs = null;
        JSONObject propObj = null;
        try {
            datahubUrl = new URL(urlStr);
            HttpURLConnection urlcon = (HttpURLConnection)datahubUrl.openConnection();
            urlcon.connect();         //获取连接
            InputStream is = urlcon.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            bs = new StringBuffer();
            String l = null;
            while((l = buffer.readLine()) != null){
                bs.append(l);
            }
            System.out.println(bs.toString());

            JSONObject respJson = JSONObject.parseObject(bs.toString());
            String errmsg = respJson.getString("errmsg");
            if (errmsg.equals("success")) {
                propObj = new JSONObject();
                JSONObject dataObj = respJson.getJSONObject("data");

                propObj.put("bizType", dataObj.getString("bizType"));
                propObj.put("serverType", dataObj.getString("serverType"));
                propObj.put("oraHost", dataObj.getString("serverIp"));
                propObj.put("oraPort", dataObj.getInteger("serverPort"));
                propObj.put("oraDb", dataObj.getString("dbName"));
                propObj.put("oraUser", dataObj.getString("dbUserName"));
                propObj.put("oraPwd", dataObj.getString("dbUserPasswd"));

//                System.out.println("bizType:" + dataObj.getString("bizType") +
//                        ",serverType:" + dataObj.getString("serverType") +
//                        ",serverIp:" + dataObj.getString("serverIp") +
//                        ",serverPort:" + dataObj.getInteger("serverPort") +
//                        ",dbName:" + dataObj.getString("dbName") +
//                        ",dbUserName:" + dataObj.getString("dbUserName") +
//                        ",dbUserPasswd:" + dataObj.getString("dbUserPasswd"));
            } else {
                System.out.println("datahub 连接错误！url:" + urlStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return propObj;
        }
    }



    public static void main(String [] args) {

        // ------------------------------ 获取配置文件数据库信息 ---------------------------------------------
        JSONObject propObj = getProperties();
        String oraHost = propObj.getString("oraHost");
        int oraPort = propObj.getInteger("oraPort");
        String oraDb = propObj.getString("oraDb");
        String oraUser = propObj.getString("oraUser");
        String oraPwd = propObj.getString("oraPwd");

        String pgHost = propObj.getString("pgHost");
        int pgPort = propObj.getInteger("pgPort");
        String pgDb = propObj.getString("pgDb");
        String pgUser = propObj.getString("pgUser");
        String pgPwd = propObj.getString("pgPwd");

        // ------------------------------ 访问 datahub oracle 数据库信息 ---------------------------------------------
//        JSONObject propDatahub = getDatahub();
//        oraHost = propDatahub.getString("oraHost");
//        oraPort = propDatahub.getInteger("oraPort");
//        oraDb = propDatahub.getString("oraDb");
//        oraUser = propDatahub.getString("oraUser");
//        oraPwd = propDatahub.getString("oraPwd");


        // ------------------------------ 执行灌库 ---------------------------------------------
        ExtractOracle2PostGIS eo2g = new ExtractOracle2PostGIS(oraHost, oraPort, oraDb, oraUser, oraPwd,
                pgHost, pgPort, pgDb, pgUser, pgPwd);


        // ------------------------------ 道路 ---------------------------------------------
        //道路 road表、road_form表、road_name表
//        eo2g.execute_road();
        //road_form表
//        eo2g.execute_road_form();
        //road_name表
//        eo2g.execute_road_name();

        // ------------------------------ 铁路 ---------------------------------------------
        //铁路 railway表、railway_name表
//        eo2g.execute_railway();
        //railway_point表
//        eo2g.execute_railway_point();


        // ------------------------------ 行政区划 ---------------------------------------------
        // region_flagpoint表、region_name表、region_face表
//        eo2g.execute_region_flagpoint_name_face();
        //region_link表
        eo2g.execute_region_link();


        // ------------------------------ 土地覆盖 ---------------------------------------------
        //土地覆盖  landcover表、landcover_name表
//        eo2g.execute_landcover_name();
        //landcover_link表、landcover_link_kind表
//        eo2g.execute_landcover_link_kind();


        // ------------------------------ 土地利用 ---------------------------------------------
        //土地利用  landuse表、landuse_name表
//        eo2g.execute_landuse();
        //landuse_link表、landuse_link_kind表
//        eo2g.execute_landuse_link();


        // ------------------------------ 市街图 ---------------------------------------------
        //市街图  city_model_link表
//        eo2g.execute_city_link();
        //市街图   city_model表、city_model_face表、city_model_name表
//        eo2g.execute_city();


    }


}
