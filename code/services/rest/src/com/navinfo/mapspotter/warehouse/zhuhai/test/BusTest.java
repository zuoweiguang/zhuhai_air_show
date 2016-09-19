package com.navinfo.mapspotter.warehouse.zhuhai.test;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zuoweiguang on 2016/9/19.
 */
public class BusTest {

    public void insertBus() {
        JSONObject prop = PropertiesUtil.getProperties();
        Mongo mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));

        try {
            DB db = mongo.getDB(prop.getString("mongoDb"));
            DBCollection col = db.getCollection(prop.getString("busColName"));

            DBObject insertObj = new BasicDBObject();
            insertObj.put("bus_type", 1);                 //车辆类型（1航展班车；2大巴）
            insertObj.put("card_num", "珠A·K1234");      //车牌号
            insertObj.put("start_location", 1);           //起始位置
            insertObj.put("end_location", 1);             //终点位置
            insertObj.put("drive_line", 1);               //形式路线
            insertObj.put("current_location", 1);         //当前位置
            insertObj.put("busload", 40);                  //最大载客量
            insertObj.put("current_load", 36);             //当前载客量
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            insertObj.put("update_time", format.format(new Date()));              //更新时间

            col.insert(insertObj);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mongo.close();
        }
    }

    public static void main(String[] args) {

    }
}
