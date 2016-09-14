package com.navinfo.mapspotter.process.storage.crud;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.util.JSON;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.MongoDBCursor;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.dom4j.*;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 情报数据库更新
 * Created by SongHuiXing on 6/24 0024.
 */
public class InfoMongoUpdate {
    private static final String MainInfoTablename = "information_main";
    private static final String FeedbackInfoTablename = "information_extension";
    private static final String DeepInfoTablename = "information_deep";

    private static final String InfoPrimaryKeyname = "globalId";

    protected final MongoDB mongoDB;

    public InfoMongoUpdate(MongoDB db){
        mongoDB = db;
    }

    /**
     * 按照grid号码下载情报
     * @param gridnum
     * @return
     */
    public JSONArray getInfomation(String gridnum){
        JSONArray infomations = new JSONArray();

        List<String> exportfields = getFields("Download4Collector");

        try (MongoDBCursor cursor = (MongoDBCursor)mongoDB.query(MainInfoTablename,
                                                                infoInGridCondition(gridnum))){
            while (cursor.next()) {
                Map<String, Object> main = cursor.convert();

                infomations.add(buildInformation4Collector(main, exportfields));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return infomations;
    }

    /**
     * 判断在指定grid中是否具有可下载的情报
     * @param gridnum
     * @return
     */
    public boolean isThereAvaliableInfo(String gridnum){
        try (MongoDBCursor cursor = (MongoDBCursor)mongoDB.query(MainInfoTablename,
                infoInGridCondition(gridnum))){

            return cursor.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 根据globalId号码获取情报信息
     * @param id
     * @return
     */
    public JSONObject getInformation(String id){
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, InfoPrimaryKeyname, id);

        List<String> exportfields = getFields("Download4Collector");

        try (MongoDBCursor cursor = (MongoDBCursor)mongoDB.query(MainInfoTablename, condition)){
            if(!cursor.next())
                return null;

            Map<String, Object> main = cursor.convert();

            return buildInformation4Collector(main, exportfields);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 构建符合手持端下载的情报对象
     * @param mainAttrs
     * @param collectorFields
     * @return
     */
    private JSONObject buildInformation4Collector(Map<String, Object> mainAttrs,
                                                  List<String> collectorFields){
        JSONObject info = new JSONObject();

        String rowkey = (String) mainAttrs.get(InfoPrimaryKeyname);

        info.putAll(filterFields(mainAttrs, collectorFields));

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, InfoPrimaryKeyname, rowkey);

        try (MongoDBCursor fedCur = (MongoDBCursor) mongoDB.query(FeedbackInfoTablename, condition)) {
            while (fedCur.next()){
                Map<String, Object> fed = fedCur.convert();
                info.putAll(filterFields(fed, collectorFields));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String geojson = JSON.serialize(info.get("g_location"));
        if(null != geojson){
            try {
                String wkt = GeoUtil.geometry2WKT(GeoUtil.readGeojson(geojson));
                info.put("g_location", wkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        geojson = JSON.serialize(info.get("g_guide"));
        if(null != geojson){
            try {
                String wkt = GeoUtil.geometry2WKT(GeoUtil.readGeojson(geojson));
                info.put("g_guide", wkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    /**
     * 新增手持端采集情报
     * @param attributes
     * @return
     */
    public Map.Entry<String, Boolean> insertInfo(JSONObject attributes)
            throws IOException {
        Map<String, Object> main = createMain(attributes);
        String globalId = (String) main.get(InfoPrimaryKeyname);
        if(null == globalId || globalId.isEmpty()) {
            globalId = StringUtil.lessUUID();
            main.put(InfoPrimaryKeyname, globalId);
        }

        if(mongoDB.insert(MainInfoTablename, main) != 0)
            return new AbstractMap.SimpleEntry<>(globalId, false);

        Map<String, Object> feed = createExtension(attributes);
        feed.put(InfoPrimaryKeyname, globalId);

        if(mongoDB.insert(FeedbackInfoTablename, feed) != 0)
            return new AbstractMap.SimpleEntry<>(globalId, false);

        return new AbstractMap.SimpleEntry<>(globalId, true);
    }

    /**
     * 更新情报反馈信息
     * @param attributes
     * @return
     * @throws IllegalArgumentException
     */
    public Map.Entry<String, Boolean> updateExtension(JSONObject attributes) throws IllegalArgumentException{
        String targetRowkey = (String) attributes.get(InfoPrimaryKeyname);

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, InfoPrimaryKeyname, targetRowkey);

        boolean sucess = mongoDB.update(FeedbackInfoTablename,
                                        condition,
                                        filterFields(attributes, getFields("extension")));

        return new AbstractMap.SimpleEntry<>(targetRowkey, sucess);
    }

    /**
     * 将attributes内的所有在filters中存在的字段过滤出来
     * @param attributes
     * @param filters
     * @return
     */
    private static Map<String, Object> filterFields(Map<String, Object> attributes,
                                                    List<String> filters){
        HashMap<String, Object> fields = new HashMap<>();

        for (String field : filters) {
            if (attributes.containsKey(field)) {
                fields.put(field, attributes.get(field));
            }
        }

        return fields;
    }

    /**
     * 创建一个情报的主表对象
     * @param attributes
     * @return
     * @throws IOException
     */
    private static Map<String, Object> createMain(Map<String, Object> attributes) throws IOException {
        HashMap<String, Object> mainobj = new HashMap<>();

        mainobj.putAll(filterFields(attributes, getFields("main")));

        Geometry geo = GeoUtil.wkt2Geometry((String) mainobj.get("g_location"));
        String geojson = GeoUtil.writeGeojson(geo);
        mainobj.put("g_location", com.mongodb.util.JSON.parse(geojson));

        Envelope env = geo.getEnvelopeInternal();

        Coordinate lt = new Coordinate(env.getMinX(), env.getMaxY());
        Coordinate rb = new Coordinate(env.getMaxX(), env.getMinY());

        for (int i = 6; i <= 17; i++) {
            mainobj.put("tile_"+i, MercatorUtil.bounds2Tiles(lt, rb,i));
        }

        return mainobj;
    }

    /**
     * 创建一个情报的扩展表对象
     * @param attributes
     * @return
     */
    private static JSONObject createExtension(Map<String, Object> attributes){
        JSONObject extension = new JSONObject();

        extension.putAll(filterFields(attributes, getFields("extension")));

        return extension;
    }

    /**
     * 从配置文件获取指定节的配置字段列表
     * @param tagname
     * @return
     */
    private static ArrayList<String> getFields(String tagname){
        ArrayList<String> fields = new ArrayList<>();

        SAXReader reader = new SAXReader();

        InputStream in = InfoMongoUpdate.class.getResourceAsStream("/InfoFieldsConfig");

        try {
            Document doc = reader.read(in);

            Element root = doc.getRootElement();

            Element feedbackEle = (Element) root.selectSingleNode(tagname);

            for(Object n : feedbackEle.elements()){

                Element childEle = (Element)n;

                fields.add(childEle.getName());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return fields;
    }

    private MongoOperator infoInGridCondition(String gridnum){
        double[] box = MeshUtil.grid2Rect(gridnum);

        MongoOperator condition = new MongoOperator();
        condition.ands(MongoOperator.FilterType.GeoWithinBox,
                "loc",
                box[0], box[1], box[2], box[3]);
        condition.and(MongoOperator.FilterType.LT, "t_status", 3);

        Date today = new Date();
        condition.and(MongoOperator.FilterType.LTE, "t_expectDate",
                    (today.getTime() / 1000)+"");

        return condition;
    }
}
