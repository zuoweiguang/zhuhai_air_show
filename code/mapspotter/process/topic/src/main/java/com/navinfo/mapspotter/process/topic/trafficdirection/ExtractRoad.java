package com.navinfo.mapspotter.process.topic.trafficdirection;

import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaojian on 2016/3/31.
 */
public class ExtractRoad {
    private static final Logger logger = Logger.getLogger(ExtractRoad.class);

    private OracleDatabase oracle = null;
    private Redis redis = null;

    private Map<String, Integer> connectionMap = new HashMap<>();
    private Map<Integer, Integer> directionMap = new HashMap<>();

    public long initialize() {
        oracle = (OracleDatabase) DataSource.getDataSource(IOUtil.makeOracleParams(
                "192.168.4.166", 1521, "orcl", "gdb_16sum2", "zaq1"
        ));

        if (oracle == null) {
            clean();
            return -1L;
        }

        redis = (Redis) DataSource.getDataSource(IOUtil.makeRedisParam(
                "192.168.4.128", 6379
        ));

        if (redis == null) {
            clean();
            return -1L;
        }

        return 0L;
    }

    public void clean() {
        if (oracle != null) {
            oracle.close();
            oracle = null;
        }

        if (redis != null) {
            redis.close();
            redis = null;
        }
    }

    public void extract(int minlen) {
        try {
            redis.transaction();
            //String sql = "select a.link_pid,a.direct,a.s_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.s_node_pid=b.s_node_pid and a.link_pid <> b.link_pid and a.length>30 and b.length>30";
            String sql = String.format("select a.link_pid,a.direct,a.s_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.s_node_pid=b.s_node_pid and a.link_pid <> b.link_pid and a.length>%d and b.length>%d" , minlen , minlen);
            OracleCursor cursor = (OracleCursor) oracle.query(sql);
            while (cursor.next()) {
                String link1 = cursor.getString(1);
                String direct1 = cursor.getString(2);
                String link2 = cursor.getString(4);
                String direct2 = cursor.getString(5);
                redis.updateString(link1 + "-" + link2, "21");
                redis.updateString(link1, direct1);
                redis.updateString(link2, direct2);
//                int link1 = cursor.getInteger(1);
//                int direct1 = cursor.getInteger(2);
//                int link2 = cursor.getInteger(4);
//                int direct2 = cursor.getInteger(5);
//                if (!connectionMap.containsKey(link2 + "-" + link1)) {
//                    connectionMap.put(link1 + "-" + link2, 21);
//                }
//                directionMap.put(link1, direct1);
//                directionMap.put(link2, direct2);
            }
            cursor.close();
            redis.commit();
        } catch (Exception e) {
            logger.error(e);
        }

        try {
            redis.transaction();
            //String sql = "select a.link_pid,a.direct,a.s_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.s_node_pid=b.e_node_pid and a.link_pid <> b.link_pid and a.length>30 and b.length>30";
            String sql = String.format("select a.link_pid,a.direct,a.s_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.s_node_pid=b.e_node_pid and a.link_pid <> b.link_pid and a.length>%d and b.length>%d" , minlen , minlen);
            OracleCursor cursor = (OracleCursor) oracle.query(sql);
            while (cursor.next()) {
                String link1 = cursor.getString(1);
                String direct1 = cursor.getString(2);
                String link2 = cursor.getString(4);
                String direct2 = cursor.getString(5);
                redis.updateString(link1 + "-" + link2, "22");
                redis.updateString(link1, direct1);
                redis.updateString(link2, direct2);
//                int link1 = cursor.getInteger(1);
//                int direct1 = cursor.getInteger(2);
//                int link2 = cursor.getInteger(4);
//                int direct2 = cursor.getInteger(5);
//                if (!connectionMap.containsKey(link2 + "-" + link1)) {
//                    connectionMap.put(link1 + "-" + link2, 22);
//                }
//                directionMap.put(link1, direct1);
//                directionMap.put(link2, direct2);
            }
            cursor.close();
            redis.commit();
        } catch (Exception e) {
            logger.error(e);
        }

        try {
            redis.transaction();
            //String sql = "select a.link_pid,a.direct,a.e_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.e_node_pid=b.s_node_pid and a.link_pid <> b.link_pid and a.length>30 and b.length>30";
            String sql = String.format("select a.link_pid,a.direct,a.e_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.e_node_pid=b.s_node_pid and a.link_pid <> b.link_pid and a.length>%d and b.length>%d" , minlen , minlen);
            OracleCursor cursor = (OracleCursor) oracle.query(sql);
            while (cursor.next()) {
                String link1 = cursor.getString(1);
                String direct1 = cursor.getString(2);
                String link2 = cursor.getString(4);
                String direct2 = cursor.getString(5);
                redis.updateString(link1 + "-" + link2, "11");
                redis.updateString(link1, direct1);
                redis.updateString(link2, direct2);
//                int link1 = cursor.getInteger(1);
//                int direct1 = cursor.getInteger(2);
//                int link2 = cursor.getInteger(4);
//                int direct2 = cursor.getInteger(5);
//                connectionMap.put(link1 + "-" + link2, 11);
//                directionMap.put(link1, direct1);
//                directionMap.put(link2, direct2);
            }
            cursor.close();
            redis.commit();
        } catch (Exception e) {
            logger.error(e);
        }
        try {
            redis.transaction();
            //String sql = "select a.link_pid,a.direct,a.e_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.e_node_pid=b.e_node_pid and a.link_pid <> b.link_pid and a.length>30 and b.length>30";
            String sql = String.format("select a.link_pid,a.direct,a.e_node_pid,b.link_pid,b.direct from rd_link a,rd_link b where a.e_node_pid=b.e_node_pid and a.link_pid <> b.link_pid and a.length>%d and b.length>%d" , minlen , minlen);
            OracleCursor cursor = (OracleCursor) oracle.query(sql);
            while (cursor.next()) {
                String link1 = cursor.getString(1);
                String direct1 = cursor.getString(2);
                String link2 = cursor.getString(4);
                String direct2 = cursor.getString(5);
                redis.updateString(link1 + "-" + link2, "12");
                redis.updateString(link1, direct1);
                redis.updateString(link2, direct2);
//                int link1 = cursor.getInteger(1);
//                int direct1 = cursor.getInteger(2);
//                int link2 = cursor.getInteger(4);
//                int direct2 = cursor.getInteger(5);
//                if (!connectionMap.containsKey(link2 + "-" + link1)) {
//                    connectionMap.put(link1 + "-" + link2, 12);
//                }
//                directionMap.put(link1, direct1);
//                directionMap.put(link2, direct2);
            }
            cursor.close();
            redis.commit();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void persistence(String path1, String path2) {
        try (FileWriter writer = new FileWriter(path1)) {
            for (Map.Entry<String, Integer> entry : connectionMap.entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            logger.error(e);
        }

        try (FileWriter writer = new FileWriter(path2)) {
            for (Map.Entry<Integer, Integer> entry : directionMap.entrySet()) {
                writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void main(String[] args) {

        int minlen = Integer.parseInt(args[0]);
        Date start = new Date();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println(df.format(start));
        ExtractRoad tool = new ExtractRoad();

        tool.initialize();

        tool.extract(minlen);

        tool.clean();

        Date end = new Date();

        System.out.println(df.format(end));
    }
}
