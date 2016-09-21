package com.navinfo.mapspotter.warehouse.zhuhai.test;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.WKTReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/9/19.
 */
public class BusTest {

    public void insertBus() {
        WKTReader wktReader = new WKTReader();
        JSONObject prop = PropertiesUtil.getProperties();
        Mongo mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));

        try {
            DB db = mongo.getDB(prop.getString("mongoDb"));
            DBCollection col = db.getCollection(prop.getString("busColName"));

            String test_data = "{ \n" +
                    "    \"bus_type\" : 1, \n" +
                    "    \"card_num\" : \"珠AK1234\", \n" +
                    "    \"start_location\" : [\n" +
                    "        113.380487, \n" +
                    "        22.015831\n" +
                    "    ], \n" +
                    "    \"end_location\" : [\n" +
                    "        113.344842, \n" +
                    "        22.042165\n" +
                    "    ], \n" +
                    "    \"drive_line\" : \"LINESTRING (113.380487 22.015831,113.379768 22.015294,113.376966 22.013083,113.366905 22.00551,113.362952 22.005443,113.359215 22.007118,113.355981 22.01248,113.34477 22.025279,113.343405 22.032315,113.344267 22.042366,113.344842 22.042165)\", \n" +
                    "    \"current_location\" : [\n" +
                    "        113.356628, \n" +
                    "        22.011072\n" +
                    "    ], \n" +
                    "    \"busload\" : 40, \n" +
                    "    \"current_load\" : 36, \n" +
                    "    \"update_time\" : \"20160920022836\"\n" +
                    "}";
            DBObject insertObj = BasicDBObject.parse(test_data);
            String drive_line = "LINESTRING (113.380487 22.015831,113.379768 22.015294,113.376966 22.013083,113.366905 22.00551,113.362952 22.005443,113.359215 22.007118,113.355981 22.01248,113.34477 22.025279,113.343405 22.032315,113.344267 22.042366,113.344842 22.042165)";

            LineString lineString = (LineString) wktReader.read(drive_line);
            Envelope envelope = lineString.getEnvelope().getEnvelopeInternal();
            //生成tile
            Coordinate lt = new Coordinate(envelope.getMinX(), envelope.getMaxY());
            Coordinate rb = new Coordinate(envelope.getMaxX(), envelope.getMinY());
            //tile级别
            for (int i = 10; i <= 17; i++) {
                insertObj.put("tile_" + i, MercatorUtil.bounds2Tiles(lt, rb, i));
            }

            System.out.println(insertObj.toString());
            col.insert(insertObj);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongo.close();
        }
    }

    public static void main(String[] args) {

        //{ "bus_type" : 1 , "card_num" : "珠AK4321" , "start_location" : [ 113.322923 , 22.032181] , "end_location" : [ 113.349441 , 22.037676] , "drive_line" : "LINESTRING (113.322923 22.032181,113.325726 22.032181,113.329319 22.034995,113.338159 22.041629,113.352675 22.047458,113.354544 22.045515,113.349729 22.040222,113.349441 22.037676)" , "current_location" : [ 113.337224 , 22.041026] , "busload" : 40 , "current_load" : 30 , "update_time" : "20160920023607"}

        BusTest bt = new BusTest();
        bt.insertBus();
    }
}
