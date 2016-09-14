package com.navinfo.mapspotter.warehouse.zhuhai.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/9/7.
 */
public class ViewDao {

    private static ViewDao instance;
    private Mongo mongo;
    private DB db;
    private DBCollection col;

    private ViewDao() {
    }

    public static synchronized ViewDao getInstance() {
        if (instance == null) {
            instance = new ViewDao();
        }
        return instance;
    }

    public DBCollection getCollection(String collectionName) {
        JSONObject prop = PropertiesUtil.getProperties();
        mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));
        db = mongo.getDB(prop.getString("mongoDb"));
        col = db.getCollection(collectionName);
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


}
