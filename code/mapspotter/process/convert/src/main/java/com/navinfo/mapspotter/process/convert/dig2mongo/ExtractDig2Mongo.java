package com.navinfo.mapspotter.process.convert.dig2mongo;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.bson.Document;
import org.geojson.LngLatAlt;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/6/17.
 */
public class ExtractDig2Mongo {

    private String digHost = null;
    private int digPort = 0;
    private String digDb = null;
    private String msHost = null;
    private int msPort = 0;
    private String msDb = null;
    private DataSourceParams digMongoParams;
    private DataSourceParams msMongoParams;

    private static GeometryFactory factory = new GeometryFactory();

    public ExtractDig2Mongo(String digHost, int digPort, String digDb,
                            String msHost, int msPort, String msDb) {

        this.digHost = digHost;
        this.digPort = digPort;
        this.digDb = digDb;
        this.msHost = msHost;
        this.msPort = msPort;
        this.msDb = msDb;

        digMongoParams = IOUtil.makeMongoDBParams(digHost, digPort, digDb);
        msMongoParams = IOUtil.makeMongoDBParams(msHost, msPort, msDb);
    }

    public void execute() {

        long start, end, current;
        int sec, min, hour;
        start = System.currentTimeMillis();
        int total = 0;
        MongoDB digMongo = null;
        MongoDB msMongo = null;

        try {
            //源数据库连接
            digMongo = new MongoDB();
            int digMongoStatus = digMongo.open(this.digMongoParams);
            if (digMongoStatus != 0) {
                System.out.println("Connection digMongoDb failed!!");
                return;
            }

            //目标数据库连接
            msMongo = new MongoDB();
            int msMongoStatus = msMongo.open(this.msMongoParams);
            if (msMongoStatus != 0) {
                System.out.println("Connection msMongoDb failed!!");
                return;
            }

            //查询目标库是否存在
            MongoIterable<String> allDatebases = msMongo.getDbs();
            for (String dbName: allDatebases) {
                if (dbName.equals(this.msDb)) {
                    System.out.println(dbName);
                }
            }

            //获取目标库的所有表
            MongoIterable<String> msAllCollections = msMongo.getTables();

            MongoCollection<Document> digCol = null;
            List<Document> docList = null;
            JSONObject fileds = null;

            List<String> msColNameList = getConvertTargets(digMongo);

            System.out.println("insert collections total:" + msColNameList.size());

            for (String digCollection : msColNameList) {
                convert(digMongo, msMongo, msAllCollections, digCollection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            digMongo.close();
            msMongo.close();
        }

        end = System.currentTimeMillis();
        sec = (int)((end - start) / 1000);
        min = sec / 60;
        hour = min / 60;
        String print_info = String.format("use time:%s:%s:%s, total:%s ", hour, min, sec, total);
        System.out.println(print_info);
        System.out.println("process done!!");
    }

    private void convert(MongoDB digMongo, MongoDB msMongo, MongoIterable<String> msAllCollections, String digCollection) throws IOException {
        List<Document> docList;
        MongoCollection<Document> digCol;
        JSONObject fileds;
        String tile;//查询目标表是否存在
        for (String msCollection: msAllCollections) {
            if (msCollection.equals(digCollection)) {
                msMongo.drop(msCollection);
            }
        }
        System.out.println("insert collections:" + digCollection);

        //查询源数据库，录入目标库
        docList = new ArrayList<>();
        digCol = digMongo.getTable(digCollection);
        //过滤字段
        fileds = new JSONObject();
        fileds.put("_id", 0);
//        fileds.put("type", 1);
//        fileds.put("timestamp", 1);
//        fileds.put("geometry", 1);
        fileds.put("properties.tile", 0);
        fileds.put("properties.tiles", 0);

        int counter = 0;
        FindIterable<Document> rows = digCol.find().projection(Document.parse(fileds.toString()));

        for (Document document : rows) {

            Document geomDoc = (Document) document.get("geometry") ;
            if(null == geomDoc)
                continue;

            String geomJsonStr = geomDoc.toJson();
            String type = geomDoc.getString("type");

            if (type.equals("LineString")) {
                org.geojson.LineString lineString = JsonUtil.getInstance().readValue(geomJsonStr, org.geojson.LineString.class);

                Envelope envelope = convertLineString(lineString).getEnvelopeInternal();

                Coordinate lt = new Coordinate(envelope.getMinX(), envelope.getMaxY());
                Coordinate rb = new Coordinate(envelope.getMaxX(), envelope.getMinY());
                //tile级别
                for (int i = 10; i <= 17; i++) {
                    document.put("tile_" + i, MercatorUtil.bounds2Tiles(lt, rb, i));
                }

            } else if (type.equals("Point")) {
                org.geojson.Point point = JsonUtil.getInstance().readValue(geomJsonStr, org.geojson.Point.class);
                LngLatAlt p = point.getCoordinates();
                Coordinate coord = new Coordinate();
                coord.x = p.getLongitude();
                coord.y = p.getLatitude();
                //tile级别
                for (int i = 10; i <= 17; i++) {
                    tile = MercatorUtil.lonLat2MCode(coord, i);
                    document.put("tile_" + i, tile);
                }
            }

            counter ++;
            docList.add(document);
            if (counter % 5000 == 0) {
                System.out.println(digCollection + ", counter:" + counter);
            }
            if (docList.size() >= 5000) {
                msMongo.insert(digCollection, docList);
                docList.clear();
            }
        }

        if (docList.size() > 0) {
            msMongo.insert(digCollection, docList);
        }
        System.out.println(digCollection + ", total:" + counter);

        //创建索引
        Map<String, Boolean> indexField = new HashMap<>();
        System.out.println("create index type...");
        indexField.put("type", true);
        msMongo.createIndex(digCollection, indexField);
        indexField.clear();
        System.out.println("create index timestamp...");
        indexField.put("timestamp", true);
        msMongo.createIndex(digCollection, indexField);
        indexField.clear();
        System.out.println("create index properties...");
        indexField.put("properties", true);
        indexField.clear();
        System.out.println("create index tile_10...");
        indexField.put("tile_10", true);
        indexField.clear();
        System.out.println("create index tile_11...");
        indexField.put("tile_11", true);
        indexField.clear();
        System.out.println("create index tile_12...");
        indexField.put("tile_12", true);
        indexField.clear();
        System.out.println("create index tile_13...");
        indexField.put("tile_13", true);
        indexField.clear();
        System.out.println("create index tile_14...");
        indexField.put("tile_14", true);
        indexField.clear();
        System.out.println("create index tile_15...");
        indexField.put("tile_15", true);
        indexField.clear();
        System.out.println("create index tile_16...");
        indexField.put("tile_16", true);
        indexField.clear();
        System.out.println("create index tile_17...");
        indexField.put("tile_17", true);
        indexField.clear();

        //空间索引
        Map<String, String> geoIndexFields = new HashMap<>();
        System.out.println("create index geometry 2dsphere...");
        geoIndexFields.put("geometry", "2dsphere");
        msMongo.createGeoIndex(digCollection, geoIndexFields);
        geoIndexFields.clear();
    }

    private List<String> getConvertTargets(MongoDB digMongo){
        //获取源数据库的所有表
        MongoIterable<String> allCollections = digMongo.getTables();

        List<String> msColNameList = new ArrayList<>();

        //缺失道路数据、//施工解除数据、//交限数据
        for (String digCollection : allCollections) {

            //筛选库名称日期，只取最新的库
            if (!digCollection.contains("qingbao_road")
                    && !digCollection.contains("sogou_construction")
                    && !digCollection.contains("sogou_restric_detail")) {
                continue;
            }

            msColNameList.add(digCollection);
        }

        return msColNameList;
    }


    //筛选库名称日期，只取最新的库
    private String filterCollectionName(MongoIterable<String> allCollections,
                                       String colName,
                                       Boolean addPoint) {

        List<Integer> filterDate = new ArrayList<>();
        String msCollectionName = null;

        //去掉日期
        String[] colSplit = colName.split("_");
        StringBuffer preffix = new StringBuffer();
        for (int i = 0; i <= colSplit.length; i ++) {
            if (i < colSplit.length - 1) {
                preffix.append(colSplit[i]);
                preffix.append("_");
            }
        }
        String filterName = preffix.toString();

        for (String digCollection : allCollections) {
            try {
                if (addPoint && digCollection.contains(filterName)) {
                    String[] nameSplit = digCollection.split("_");
                    int date = Integer.valueOf(nameSplit[nameSplit.length - 1]);
                    filterDate.add(date);
                }
                else if (!addPoint && digCollection.contains(filterName) && !digCollection.contains("point_")) {
                    String[] nameSplit = digCollection.split("_");
                    int date = Integer.valueOf(nameSplit[nameSplit.length - 1]);
                    filterDate.add(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (filterDate.size() > 0) {

            Collections.sort(filterDate);

            int lastDate = filterDate.get(filterDate.size() - 1);
            msCollectionName = filterName + String.valueOf(lastDate);
            System.out.println("The last collection:" + msCollectionName);
        }

        return msCollectionName;
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
            String digHost = props.getProperty("digMongoHost");
            int digPort = Integer.valueOf(props.getProperty("digMongoPort"));
            String digDb = props.getProperty("digMongoDb");

            String msHost = props.getProperty("msMongoHost");
            int msPort = Integer.valueOf(props.getProperty("msMongoPort"));
            String msDb = props.getProperty("digMongoDb");

            propObj = new JSONObject();
            propObj.put("digHost", digHost);
            propObj.put("digPort", digPort);
            propObj.put("digDb", digDb);

            propObj.put("msHost", msHost);
            propObj.put("msPort", msPort);
            propObj.put("msDb", msDb);

            System.out.println("digHost:" + digHost +", digPort:"+":"+digPort + ", digDb:" + digDb +
                    ", msHost:"+ msHost + ", msPort:" + msPort + ", msDb:" + msDb);
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

    public static LineString convertLineString(org.geojson.LineString line){
        List<LngLatAlt> coordinates = line.getCoordinates();

        Coordinate[] lineStringList = new Coordinate[coordinates.size()];
        for (int i = 0;i < coordinates.size(); i++) {
            LngLatAlt point = coordinates.get(i);
            double lon = point.getLongitude();
            double lat = point.getLatitude();
            Coordinate coordinate = new Coordinate(lon, lat);
            lineStringList[i] = coordinate;
        }

        return factory.createLineString(lineStringList);
    }

    public static void main(String args[]) {
        // ------------------------------ 获取配置文件数据库信息 ---------------------------------------------
        JSONObject propObj = getProperties();
        String digHost = propObj.getString("digHost");
        int digPort = propObj.getInteger("digPort");
        String digDb = propObj.getString("digDb");

        String msHost = propObj.getString("msHost");
        int msPort = propObj.getInteger("msPort");
        String msDb = propObj.getString("msDb");

        ExtractDig2Mongo eo2g = new ExtractDig2Mongo(digHost, digPort, digDb, msHost, msPort, msDb);
        eo2g.execute();
    }


}
