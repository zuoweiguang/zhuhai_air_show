package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ActualTimeEvent extends ActualTimeRequest {
    private Logger logger = Logger.getLogger(ActualTimeEvent.class);
    private static JSONObject propObj = PropertiesUtil.getProperties();

    public void getEvent() {

        String url = propObj.getString("actualTimeEventUrl");
        String result = sendGet(url);
        String[] splitList = result.split("null");

        String tile;

        try {
            List<DBObject> bulk = new ArrayList<>();
            DBCollection col = getMongoCollection("events");

            for (String line: splitList) {
                DBObject event = new BasicDBObject();
                String[] valueList = line.split(",");
                event.put("SEQ", valueList[0]);
                event.put("MapVersion", valueList[1]);
                event.put("RoadName", valueList[2]);
                event.put("Kind", valueList[3]);
                event.put("EventReasonType", valueList[4] + "," + valueList[5]);
                event.put("EventReason", valueList[6] + "," + valueList[7]);
                event.put("EventPublishTime", valueList[8]);
                event.put("StartTime", valueList[9]);
                event.put("ExpireTime", valueList[10]);
                event.put("EventDescription", valueList[11]);
                event.put("LinkIDs", valueList[12]);
                List<Double> coordinate = new ArrayList<>();
                double lon = Double.valueOf(valueList[13]);
                double lat = Double.valueOf(valueList[14]);
                coordinate.add(lon);
                coordinate.add(lat);
                event.put("LinkCoordinate", coordinate);
//                event.put("PublishState", valueList[15]);
//                event.put("EventPublishUser", valueList[16]);
//                event.put("TrafficEyeType", valueList[17]);
//                event.put("Uid", valueList[18]);
//                event.put("VerifyType", valueList[19]);

                //生成tile
                Coordinate coord = new Coordinate(lon, lat);
                for (int i = 10; i <= 17; i++) {
                    tile = MercatorUtil.lonLat2MCode(coord, i);
                    event.put("tile_" + i, tile);
                }
//                System.out.println(event.toString());
                bulk.add(event);
                DBObject query = new BasicDBObject("SEQ", valueList[0]);
                DBObject update = new BasicDBObject("$set", event);
                col.update(query, update, true, false);
            }

            //创建索引
            for (int i = 10; i <= 17; i++) {
                col.createIndex("tile_" + i);
            }

//        System.out.println(bulk);
//        col.insert(bulk);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeMongo();
        }

    }


    public static void main(String[] args) {
        new ActualTimeEvent().getEvent();
    }

}
