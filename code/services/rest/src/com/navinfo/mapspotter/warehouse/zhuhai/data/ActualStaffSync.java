package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ActualStaffSync extends ActualTimeRequest {
    private Logger logger = Logger.getLogger(ActualStaffSync.class);
    private static JSONObject propObj = PropertiesUtil.getProperties();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public void getStaff() {

        String url = propObj.getString("staffSyncUrl");
        String param = propObj.getString("staffSyncCode");
        String result = sendPost(url, param);

//        System.out.println(result);

        String tile;
        try {
            List<DBObject> bulk = new ArrayList<>();
            DBCollection col = getMongoCollection("userColName");
            DBObject staffJson = BasicDBObject.parse(result);
            List<DBObject> staffList = (List) staffJson.get("content");

            for (DBObject staff: staffList) {
                try {

                    DBObject user = new BasicDBObject();
                    String mobile_phone  = (String) staff.get("iphone");
                    user.put("mobile_phone", mobile_phone);
                    user.put("user_name", staff.get("nickname"));
                    Double lon = Double.valueOf((String) staff.get("lng"));
                    Double lat = Double.valueOf((String) staff.get("lat"));
                    List<Double> location = new ArrayList<>();
                    location.add(lon);
                    location.add(lat);
                    user.put("location", location);
                    if (null != staff.get("time")) {
                        Long time = Long.valueOf(staff.get("time") + "000");
                        sdf.format(new Date(time));
                    }
                    user.put("update_time", null);

                    //生成tile
                    Coordinate coord = new Coordinate(lon, lat);
                    for (int i = 10; i <= 17; i++) {
                        tile = MercatorUtil.lonLat2MCode(coord, i);
                        user.put("tile_" + i, tile);
                    }
//                    System.out.println(user.toString());
                    DBObject query = new BasicDBObject("mobile_phone", mobile_phone);
                    DBObject update = new BasicDBObject("$set", user);
                    col.update(query, update, true, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //创建索引
            for (int i = 10; i <= 17; i++) {
                col.createIndex("mobile_phone" + i);
            }

            System.out.println("update staff total:" + staffList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeMongo();
        }

    }


    public static void main(String[] args) {
        new ActualStaffSync().getStaff();
    }

}
