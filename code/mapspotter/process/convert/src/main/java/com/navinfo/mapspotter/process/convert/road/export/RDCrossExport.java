package com.navinfo.mapspotter.process.convert.road.export;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.OracleDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.util.DateTimeUtil;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 母库路口导出类
 * 导出(minid, maxid]区间内的路口信息
 * Created by SongHuiXing on 2016/4/5.
 */
public class RDCrossExport {
    private static Logger logger = Logger.getLogger(RDCrossExport.class);

    private static final String query_idrange_sql = "select MIN(PID), MAX(PID) from RD_Cross";

    private static final int count_per_thread = 100000;

    private int min_crossid = 0;
    private int max_crossid = 0;

    private final String oraHost;
    private final int oraPort;
    private final String oraDb;
    private final String oraUser;
    private final String oraPwd;

    public RDCrossExport(String host, int port, String db,
                         String user, String pwd){
        oraHost = host;
        oraPort = port;
        oraDb = db;
        oraUser = user;
        oraPwd = pwd;
    }

    private void queryIDRange(){
        OracleDatabase oracle = (OracleDatabase) DataSource.getDataSource(
                                                    IOUtil.makeOracleParams(oraHost,
                                                            oraPort,
                                                            oraDb,
                                                            oraUser,
                                                            oraPwd));

        try (SqlCursor cursor = oracle.query(query_idrange_sql)){
            if(cursor.next()){
                min_crossid = cursor.getInteger(1);
                max_crossid = cursor.getInteger(2) + 1;
            }
        } catch (SQLException e){
            logger.error(e.getMessage());
        }

        oracle.close();
    }

    public boolean export(String folder, boolean getBranchLinks){
        File targetPath = new File(folder);
        if(!targetPath.exists()){
            if(!targetPath.mkdir())
                return false;
        }

        File childPath = new File(folder, "cross_"+DateTimeUtil.formatDate("YYYYMMdd"));
        childPath.mkdir();

        queryIDRange();

        logger.info(String.format("Begin to export to %s, from cross %d to %d",
                                folder,
                                min_crossid,
                                max_crossid));

//        try {
//            ExportThreadImp imp =
//                    new ExportThreadImp(oraHost, oraPort, oraDb, oraUser, oraPwd,
//                                        childPath.getAbsolutePath(), 1, 200001, getBranchLinks);
//
//            imp.call();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            ExecutorService excutor = Executors.newFixedThreadPool(6);

            int preLow = min_crossid;
            while (preLow < max_crossid) {
                int upper = preLow + count_per_thread;

                Future<Integer> res = excutor.submit(
                        new ExportThreadImp(oraHost, oraPort, oraDb, oraUser, oraPwd,
                                childPath.getAbsolutePath(), preLow, upper,
                                getBranchLinks));

                preLow = upper;
            }

            excutor.shutdown();
            excutor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        } catch (Exception e){
            logger.error(e.getMessage());
        }

        return true;
    }

    public static void main(String args[]){
        logger.warn(String.format("Build cross from orcl@192.168.4.166 %s with %s to %s, with chains %s",
                                    args[0],
                                    args[1],
                                    args[2],
                                    args[3]));

        RDCrossExport export = new RDCrossExport("192.168.4.166", 1521, "orcl",
                                                args[0], args[1]);

        Boolean getBranch = new Boolean(args[3]);

        export.export(args[2], getBranch);
    }
}

class ExportThreadImp implements Callable<Integer>{

    private final String oraHost;
    private final int oraPort;
    private final String oraDb;
    private final String oraUser;
    private final String oraPwd;

    private final int min_crossid;
    private final int max_crossid;

    private final String targetFolder;

    private boolean getBranch = false;

    public ExportThreadImp(String host, int port, String db,
                           String user, String pwd,
                           String folder, int min, int max,
                           boolean getBranchLinks){
        min_crossid = min;
        max_crossid = max;
        targetFolder = folder;

        oraHost = host;
        oraPort = port;
        oraDb = db;
        oraUser = user;
        oraPwd = pwd;

        getBranch = getBranchLinks;
    }

    @Override
    public Integer call() throws Exception {
        int totalCount = 0;

        File f = new File(targetFolder, String.format("%d.txt", min_crossid));

        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(f));
        BufferedWriter writer = new BufferedWriter(output);

        try(PartCrossExport exportor =
                    new PartCrossExport(oraHost, oraPort, oraDb, oraUser, oraPwd, min_crossid, max_crossid, getBranch)){

            while (exportor.hasNext()) {
                String crossJson = exportor.next();

                writer.write(crossJson);
                writer.newLine();

                totalCount++;

                if((totalCount % 30) == 0)
                    writer.flush();
            }
        } catch (Exception e){
            Logger.getLogger(RDCrossExport.class).error(e.getMessage());
        }

        writer.flush();
        writer.close();
        output.close();

        return totalCount;
    }
}

class PartCrossExport  implements Iterator<String>, AutoCloseable {
    private final String query_crossids_sql =
            "SELECT pid FROM RD_Cross WHERE pid >= %d AND pid < %d";

    private JsonUtil jsonUtil = JsonUtil.getInstance();

    private OracleDatabase oracle = null;
    private OracleStatementContainer container = null;

    private int min_crossid = 0;
    private int max_crossid = 0;

    private Queue<Integer> crossIDs = null;

    private boolean getBranchLinks =false;

    public PartCrossExport(String host,
                           int port,
                           String db,
                           String user,
                           String pwd,
                           int minid,
                           int maxid,
                           boolean getBranch){

        try{
            oracle = (OracleDatabase) DataSource.getDataSource(
                                            IOUtil.makeOracleParams(host,
                                                    port,
                                                    db,
                                                    user,
                                                    pwd));

            if(null != oracle) {
                container = new OracleStatementContainer(oracle);
            }

            min_crossid = minid;
            max_crossid = maxid;

            getBranchLinks = getBranch;
        } catch (Exception e){
            Logger.getLogger(RDCrossExport.class).error(e.getMessage());
        }
    }

    @Override
    public boolean hasNext() {
        if(null == oracle) {
            Logger.getLogger(RDCrossExport.class).error("No oracle connection");
            return false;
        }

        if(null == crossIDs){

            crossIDs = new LinkedList<>();

            try(SqlCursor cursor = oracle.query(String.format(query_crossids_sql,
                    min_crossid,
                    max_crossid))){

                while (cursor.next()){
                    crossIDs.offer(cursor.getInteger(1));
                }

            } catch (SQLException e){
                Logger.getLogger(RDCrossExport.class).error(e.getMessage());
            }

        }

        return !crossIDs.isEmpty();
    }

    @Override
    public String next() {
        Integer targetid = crossIDs.poll();

        if(targetid == null) {
            return null;
        }

        try{
            ExportedCross cross = new ExportedCross(targetid);

            cross.build(container, getBranchLinks);

            return jsonUtil.write2String(cross);
        } catch (Exception e){
            Logger.getLogger(PartCrossExport.class).error(e.getMessage());
        }

        return "";
    }

    @Override
    public void remove() {
    }

    @Override
    public void close() throws Exception {
        if(null != container){
            container.close();
        }

        if(null != oracle){
            oracle.close();
        }
    }
}
