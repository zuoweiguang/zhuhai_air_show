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
import com.navinfo.mapspotter.foundation.model.geojson.GeoJsonPoint;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import oracle.spatial.geometry.JGeometry;
import org.bson.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Created by zuoweiguang on 2016/6/15.
 */
public class ExtractOracle2Mongo {

    private String oraHost = null;
    private int oraPort = 0;
    private String oraDb = null;
    private String oraUser = null;
    private String oraPwd = null;

    private final DataSourceParams mongoParams;

    public ExtractOracle2Mongo(String oraHost, int oraPort, String oraDb, String oraUser, String oraPwd,
                               String mongoHost, int mongoPort, String mongoDb) {

        this.oraHost = oraHost;
        this.oraPort = oraPort;
        this.oraDb = oraDb;
        this.oraUser = oraUser;
        this.oraPwd = oraPwd;

        mongoParams = IOUtil.makeMongoDBParams(mongoHost, mongoPort, mongoDb);
    }

    private Map<String, String> initPoiKindCode(){
        HashMap<String, String> mainkind = new HashMap<>();

        SAXReader reader = new SAXReader();

        InputStream in = ExtractOracle2Mongo.class.getResourceAsStream("/POIMainKind");

        try {
            org.dom4j.Document doc = reader.read(in);

            Element root = doc.getRootElement();

            List<Node> conditions = root.selectNodes("//Kind");

            for(Node n : conditions){
                Element kindEle = (Element)n;

                mainkind.put(kindEle.attributeValue("maincode"),
                        kindEle.attributeValue("mainname"));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return mainkind;
    }

    public void execute_poi(String mongoColName) {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        int total = 0;
        String url = "jdbc:oracle:thin:@" + this.oraHost + ":" + this.oraPort + ":" + this.oraDb;
        MongoClient client = null;

        Map<String, String> poimainKind = initPoiKindCode();

        try {
            System.out.println("Extract Oracle IX_POI to Mongo......");

            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, this.oraUser, this.oraPwd);

            client = new MongoClient(new ServerAddress(mongoParams.getHost(), mongoParams.getPort()));
            MongoDatabase db = client.getDatabase(mongoParams.getDb());

            //在oracle中创建 TEMP_EXP_IX_POI POI临时表
            System.out.println("CREATE TEMP_EXP_IX_POI......");
            create_TEMP_EXP_IX_POI(conn);
            current = System.currentTimeMillis();
            sec = (int)((current - start) / 1000);
            min = sec / 60;
            hour = min / 60;
            String print_info = "CREATE TEMP_EXP_IX_POI [use time:%s:%s:%s]";
            System.out.println(String.format(print_info, total, hour, min, sec));


            //创建 poi 表
            for (String colName: db.listCollectionNames()) {
                if (colName.equals(mongoColName)) {
                    db.getCollection(mongoColName).drop();
                    break;
                }
            }

            db.createCollection(mongoColName);
            MongoCollection<Document> dbColl = db.getCollection(mongoColName);
            System.out.println("create poi collection success!!");

            List<WriteModel<Document>> doces = new ArrayList<>();
            List<JSONObject> contactList = new ArrayList<>();

            //查询抽取数据的总量
            String oraSql = "SELECT COUNT(PID) FROM TEMP_EXP_IX_POI";
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(oraSql);
            resultSet.next();
            total = resultSet.getInt(1);
            if (resultSet != null) {resultSet.close();resultSet = null;}
            if (stmt != null) {stmt.close();stmt = null;}

            String sql = "SELECT PID, KIND_CODE, NAME, ADDRESS, CONTACT_TYPE, CONTACT, GEOMETRY FROM TEMP_EXP_IX_POI";
            stmt = conn.createStatement();
            stmt.setQueryTimeout(60 * 60 * 4);
            resultSet = stmt.executeQuery(sql);
            resultSet.setFetchSize(5000);

            int counter = 0;
            int commit_counter = 0;
            JSONObject poiObj = null;
            GeoJsonPoint loc = null;
            Coordinate coord = null;
            String tile = null;
            while (resultSet.next()) {
                try {
                    Integer pid = resultSet.getInt("PID");
                    String kind_code = resultSet.getString("KIND_CODE");
                    String name = resultSet.getString("NAME");
                    String address = resultSet.getString("ADDRESS") ;
                    if (null == address) {
                        address = "";
                    }
                    String contactType = resultSet.getString("CONTACT_TYPE");
                    String contacts = resultSet.getString("CONTACT");
                    if (null != contactType && contactType.contains("|")) {
                        String[] type = contactType.split("|");
                        String[] contact = contacts.split("|");
                        for (int i = 0; i < type.length; i ++) {
                            JSONObject contactJson = new JSONObject();
                            contactJson.put("contact_type", type[i]);
                            contactJson.put("contact_type", contact[i]);
                            contactList.add(contactJson);
                        }
                    }

                    byte[] spatialBytes = resultSet.getBytes("GEOMETRY");
                    JGeometry sdoGeo = JGeometry.load(spatialBytes);
                    double[] point = sdoGeo.getPoint();

                    poiObj = new JSONObject();
                    poiObj.put("pid", pid);
                    poiObj.put("kindcode", kind_code);

                    String mainKind = kind_code.substring(0, 2);
                    poiObj.put("mainkind", mainKind);

//                    String mainClass = "其他";
//                    if(poimainKind.containsKey(mainKind)){
//                        mainClass = poimainKind.get(mainKind);
//                    }

                    poiObj.put("form_name", name);
                    poiObj.put("address", address);
                    poiObj.put("contact", contactList);

                    poiObj.put("month_edit", 0);
                    poiObj.put("day_edit", 0);
                    poiObj.put("collect", 0);

                    poiObj.put("x", point[0]);
                    poiObj.put("y", point[1]);
                    //空间索引字段
                    loc = new GeoJsonPoint();
                    loc.setCoordinates(point[0], point[1]);
                    poiObj.put("loc", loc);
                    //tile级别
                    coord = new Coordinate(point[0], point[1]);
                    tile = MercatorUtil.lonLat2MCode(coord, 14);
                    poiObj.put("tile_14", tile);
                    tile = MercatorUtil.lonLat2MCode(coord, 15);
                    poiObj.put("tile_15", tile);
                    tile = MercatorUtil.lonLat2MCode(coord, 16);
                    poiObj.put("tile_16", tile);
                    tile = MercatorUtil.lonLat2MCode(coord, 17);
                    poiObj.put("tile_17", tile);

                    doces.add(new InsertOneModel<>(Document.parse(poiObj.toString())));

                    contactList.clear();

                    counter++;
                    commit_counter++;
                    if (counter % 5000 == 0) {
                        current = System.currentTimeMillis();
                        sec = (int)((current - start) / 1000);
                        min = sec / 60;
                        hour = min / 60;
                        print_info = "poi [total:%s | counter:%s] [use time:%s:%s:%s]";
                        System.out.println(String.format(print_info, total, counter, hour, min, sec));
                    }
                    if (commit_counter % 5000 == 0) {
                        //保存 mongo
                        dbColl.bulkWrite(doces);
                        doces.clear();
                        commit_counter = 0;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    contactList.clear();
                }

            }
            //保存 mongo
            if (commit_counter != 0) {
                dbColl.bulkWrite(doces);
            }

            //创建索引
            System.out.println("create index pid...");
            Document indexDoc = new Document("pid", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index kindcode...");
            indexDoc = new Document("kindcode", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index mainkind...");
            indexDoc = new Document("mainkind", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index form_name...");
            indexDoc = new Document("form_name", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index address...");
            indexDoc = new Document("address", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index contact...");
            indexDoc = new Document("contact", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index month_edit...");
            indexDoc = new Document("month_edit", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index day_edit...");
            indexDoc = new Document("day_edit", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index collect...");
            indexDoc = new Document("collect", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index x...");
            indexDoc = new Document("x", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index y...");
            indexDoc = new Document("y", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index tile_14...");
            indexDoc = new Document("tile_14", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index tile_15...");
            indexDoc = new Document("tile_15", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index tile_16...");
            indexDoc = new Document("tile_16", 1);
            dbColl.createIndex(indexDoc);
            System.out.println("create index tile_17...");
            indexDoc = new Document("tile_17", 1);
            dbColl.createIndex(indexDoc);
            //空间索引
            System.out.println("create index loc 2dsphere...");
            indexDoc = new Document("loc", "2dsphere");
            dbColl.createIndex(indexDoc);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {conn.close();conn = null;}
                if (stmt != null) {stmt.close();stmt = null;}
                if (resultSet != null) { resultSet.close();resultSet = null;}
                if (client != null) {client.close();client = null;}
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

    //在oracle中创建POI临时表
    public void create_TEMP_EXP_IX_POI(Connection conn) {
        Statement tmp_stmt = null;
        try {
            tmp_stmt = conn.createStatement();
            String sql = "DROP TABLE TEMP_EXP_IX_POI";
            try {
                tmp_stmt.executeQuery(sql);
                conn.commit();
                System.out.println("DROP TABLE TEMP_EXP_IX_POI......");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("DROP TABLE TEMP_EXP_IX_POI FAILED......");
            }
            sql = "CREATE TABLE TEMP_EXP_IX_POI AS \n" +
                            "SELECT -- PARALLEL(P 10)\n" +
                            " P.PID,\n" +
                            " P.KIND_CODE,\n" +
                            " NAME.NAME,\n" +
                            " ADDRESS.ADDRESS,\n" +
                            " CONTACT.CONTACT_TYPE,\n" +
                            " CONTACT.CONTACT,\n" +
                            " P.GEOMETRY\n" +
                            "  FROM IX_POI P,\n" +
                            "       (SELECT N.POI_PID, N.NAME\n" +
                            "          FROM IX_POI_NAME N\n" +
                            "         WHERE N.LANG_CODE = 'CHI'\n" +
                            "           AND N.NAME_TYPE = 1\n" +
                            "           AND N.NAME_CLASS = 1) NAME,\n" +
                            "       (SELECT DISTINCT A.POI_PID, LISTAGG(NVL(A.FULLNAME, 0), '|') WITHIN\n" +
                            "         GROUP(\n" +
                            "         ORDER BY A.FULLNAME) OVER(PARTITION BY A.POI_PID) AS ADDRESS\n" +
                            "          FROM IX_POI_ADDRESS A\n" +
                            "         WHERE A.LANG_CODE = 'CHI') ADDRESS,\n" +
                            "       (SELECT DISTINCT C.POI_PID,\n" +
                            "                        LISTAGG(NVL(C.CONTACT_TYPE, 0), '|') WITHIN\n" +
                            "         GROUP(\n" +
                            "         ORDER BY C.CONTACT_TYPE, C.CONTACT) OVER(PARTITION BY C.POI_PID) AS CONTACT_TYPE, \n" +
                            "                        LISTAGG(NVL(C.CONTACT, 0), '|') WITHIN\n" +
                            "         GROUP(\n" +
                            "         ORDER BY C.CONTACT_TYPE, C.CONTACT) OVER(PARTITION BY C.POI_PID) AS CONTACT\n" +
                            "          FROM IX_POI_CONTACT C\n" +
                            "         WHERE C.CONTACT_TYPE IN (1, 2)) CONTACT\n" +
                            " WHERE P.PID = NAME.POI_PID(+)\n" +
                            "   AND P.PID = ADDRESS.POI_PID(+)\n" +
                            "   AND P.PID = CONTACT.POI_PID(+)";
            tmp_stmt.executeQuery(sql);
            conn.commit();
            System.out.println("CREATE TABLE TEMP_EXP_IX_POI SUCCESS!!");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {if (tmp_stmt != null) {tmp_stmt.close();}} catch (SQLException e) {e.printStackTrace();}
        }

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
            String mongoColName = props.getProperty("msMongoColName");

            propObj = new JSONObject();
            propObj.put("oraHost", oraHost);
            propObj.put("oraPort", oraPort);
            propObj.put("oraDb", oraDb);
            propObj.put("oraUser", oraUser);
            propObj.put("oraPwd", oraPwd);

            propObj.put("mongoHost", mongoHost);
            propObj.put("mongoPort", mongoPort);
            propObj.put("mongoDb", mongoDb);
            propObj.put("mongoColName", mongoColName);
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

    public static void main(String [] args) {

        // ------------------------------ 获取配置文件数据库信息 ---------------------------------------------
        JSONObject propObj = getProperties();
        String oraHost = propObj.getString("oraHost");
        int oraPort = propObj.getInteger("oraPort");
        String oraDb = propObj.getString("oraDb");
        String oraUser = propObj.getString("oraUser");
        String oraPwd = propObj.getString("oraPwd");

        String mongoHost = propObj.getString("mongoHost");
        int mongoPort = propObj.getInteger("mongoPort");
        String mongoDb = propObj.getString("mongoDb");
        String mongoColName = propObj.getString("mongoColName");

        ExtractOracle2Mongo eo2g = new ExtractOracle2Mongo(oraHost, oraPort, oraDb, oraUser, oraPwd,
                mongoHost, mongoPort, mongoDb);

        eo2g.execute_poi(mongoColName);

    }
}
