package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by zuoweiguang on 2016/9/13.
 */
public abstract class ActualTimeRequest {

    private static JSONObject propObj = PropertiesUtil.getProperties();
    private static GeometryFactory geometryFactory = new GeometryFactory();
    public WKBReader wkbReader = new WKBReader();
    public WKTReader wktReader = new WKTReader();
    public Mongo mongo = null;
    private static DB db = null;
    private static DBCollection col = null;
    private Connection pgConn;

    public String sendGet(String url) {

        String result = "";
        BufferedReader in = null;
        URL realUrl = null;
        try {
            String urlNameString = url;
            realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
//        System.out.println(result);
        return result;
    }

    public DBCollection getMongoCollection(String type) {
        mongo = new MongoClient(propObj.getString("mongoHost"), propObj.getInteger("mongoPort"));
        db = mongo.getDB(propObj.getString("mongoDb"));
        if (type.equals("events")) {
            return db.getCollection(propObj.getString("eventColName"));
        }
        else if (type.equals("forecast_halfhour")) {
            return db.getCollection(propObj.getString("halfhour_forecastColName"));
        }
        else if (type.equals("forecast_onehour")) {
            return db.getCollection(propObj.getString("onehour_forecastColName"));
        }
        else if (type.equals("traffic")) {
            return db.getCollection(propObj.getString("trafficColName"));
        }
        else {
            return null;
        }
    }

    public void closeMongo() {
        if (null != mongo) {
            mongo.close();
            System.out.println("close MongoDB connection!!");
        }
    }

    public Connection getPostgisConnection() {
        String pgHost = propObj.getString("pgHost");
        int pgPort = propObj.getInteger("pgPort");
        String pgDb = propObj.getString("pgDb");
        String pgUser = propObj.getString("pgUser");
        String pgPwd = propObj.getString("pgPwd");
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://"+pgHost+":"+pgPort+"/"+pgDb+"";
            pgConn = DriverManager.getConnection(url, pgUser, pgPwd);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return pgConn;
        }

    }

    public void closePostgisConnection() {
        if (null != pgConn) {
            try {
                pgConn.close();
                System.out.println("close PostGis connection!!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
