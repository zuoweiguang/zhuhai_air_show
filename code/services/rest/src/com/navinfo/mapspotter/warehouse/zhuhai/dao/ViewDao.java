package com.navinfo.mapspotter.warehouse.zhuhai.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/9/7.
 */
public class ViewDao {

    private static ViewDao instance;
    private static JSONObject prop = PropertiesUtil.getProperties();
    private static Mongo mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));

    private ViewDao() {
    }

    public static synchronized ViewDao getInstance() {
        if (instance == null) {
            instance = new ViewDao();
        }
        return instance;
    }

    public DBCollection getCollection(String collectionName) {
        DB db = mongo.getDB(prop.getString("mongoDb"));
        DBCollection col = db.getCollection(collectionName);
        return col;
    }

    public List<DBObject> getTrafficEvent(DBCollection col, int z, int x, int y) {
        List<DBObject> evenList = new ArrayList<>();
        try {
            String key = "tile_" + z;
            String value = x + "_" + y;
            DBObject query = new BasicDBObject(key, value);
            DBCursor cursor = col.find(query);
            while (cursor.hasNext()) {
                DBObject event = cursor.next();
                evenList.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return evenList;
        }
    }

    public List<DBObject> getForecasts(DBCollection col, int z, int x, int y) {
        List<DBObject> forecastList = new ArrayList<>();
        try {
            String key = "tile_" + z;
            String value = x + "_" + y;
            DBObject query = new BasicDBObject(key, value);
            DBCursor cursor = col.find(query);
            while (cursor.hasNext()) {
                DBObject event = cursor.next();
                forecastList.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return forecastList;
        }
    }

    public List<DBObject> getTraffic(DBCollection col, int z, int x, int y) {
        List<DBObject> trafficList = new ArrayList<>();
        try {
            String key = "tile_" + z;
            String value = x + "_" + y;
            DBObject query = new BasicDBObject(key, value);
            DBCursor cursor = col.find(query);
            while (cursor.hasNext()) {
                DBObject event = cursor.next();
                trafficList.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return trafficList;
        }
    }

    public List<DBObject> getStaff(DBCollection col) {
        List<DBObject> staffList = new ArrayList<>();
        try {
            DBCursor cursor = col.find();
            while (cursor.hasNext()) {
                DBObject event = cursor.next();
                staffList.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return staffList;
        }
    }

    public int checkUserExists(DBCollection col, String mobile_phone) {
        int exists = 0;
        try {
            DBObject check = new BasicDBObject("mobile_phone", mobile_phone);
            DBCursor cursor = col.find(check);
            if (cursor.hasNext()) {
                exists = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            exists = -1;
        }
        return exists;
    }

    public int addStaff(DBCollection col, String mobile_phone, String password, String confirm_password, String user_name,
                           int user_type, String id_card, int sex, int age, String address) {
        try {
            DBObject user = new BasicDBObject();
            user.put("mobile_phone", mobile_phone);
            user.put("password", password);
            user.put("user_name", user_name);
            if (user_type == 1) {
                user.put("user_type", "协管员");
            }
            else if (user_type == 2) {
                user.put("user_type", "志愿者");
            }
            else if (user_type == 3) {
                user.put("user_type", "民警");
            }
            user.put("id_card", id_card);
            user.put("sex", sex);
            user.put("age", age);
            user.put("address", address);
            user.put("location", null);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            user.put("register_time", format.format(new Date()));
            user.put("update_time", null);
            col.insert(user);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int staffUploadLocation(DBCollection col, String mobile_phone, double lon, double lat) {
        int result = 0;
        try {
            DBObject query = new BasicDBObject("mobile_phone", mobile_phone);
            List<Double> location = new ArrayList<>();
            location.add(lon);
            location.add(lat);
            DBObject updateObj = new BasicDBObject("location", location);
            col.update(query, updateObj, false, false);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        }
        return result;
    }

    public List<DBObject> getParking(DBCollection col) {
        List<DBObject> parkList = new ArrayList<>();
        try {
            DBCursor cursor = col.find();
            while (cursor.hasNext()) {
                DBObject event = cursor.next();
                parkList.add(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return parkList;
        }
    }


}
