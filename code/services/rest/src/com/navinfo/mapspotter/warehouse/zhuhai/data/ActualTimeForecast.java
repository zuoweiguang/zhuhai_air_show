package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zuoweiguang on 2016/9/13.
 */
public class ActualTimeForecast extends ActualTimeRequest {

    private Logger logger = Logger.getLogger(ActualTimeEvent.class);
    private static JSONObject propObj = PropertiesUtil.getProperties();

    public void getForecast(String zoneTime) {

        String tile = null;

        try {
            String url = propObj.getString("ActualTimeForecastUrl");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            String dateStr = sdf.format(new Date());
            String params = "&timestamp=" + dateStr;
            String result = sendGet(url + params + zoneTime);
            String[] splitText = result.split("#");
            String[] valueText = splitText[1].split("\\|");
            System.out.println("current data total:" + valueText.length);

            List<DBObject> bulk = new ArrayList<>();
            DBCollection col = null;
            if (zoneTime.equals("&zone=30")) {
                col = getMongoCollection("forecast_halfhour");
            } else if (zoneTime.equals("&zone=60")) {
                col = getMongoCollection("forecast_onehour");
            }
            Connection pgConn = getPostgisConnection();

            //获取link对应的geometry
            Map<Integer, byte[]> linkGeom = new HashMap<>();
            StringBuffer sb = new StringBuffer();
            for (String line: valueText) {
                String[] valueList = line.split(",");
                sb.append(valueList[2] + ",");
            }
            String pidStrList = sb.substring(0, sb.length() - 1);
            String sql = "SELECT pid, st_asewkb(geom) as geom FROM road WHERE pid in ("+pidStrList.toString()+")";
            Statement stmt = pgConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int pid = rs.getInt("pid");
                byte[] wkb = rs.getBytes("geom");
                linkGeom.put(pid, wkb);
            }
            Set linkPidList = linkGeom.keySet();
            int count = 0;
            for (String line: valueText) {
//                System.out.println(line);
                try {
                    DBObject forecast = new BasicDBObject();
                    String[] valueList = line.split(",");
                    forecast.put("direct", Integer.valueOf(valueList[0]));
                    forecast.put("region_id", Integer.valueOf(valueList[1]));
                    int link_id = Integer.valueOf(valueList[2]);
                    forecast.put("link_id", link_id);
                    forecast.put("road_length", Integer.valueOf(valueList[3]));
                    String road_class = valueList[4].subSequence(valueList[4].length() - 1, valueList[4].length()).toString();
                    forecast.put("road_class", Integer.valueOf(road_class));
                    String link_type = valueList[5].substring(valueList[5].length() - 1, valueList[5].length()).toString();
                    forecast.put("link_type", Integer.valueOf(link_type));
                    forecast.put("status", Integer.valueOf(valueList[6]));
                    forecast.put("travel_time", Integer.valueOf(valueList[7]));

                    if (linkPidList.contains(link_id)) {
                        byte[] wkb = linkGeom.get(link_id);
                        LineString lineString = (LineString) wkbReader.read(wkb);
                        Envelope envelope = lineString.getEnvelope().getEnvelopeInternal();
                        //生成tile
                        Coordinate lt = new Coordinate(envelope.getMinX(), envelope.getMaxY());
                        Coordinate rb = new Coordinate(envelope.getMaxX(), envelope.getMinY());
                        //tile级别
                        for (int i = 10; i <= 17; i++) {
                            forecast.put("tile_" + i, MercatorUtil.bounds2Tiles(lt, rb, i));
                        }
                        forecast.put("geometry", lineString.toString());

                    }

                    DBObject query = new BasicDBObject("link_id", link_id);
                    DBObject update = new BasicDBObject("$set", forecast);
                    col.update(query, update, true, false);
                    count ++;

                    if (count % 2000 == 0 && zoneTime.equals("&zone=30")) {
                        System.out.println("forecast halfhour make count:" + count);
                    }
                    else if (count % 2000 == 0 && zoneTime.equals("&zone=60")) {
                        System.out.println("forecast onehour make count:" + count);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //创建索引
            for (int i = 10; i <= 17; i++) {
                col.createIndex("tile_" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeMongo();
            closePostgisConnection();
        }

    }

    public static void main(String[] args) {

        new ActualTimeForecast().getForecast("&zone=30");

//        try {
//            ActualTimeForecast af = new ActualTimeForecast();
//            LineString line = (LineString) af.wktReader.read("LINESTRING (113.51859 22.26507, 113.51865 22.26496)");
//            System.out.println(line.toString());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }


    }

}
