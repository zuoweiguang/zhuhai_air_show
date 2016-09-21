package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKBReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ActualTimeTraffic extends ActualTimeRequest {

    private Logger logger = Logger.getLogger(ActualTimeEvent.class);
    private ExecutorService threadPool = Executors.newFixedThreadPool(20);
    private JSONObject prop = PropertiesUtil.getProperties();
    private Connection pgConn = null;
    private Mongo mongo = null;
    private DB db = null;
    private DBCollection corresCol = null;
    private DBCollection trafficStatusCol = null;
    private SimpleDateFormat sdf = null;

    public ActualTimeTraffic() {
        pgConn = getPostgisConnection();
        mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));
        db = mongo.getDB(prop.getString("mongoDb"));
        corresCol = db.getCollection(prop.getString("corresponding"));
        trafficStatusCol = db.getCollection(prop.getString("trafficColName"));
        sdf = new SimpleDateFormat("yyyyMMddHHmm");
    }

    public void getTraffic() {
        byte[] flowByte = null;
        byte[] eventByte = null;
        JSONObject flowJson = null;
        JSONObject eventJson = null;
        try {
            String url = prop.getString("ActualTimeTrafficUrl");
            String result = sendGet(url);

            Connection pgConn = getPostgisConnection();

            JSONObject resultObj = JSONObject.parseObject(result);
            JSONObject resultValue = resultObj.getJSONObject("result");
            JSONObject city = resultValue.getJSONObject("cities").getJSONObject("city");
            String adcode = city.getString("adcode");
            String updatetime = city.getString("updatetime");
            String version = city.getString("version");
            JSONArray meshList = city.getJSONArray("mesh");
            System.out.println("adcode:" + adcode + ", updatetime:" + updatetime + ", version:" + version);
            System.out.println("rtic http get size:" + meshList.size());

            int meshCount = 0;
            int convertMongoCount = 0;
            for (int i = 0; i < meshList.size(); i++) {
                JSONObject mesh = meshList.getJSONObject(i);
                String code = mesh.getString("code");
                String flow = mesh.getString("flow");
                String event = mesh.getString("event");
                if (null != flow) {
                    int count = this.convertFlow2Mongo(flow);
                    convertMongoCount += count;
                }
//                if (null != event) {
//                    eventJson = this.getEvent(event);
//                    System.out.println("event:" + eventJson.toString());
//                }
                meshCount++;
                System.out.println("convert count:" + meshCount);
            }

            System.out.println("make mesh total:" + meshCount + ", convert mongo total:" + convertMongoCount);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            if (null != pgConn) {
                try {
                    pgConn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (null != mongo) {
                mongo.close();
            }
        }

    }

    public int convertFlow2Mongo(String flow) {
        int convertMongoCount = 0;
        try {
            byte[] meshData = Base64.decodeBase64(flow.getBytes());
            // 计算2次网格号，JamNew中格网号占3个字节
            int i = 0;
            int x1 = (((meshData[i + 2] & 0xF0) << 1) | (meshData[i] & 0xF8) >> 3);
            int y1 = (((meshData[i + 2] & 0xF) << 5) | (meshData[i + 1] & 0xF8) >> 3);
            int x2 = ((meshData[i] & 0x7) & 0xFF);
            int y2 = ((meshData[i + 1] & 0x7) & 0xFF);
            int meshCode = y1 * 10000 + x1 * 100 + y2 * 10 + x2;
            i += 3;

            // RTIC记录数
            int rticCnt = (((meshData[i] & 0xFF) << 8) | ((meshData[i + 1] & 0xFF)));
            i += 2;

            List<JSONObject> trafficList = new ArrayList<>();
            // 路况信息
            for (int j = 0; j < rticCnt; j++) {

                // 路链分类     0高速；1快速；2一般；3其他
                int rticKind = ((meshData[i] & 0xF0) >> 4) + 1;

                // 路链序号 路链序号取值范围为1～4095
                int rticId = (meshData[i] & 0x0F) << 8 | (meshData[i + 1] & 0xFF);
                i += 2;

                // 路链旅行时间   当前路链的旅行时间，单位是秒，取值范围0-8190，当值为8191时，表示“不明”。
                int travelTime = (meshData[i] & 0xFF) << 8 | (meshData[i + 1] & 0xFF);
                i += 2;

                // 拥堵路段数    当前路链包含的路段的个数。
                int sectionCnt = meshData[i] & 0xFF;
                i += 1;

                // 拥堵程度 0不明；1通畅；2缓慢；3拥堵
                int iLOS = (meshData[i] & 0x18) >> 3;

                String rticIdStr = String.format("%05d", rticId);
                String rtic_id = meshCode + rticIdStr;
                DBCursor cursor = corresCol.find(new BasicDBObject("rtic_id", rtic_id));
                List<Future> futures = new ArrayList<>();
                while (cursor.hasNext()) {
                    List<Integer> link_pid_list = (List) cursor.next().get("link_pid_list");
                    if (link_pid_list.size() == 0) {
                        continue;
                    }
                    Future future = threadPool.submit(new Convert2mongo(this.pgConn, link_pid_list, this.trafficStatusCol, sdf,
                                                                        meshCode, rticIdStr, iLOS, travelTime));
                    futures.add(future);
//                    callables.add(new Convert2mongo(this.pgConn, link_pid_list, this.trafficStatusCol, sdf,
//                            meshCode, rticIdStr, iLOS, travelTime));
                    convertMongoCount++;
                }
                for (Future future: futures) {
                    future.get();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return convertMongoCount;
        }

    }

    public JSONObject getEvent(String event) {
        JSONObject eventJson = new JSONObject();
        try {
            byte[] meshData = Base64.decodeBase64(event.getBytes());

            int i = 0;
            // 计算2次网格号，JamNew中格网号占3个字节
            int x1 = (((meshData[i + 2] & 0xF0) << 1) | (meshData[i] & 0xF8) >> 3);
            int y1 = (((meshData[i + 2] & 0xF) << 5) | (meshData[i + 1] & 0xF8) >> 3);
            int x2 = ((meshData[i] & 0x7) & 0xFF);
            int y2 = ((meshData[i + 1] & 0x7) & 0xFF);
            int meshCode = y1 * 10000 + x1 * 100 + y2 * 10 + x2;
            eventJson.put("meshCode", meshCode);

            i += 3;

            // RTIC记录数
            int rticCnt = (((meshData[i] & 0xFF) << 8) | ((meshData[i + 1] & 0xFF)));
            i += 2;
            eventJson.put("rticCnt", rticCnt);

            // 路况信息
            for (int j = 0; j < rticCnt; j++) {

                // 路链分类
                int rticKind = ((meshData[i] & 0x30) >> 4) + 1;
                eventJson.put("rticKind", rticKind);

                // 路链序号
                int rticId = (meshData[i] & 0x0F) << 8 | (meshData[i + 1] & 0xFF);
                eventJson.put("rticId", rticId);
                i += 2;

                // 路链旅行时间
                int travelTime = (meshData[i] & 0xFF) << 8 | (meshData[i + 1] & 0xFF);
                eventJson.put("travelTime", travelTime);
                i += 2;

                // 拥堵路段数
                int sectionCnt = meshData[i] & 0xFF;
                eventJson.put("sectionCnt", sectionCnt);
                i += 1;

                // 拥堵程度
                int iLOS = (meshData[i] & 0x18) >> 3;
                eventJson.put("status", iLOS);

            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return eventJson;
        }

    }


    public static void main(String[] args) {
        new ActualTimeTraffic().getTraffic();
    }

}


class Convert2mongo extends Thread {

    private Connection pgConn = null;
    private List<Integer> link_pid_list = null;
    private WKBReader wkbReader = null;
    private DBCollection trafficStatusCol = null;
    private int meshCode;
    private String rticIdStr;
    private int iLOS;
    private int travelTime;
    private SimpleDateFormat sdf = null;

    public Convert2mongo(Connection pgConn, List<Integer> link_pid_list, DBCollection trafficStatusCol, SimpleDateFormat sdf,
                            int meshCode, String rticIdStr, int iLOS, int travelTime) {
        this.link_pid_list = link_pid_list;
        this.pgConn = pgConn;
        this.wkbReader = new WKBReader();
        this.trafficStatusCol = trafficStatusCol;
        this.sdf = sdf;
        this.meshCode = meshCode;
        this.rticIdStr = rticIdStr;
        this.iLOS = iLOS;
        this.travelTime = travelTime;
    }

    @Override
    public void run() {
        try {
            //查询 pg 对应的道路
            StringBuffer sb = new StringBuffer();
            for (Integer link_pid : this.link_pid_list) {
                BigInteger b = new BigInteger(String.valueOf(link_pid));
                sb.append(b.abs() + ",");
            }
            String pidStrList = sb.substring(0, sb.length() - 1);
            String sql = "SELECT pid, direct, functionclass, st_asewkb(geom) as geom FROM road WHERE pid in (" + pidStrList.toString() + ")";
            Statement stmt = pgConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                try {
                    DBObject trafficObj = new BasicDBObject();
                    int link_id = rs.getInt("pid");
                    trafficObj.put("link_id", link_id);
                    trafficObj.put("direct", rs.getInt("direct"));
                    trafficObj.put("functionclass", rs.getInt("functionclass"));
                    trafficObj.put("status", iLOS);
                    trafficObj.put("rtic_id", meshCode + rticIdStr);
                    trafficObj.put("travel_time", travelTime);
                    trafficObj.put("update_time", sdf.format(new Date()));
                    byte[] wkb = rs.getBytes("geom");
                    LineString lineString = (LineString) wkbReader.read(wkb);
                    trafficObj.put("geometry", lineString.toString());
                    Envelope envelope = lineString.getEnvelope().getEnvelopeInternal();
                    //生成tile
                    Coordinate lt = new Coordinate(envelope.getMinX(), envelope.getMaxY());
                    Coordinate rb = new Coordinate(envelope.getMaxX(), envelope.getMinY());
                    //tile级别
                    for (int k = 10; k <= 17; k++) {
                        trafficObj.put("tile_" + k, MercatorUtil.bounds2Tiles(lt, rb, k));
                    }
                    DBObject query = new BasicDBObject("link_id", link_id);
                    DBObject update = new BasicDBObject("$set", trafficObj);
                    this.trafficStatusCol.update(query, update, true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}