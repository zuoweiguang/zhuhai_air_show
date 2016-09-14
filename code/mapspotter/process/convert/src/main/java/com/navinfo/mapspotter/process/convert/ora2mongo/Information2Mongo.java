package com.navinfo.mapspotter.process.convert.ora2mongo;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.bson.Document;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by zuoweiguang on 2016/6/24.
 */
public class Information2Mongo {

    private String oraHost = null;
    private int oraPort = 0;
    private String oraDb = null;
    private String oraUser = null;
    private String oraPwd = null;

    private final DataSourceParams mongoParams;
    private String mongoHost = null;
    private int mongoPort = 0;
    private String mongoDb = null;
    private String mongoColName = null;
    private String table1 = "information_main";
    private String table2 = "information_extension";
    private String table3 = "information_deep";

    public Information2Mongo(String oraHost, int oraPort, String oraDb, String oraUser, String oraPwd,
                             String mongoHost, int mongoPort, String mongoDb) {

        this.oraHost = oraHost;
        this.oraPort = oraPort;
        this.oraDb = oraDb;
        this.oraUser = oraUser;
        this.oraPwd = oraPwd;

        mongoParams = IOUtil.makeMongoDBParams(mongoHost, mongoPort, mongoDb);
        this.mongoHost = mongoHost;
        this.mongoPort = mongoPort;
        this.mongoDb = mongoDb;
    }


    public void execute() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        MercatorUtil mu = new MercatorUtil();

        int total = 0;
        String url = "jdbc:oracle:thin:@" + this.oraHost + ":" + this.oraPort + ":" + this.oraDb;
        MongoClient client = null;

        try {
            System.out.println("Extract Oracle Information to Mongo......");
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, this.oraUser, this.oraPwd);

            client = new MongoClient(new ServerAddress(mongoParams.getHost(), mongoParams.getPort()));
            MongoDatabase db = client.getDatabase(mongoParams.getDb());

            //创建表
            for (String colName: db.listCollectionNames()) {
                if (colName.equals(this.table1)
                        || colName.equals(this.table2)
                        || colName.equals(this.table3)) {
                    db.getCollection(colName).drop();
                }
            }
            db.createCollection(this.table1);
            db.createCollection(this.table2);
            db.createCollection(this.table3);

            MongoCollection<Document> mainCollection = db.getCollection(this.table1);
            MongoCollection<Document> extensionCollection = db.getCollection(this.table2);
            MongoCollection<Document> deepCollection = db.getCollection(this.table3);
            System.out.println("create information collection success!!");

            List<WriteModel<Document>> mainList = new ArrayList<>();
            List<WriteModel<Document>> extensionList = new ArrayList<>();
            List<WriteModel<Document>> deepList = new ArrayList<>();

            //获取当前表的所有字段
            List<String> columeList = new ArrayList<>();
            String columeSql = "SELECT COLUMN_NAME FROM USER_TAB_COLUMNS  WHERE TABLE_NAME='TEMP_TO_FM'";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(columeSql);
            while (resultSet.next()) {
                columeList.add(resultSet.getString(1));
            }
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            String oraSql = "SELECT COUNT(1) FROM TEMP_TO_FM";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            String sql = "SELECT * FROM TEMP_TO_FM";
            stmt = conn.createStatement();
//            stmt.setQueryTimeout(60 * 60 * 4);
            resultSet = stmt.executeQuery(sql);
            resultSet.setFetchSize(2000);

            int counter = 0;
            int commit_counter = 0;
            JSONObject poiObj = null;
            Coordinate coord = null;
            String tile = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (resultSet.next()) {
                try {
                    int infoTypeCode = resultSet.getInt("INFO_TYPE_CODE");
                    //-------------------------------- main ----------------------------------
                    Document main = new Document();
                    String uuid = UUID.randomUUID().toString();
                    uuid = uuid.replace("-", "");
                    main.put("globalId", uuid);
                    String infoTypeCodeStr = String.valueOf(infoTypeCode);
                    //INFO_TYPE_CODE以1开头时转出1 POI；2或6开头时转出2 道路；
                    if (infoTypeCodeStr.startsWith("1")) {
                        main.put("b_featureKind", 1);
                    } else if (infoTypeCodeStr.startsWith("2") || infoTypeCodeStr.startsWith("6")) {
                        main.put("b_featureKind", 2);
                    }
                    main.put("b_sourceCode", 1);
                    main.put("b_sourceId", resultSet.getString("INFO_INTEL_ID"));
                    main.put("b_reliability", 1);
                    String guidelon = resultSet.getString("GUIDELON");
                    String guidelat = resultSet.getString("GUIDELAT");
                    if (null != guidelon && null != guidelat) {
                        Document geometry = new Document();
                        geometry.put("type", "Point");
                        List<Double> c = new ArrayList<>();
                        c.add(Double.valueOf(guidelon));
                        c.add(Double.valueOf(guidelat));
                        geometry.put("coordinates", c);
                        main.put("g_guide", geometry);
                        main.put("loc", geometry);
                    } else {
                        main.put("g_guide", "");
                    }
                    String TRUST_LEVEL = resultSet.getString("TRUST_LEVEL");
                    if (null != TRUST_LEVEL && TRUST_LEVEL.equals("准确")) {
                        main.put("t_expDateReliab", 1);
                    }
                    else if (null != TRUST_LEVEL && TRUST_LEVEL.equals("不准确")) {
                        main.put("t_expDateReliab", 2);
                    }
                    else {
                        main.put("t_expDateReliab", 0);
                    }
                    main.put("t_storageDate", String.valueOf(System.currentTimeMillis()));
                    main.put("t_expectDate", "");
                    String PLAN_USE_TIME = resultSet.getString("PLAN_USE_TIME");
                    if (null != PLAN_USE_TIME) {
                        Date d = sdf.parse(PLAN_USE_TIME);
                        long l = d.getTime();
                        String planUseTime = String.valueOf(l);
//                        planUseTime = planUseTime.substring(0, 10);
                        main.put("t_expectDate", planUseTime);
                    }
                    main.put("t_status", 0);
                    String PROJECT_TYPE = resultSet.getString("PROJECT_TYPE");
                    if (null != PROJECT_TYPE && PROJECT_TYPE.equals("新增")) {
                        main.put("i_proposal", 3);
                    }
                    else if (null != PROJECT_TYPE && PROJECT_TYPE.equals("删除")) {
                        main.put("i_proposal", 1);
                    }
                    else {
                        main.put("i_proposal", 2);
                    }

                    String USE_LEVEL = resultSet.getString("USE_LEVEL");
                    if (null != USE_LEVEL && USE_LEVEL.equals("Ⅰ")) {
                        main.put("i_level", 1);
                    }
                    else if (null != USE_LEVEL && USE_LEVEL.equals("Ⅱ")) {
                        main.put("i_level", 2);
                    }
                    else if (null != USE_LEVEL && USE_LEVEL.equals("Ⅲ")) {
                        main.put("i_level", 3);
                    }
                    main.put("i_infoName", resultSet.getString("INFO_NAME"));
                    main.put("i_infoContent", resultSet.getString("INFO_CONTENT"));

                    String wkt = resultSet.getString("MAP_CONFIG");
                    Geometry geom = GeoUtil.wkt2Geometry(wkt);
                    if (wkt.contains("POINT")) {
                        Coordinate point = geom.getCoordinate();
                        Document geometry = new Document();
                        geometry.put("type", "Point");
                        List<Double> c = new ArrayList<>();
                        c.add(point.x);
                        c.add(point.y);
                        geometry.put("coordinates", c);
                        main.put("g_location", geometry);
                        main.put("loc", geometry);
                        //tile级别
                        for (int i = 14; i <= 17; i++) {
                            tile = MercatorUtil.lonLat2MCode(point, i);
                            main.put("tile_" + i, tile);
                        }
                    }
                    else if (wkt.contains("LINESTRING")) {
                        Coordinate[] coords = geom.getCoordinates();
                        Document geometry = new Document();
                        geometry.put("type", "LineString");
                        List<List> coordinates = new ArrayList<>();
                        for (Coordinate point: coords) {
                            List<Double> c = new ArrayList<>();
                            c.add(point.x);
                            c.add(point.y);
                            coordinates.add(c);
                        }
                        geometry.put("coordinates", coordinates);
                        main.put("g_location", geometry);
                        main.put("loc", geometry);
                        Envelope envelope = new GeometryFactory().createLineString(coords).getEnvelopeInternal();
                        //tile级别
                        for (int i = 14; i <= 17; i++) {
                            List<String> mcodes = mu.bound2MCode(envelope, i);
                            main.put("tile_" + i, mcodes);
                        }
                    }

                    mainList.add(new InsertOneModel<>(main));

                    //-------------------------------- extension ----------------------------------
                    Document extension = new Document();
                    extension.put("globalId", uuid);
                    extension.put("t_publishDate", "");
                    extension.put("t_submitDate", "");
                    extension.put("t_closeDate", "");
                    extension.put("t_operateDate", "");
                    extension.put("t_payPoints", 0);
                    extension.put("h_outdoor", 0);
                    extension.put("h_indoor", 0);
                    extension.put("h_audit", 0);

                    Document i_poi = new Document();
                    i_poi.put("kindCode", resultSet.getString("KIND_CODE"));
                    i_poi.put("name", resultSet.getString("INFO_NAME"));
                    i_poi.put("address", resultSet.getString("ADDRESS"));
                    i_poi.put("telephone", resultSet.getString("PHONE"));
                    i_poi.put("telephone", resultSet.getString("PHONE"));
                    i_poi.put("brandCode", "");
                    if (columeList.contains("CHAIN")) {
                        i_poi.put("brandCode", resultSet.getString("CHAIN"));
                    }
                    i_poi.put("foodtype", "");
                    if (columeList.contains("FOOD_TYPE")) {
                        i_poi.put("foodtype", resultSet.getString("FOOD_TYPE"));
                    }
                    i_poi.put("father", "");
                    if (columeList.contains("FARTHERSON")) {
                        i_poi.put("father", resultSet.getString("FARTHERSON"));
                    }
                    i_poi.put("rating", "");
                    if (columeList.contains("RATING")) {
                        i_poi.put("rating", resultSet.getString("RATING"));
                    }
                    extension.put("i_poi", i_poi);

                    Document i_road = new Document();
                    i_road.put("roadKind", 0);
                    i_road.put("length", resultSet.getString("LENGTH"));
                    i_road.put("startPoint", resultSet.getString("START_POINT"));
                    i_road.put("passPoint", resultSet.getString("PASS_POINT"));
                    i_road.put("endPoint", resultSet.getString("END_POINT"));
                    extension.put("i_road", i_road);

                    extension.put("i_memo", resultSet.getString("INFO_TITLE"));

                    extension.put("i_confirmMode", "");
                    if (columeList.contains("CONFIRM_MODE")) {
                        extension.put("i_confirmMode", resultSet.getString("CONFIRM_MODE"));
                    }
                    extension.put("i_confirmResult", "");
                    if (columeList.contains("CONFIRM_RESULT")) {
                        extension.put("i_confirmMode", resultSet.getString("CONFIRM_RESULT"));
                    }
                    extension.put("i_precision", resultSet.getString("PRECISION"));
                    extension.put("i_serviceStatus", resultSet.getString("SERVICE_STATUS"));

                    Document r_features = new Document();
                    r_features.put("featureKind", 1);
                    r_features.put("pid", 0);
                    r_features.put("fid", "");
                    if (columeList.contains("FID")) {
                        r_features.put("fid", resultSet.getString("FID"));
                    }
                    r_features.put("similarity", "");
                    extension.put("r_features", r_features);

                    Document f_array = new Document();
                    f_array.put("user_id", 0);
                    f_array.put("type", 0);
                    f_array.put("content", "");
                    f_array.put("auditRemark", "");
                    f_array.put("date", "");
                    extension.put("f_array", f_array);

                    extension.put("c_isAdopted", 0);
                    extension.put("c_denyReason", "");
                    extension.put("c_denyRemark", "");
                    extension.put("c_userId", 0);
                    extension.put("c_pid", 0);
                    extension.put("c_fid", "");

                    extensionList.add(new InsertOneModel<>(extension));

                    //-------------------------------- deep ----------------------------------
                    DeepFields[] fields = DeepFields.values();
                    Document deep = new Document();
                    Document d_attr = new Document();
                    for (DeepFields t2: fields) {
                        String key = t2.toString();
                        if (columeList.contains(key)) {
                            d_attr.put(key, resultSet.getString(key));
                        }
                    }
                    deep.put("globalId", uuid);
                    deep.put("d_attr", d_attr);

                    deepList.add(new InsertOneModel<>(deep));

                    counter++;
                    commit_counter++;
                    if (counter % 2000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        String print_info = "information [total:%s | counter:%s] [use time:%s:%s:%s]";
                        System.out.println(String.format(print_info, total, counter, hour, min, sec));
                    }
                    if (commit_counter % 2000 == 0) {
                        //保存 mongo
                        mainCollection.bulkWrite(mainList);
                        extensionCollection.bulkWrite(extensionList);
                        deepCollection.bulkWrite(deepList);
                        mainList.clear();
                        extensionList.clear();
                        deepList.clear();
                        commit_counter = 0;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }

            }
            //保存 mongo
            if (commit_counter != 0) {
                mainCollection.bulkWrite(mainList);
                extensionCollection.bulkWrite(extensionList);
                deepCollection.bulkWrite(deepList);
            }


            //create main index......
            System.out.println("create index globalId...");
            Document indexDoc = new Document("globalId", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index b_featureKind...");
            indexDoc = new Document("b_featureKind", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index b_sourceCode...");
            indexDoc = new Document("b_sourceCode", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index b_sourceId...");
            indexDoc = new Document("b_sourceId", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index b_reliability...");
            indexDoc = new Document("b_reliability", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index t_storageDate...");
            indexDoc = new Document("t_storageDate", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index t_status...");
            indexDoc = new Document("t_status", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index i_proposal...");
            indexDoc = new Document("i_proposal", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index i_level...");
            indexDoc = new Document("i_level", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index i_infoName...");
            indexDoc = new Document("i_infoName", 1);
            mainCollection.createIndex(indexDoc);
//            System.out.println("create index i_infoContent...");
//            indexDoc = new Document("i_infoContent", 1);
//            mainCollection.createIndex(indexDoc);
            System.out.println("create index tile_14...");
            indexDoc = new Document("tile_14", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index tile_15...");
            indexDoc = new Document("tile_15", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index tile_16...");
            indexDoc = new Document("tile_16", 1);
            mainCollection.createIndex(indexDoc);
            System.out.println("create index tile_17...");
            indexDoc = new Document("tile_17", 1);
            mainCollection.createIndex(indexDoc);
            //空间索引
            System.out.println("create index loc 2dsphere...");
            indexDoc = new Document("loc", "2dsphere");
            mainCollection.createIndex(indexDoc);

            //create extension index......
            System.out.println("create index globalId...");
            indexDoc = new Document("globalId", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index t_publishDate...");
            indexDoc = new Document("t_publishDate", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index t_submitDate...");
            indexDoc = new Document("t_submitDate", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index t_closeDate...");
            indexDoc = new Document("t_closeDate", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index t_operateDate...");
            indexDoc = new Document("t_operateDate", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index h_outdoor...");
            indexDoc = new Document("h_outdoor", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index h_indoor...");
            indexDoc = new Document("h_indoor", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index h_audit...");
            indexDoc = new Document("h_audit", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index i_poi...");
            indexDoc = new Document("i_poi", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index i_road...");
            indexDoc = new Document("i_road", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index i_precision...");
            indexDoc = new Document("i_precision", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index i_serviceStatus...");
            indexDoc = new Document("i_serviceStatus", 1);
            extensionCollection.createIndex(indexDoc);
            System.out.println("create index loc 2dsphere...");
            indexDoc = new Document("loc", "2dsphere");
            extensionCollection.createIndex(indexDoc);

            //create deep index......
            System.out.println("create index globalId...");
            indexDoc = new Document("globalId", 1);
            deepCollection.createIndex(indexDoc);
//            System.out.println("create index d_attr...");
//            indexDoc = new Document("d_attr", 1);
//            deepCollection.createIndex(indexDoc);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != resultSet) {resultSet.close();}
                if (null != stmt) {stmt.close();}
                if (null != conn) {conn.close();}
                if (null != client) {client.close();}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");

    }

    public enum DeepFields {
        INFO_INTEL_ID,
        INFO_CODE,
        INFO_NAME,
        STATUS,
        URL,
        INFO_TITLE,
        INFO_TYPE,
        INFO_CONTENT,
        TOPIC_NAME,
        INFO_TYPE_CODE,
        INFO_TYPE_NAME,
        KIND_CODE,
        RATING,
        CHAIN,
        ADMIN_CODE,
        ADMIN_NAME,
        RESULT_TYPE,
        USE_LEVEL,
        UPDATE_TYPE,
        GAIN_TYPE,
        PLAN_USE_TIME,
        PROJECT_TYPE,
        PHONE,
        LNG,
        LAT,
        GUIDELON,
        GUIDELAT,
        MAP_CODE,
        CHECK_STATUS,
        CONFIRM_MODE,
        CONFIRM_RESULT,
        TRUST_LEVEL,
        AREA,
        REMARK,
        ADDRESS,
        LENGTH,
        FID,
        FARTHERSON,
        START_POINT,
        PASS_POINT,
        END_POINT,
        FACT_USE_TIME,
        CLOSE_DESC,
        CREATOR,
        CREATE_TIME,
        UPDATER,
        UPDATE_TIME,
        PUBLISHER,
        PUBLISH_TIME,
        MAP_CONFIG,
        DEL_FLAG,
        PRECISION,
        PLAN,
        PUBLISH_CODE,
        PUBLISH_TYPE,
        TMAPS_ID,
        IS_HAVE_IMAGE,
        PUBLISH_VERSION,
        ORIGINAL_ID,
        TENCENT_UPDATE_TIME,
        SERVICE_STATUS,
        FOOD_TYPE,
        }

    //获取配置文件数据库信息
    public static JSONObject getProperties() {

        File directory = new File("db.properties");
        String filePath = directory.getAbsolutePath();
        Properties props = new Properties();
        InputStream in = null;
        JSONObject propObj = null;

        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            String oraHost = props.getProperty("oraHost");
            int oraPort = Integer.valueOf(props.getProperty("oraPort"));
            String oraDb = props.getProperty("oraDb");
            String oraUser = props.getProperty("oraUser");
            String oraPwd = props.getProperty("oraPwd");

            String mongoHost = props.getProperty("msMongoHost");
            int mongoPort = Integer.valueOf(props.getProperty("msMongoPort"));
            String mongoDb = props.getProperty("msMongoDb");

            propObj = new JSONObject();
            propObj.put("oraHost", oraHost);
            propObj.put("oraPort", oraPort);
            propObj.put("oraDb", oraDb);
            propObj.put("oraUser", oraUser);
            propObj.put("oraPwd", oraPwd);

            propObj.put("mongoHost", mongoHost);
            propObj.put("mongoPort", mongoPort);
            propObj.put("mongoDb", mongoDb);

//            System.out.println(oraHost +":"+oraPort+":"+oraDb+":"+oraUser+":"+oraPwd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {in.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
            return propObj;
        }
    }


    public static void main(String[] args) {

        // ------------------------------ 获取配置文件数据库信息 ---------------------------------------------
        JSONObject propObj = getProperties();
        String oraHost = propObj.getString("oraHost");
        int oraPort = propObj.getInteger("oraPort");
        String oraDb = propObj.getString("oraDb");
        String oraUser = propObj.getString("oraUser");//"iqu";
        String oraPwd = propObj.getString("oraPwd");//"zaq1";

        String mongoHost = propObj.getString("mongoHost");
        int mongoPort = propObj.getInteger("mongoPort");
        String mongoDb = propObj.getString("mongoDb");

        Information2Mongo im = new Information2Mongo(oraHost, oraPort, oraDb, oraUser, oraPwd,
                mongoHost, mongoPort, mongoDb);

        im.execute();

    }

}
