package com.navinfo.mapspotter.process.storage.crud;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.loader.rabbitmq.JsonMessageConsumer;
import com.navinfo.mapspotter.process.loader.rabbitmq.MessageConnector;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接收消息，实时处理Mongo数据更新
 * Created by SongHuiXing on 6/22 0022.
 */
public class RealtimeMongoUpdate extends JsonMessageConsumer implements AutoCloseable {

    public enum OperateType{
        INSERT,
        UPDATE,
        DELETE,
    }

    private final DataSourceParams params;

    private final MessageConnector msgListener;

    private final String msgQueue;

    private final String mongo_poi_tablename = "poi";

    private WKTReader wktReader = new WKTReader();

    public RealtimeMongoUpdate(String host, int port, String db, String user, String pwd,
                               String msgUri, String msgExchange, String queue) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        if(null == user){
            params = IOUtil.makeMongoDBParams(host, port, db);
        } else {
            params = IOUtil.makeMongoDBParams(host, port, db, user, pwd);
        }

        msgListener = new MessageConnector(msgUri, msgExchange);

        msgQueue = queue;
    }

    private MongoDB db = null;

    public boolean open(){
        if(null != db)
            return false;

        db = (MongoDB)DataSource.getDataSource(params);

        if(null == db){
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        if(!msgListener.openListenning(msgQueue, this))
            return false;

        return true;
    }

    @Override
    public void close() throws Exception {
        msgListener.close();

        if(null != db){
            db.close();
        }
    }

    /**
     * {"pid":123,"status":"INSERT","type":"Collect","name":"xx","address":"xx","kindCode":"12345","geometry":"POINT(106.313 39.123)"}
       {"pid":123,"status":"UPDATE","type":"DayEdit","name":"xx","address":"xx"}
       {"pid":123,"status":"DELETE","type":"MonthEdit"}
     * @param json
     * @return
     */
    @Override
    public boolean consumer(String json){
        JsonUtil jsonUtil = JsonUtil.getInstance();

        Map<String, Object> poimsg = null;

        try {
            poimsg = jsonUtil.readMap(json);
        } catch (IOException e) {
            return false;
        }

        OperateType opType = OperateType.valueOf(poimsg.get("status").toString());
        poimsg.remove("status");

        synchronized (lock){
            switch (opType){
                case INSERT:
                    return CreatePoi(poimsg);
                case UPDATE: {
                    int pid = (int)poimsg.get("pid");
                    poimsg.remove("pid");
                    return UpdatePoi(pid, poimsg);
                }
                case DELETE: {
                    int pid = (int)poimsg.get("pid");
                    return DeletePoi(pid);
                }
            }
        }

        return false;
    }

    private boolean CreatePoi(Map<String, Object> attributes){
        Map<String, Object> attrs = mapMsgAttr2MongoAttr(attributes);

        return db.insert(mongo_poi_tablename, attrs) == 0;
    }

    private boolean UpdatePoi(int id, Map<String, Object> attributes){
        Map<String, Object> attrs = mapMsgAttr2MongoAttr(attributes);

        MongoOperator operator = new MongoOperator();
        operator.and(MongoOperator.FilterType.EQ, "pid", id);

        return db.update(mongo_poi_tablename, operator, attrs);
    }

    private boolean DeletePoi(int id){
        MongoOperator operator = new MongoOperator();
        operator.and(MongoOperator.FilterType.EQ, "pid", id);

        long res = db.delete(mongo_poi_tablename, operator);

        return res == 1;
    }

    private Map<String, Object> mapMsgAttr2MongoAttr(Map<String, Object> msgAttrs){
        HashMap<String, Object> mongoAttrs = new HashMap<>();

        List<String> poifields = POIFieldsConfig.getFields();

        for (Map.Entry<String, Object> attr : msgAttrs.entrySet()){
            String key = attr.getKey();

            if(key.equals("geometry")){
                String wkt = attr.getValue().toString();
                try {
                    Point position = (Point) wktReader.read(wkt);
                    mongoAttrs.put("loc", new org.geojson.Point(position.getX(), position.getY()));
                    mongoAttrs.put("x", position.getX());
                    mongoAttrs.put("y", position.getY());

                    for (int i = 14; i < 18; i++) {
                        String tilekey = "tile_" + i;
                        mongoAttrs.put(tilekey, MercatorUtil.lonLat2MCode(position.getCoordinate(), i));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if(key.equals("name")){
                mongoAttrs.put("form_name", attr.getValue());
            } else if(key.equals("type")){
                String editType = attr.getValue().toString();
                if(editType.equals("MonthEdit")){
                    mongoAttrs.put("month_edit", 1);
                } else if(editType.equals("DayEdit")){
                    mongoAttrs.put("day_edit", 1);
                } else {
                    mongoAttrs.put("collect", 1);
                }
            } else {
                if(poifields.contains(key)) {
                    mongoAttrs.put(key, attr.getValue());
                }
            }
        }

        return mongoAttrs;
    }
}
