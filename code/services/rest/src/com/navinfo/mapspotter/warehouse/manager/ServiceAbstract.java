package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.MongoDBCursor;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import com.navinfo.mapspotter.process.storage.pool.MongoPool;
import com.navinfo.mapspotter.warehouse.connection.DBPool;
import org.bson.Document;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Created by cuiliang on 2016/6/12.
 */
public abstract class ServiceAbstract {
    public DataSourceParams pgParams;
    public MongoDB database;

    public JSONObject output;
    public int errcode;
    public String errmsg;
    public JSONObject data;

    public ServiceAbstract() {
        DBPool pool = DBPool.getInstance();
        MongoPool mongoPool = pool.getMongoManagerPool();
        database = mongoPool.getMongo();
    }

    public void organizationJson(MongoDBCursor cursor, JSONObject json) throws Exception {
        Map<String, Object> map = cursor.convert();
        map.remove("_id");
        json.putAll(map);
    }

    public String add(String table_name, String json) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        try {
            String id = StringUtil.uuid().replace("-","");
            Document inputJson = Document.parse(json);
            inputJson.put("id", id);
            database.insert(table_name, inputJson);
            errcode = 0;
            data.put("id", id);
        } catch (Exception e) {
            errcode = 1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);

        return output.toJSONString();
    }

    public boolean delete(String table_name, MongoOperator condition, String msg){
        try {
            database.delete(table_name, condition);
            return true;
        } catch (Exception e) {
            msg = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }


    public String delete(String table_name, MongoOperator condition) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();

        if(delete(table_name, condition, errmsg)){
            errcode = 0;
        }
        else{
            errcode = 1;
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return output.toJSONString();
    }

    public JSONObject queryJsonObject(String table_name, MongoOperator condition) throws Exception {
        JSONObject json = null;

        MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, condition);
        while (cursor.next()) {
            json = new JSONObject();
            organizationJson(cursor, json);
        }
        return json;
    }

    public String query(String table_name, MongoOperator condition) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        try {
            JSONObject json = queryJsonObject(table_name, condition);
            if (json == null) {
                errcode = 2;
                errmsg = "no record found";
            } else {
                data.put(table_name, json);
                errcode = 0;
            }

        } catch (Exception e) {
            errcode = 1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return output.toJSONString();
    }

    public JSONArray queryJsonArray(String table_name,
                                    MongoOperator condition,
                                    String... ascendFields) throws Exception {
        JSONArray array = new JSONArray();
        MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, condition);
        if(ascendFields.length > 0){
            cursor.ascendSort(ascendFields);
        }

        while (cursor.next()) {
            JSONObject jsons = new JSONObject();
            organizationJson(cursor, jsons);
            array.add(jsons);
        }
        return array;
    }

    public String queryList(String table_name, MongoOperator condition,
                            String... ascendFields) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();

        try {
            JSONArray array = queryJsonArray(table_name, condition, ascendFields);
            if (array.size() == 0) {
                errcode = 2;
                errmsg = "no record found";
            } else {
                errcode = 0;
                data.put(table_name + "s", array);
            }
        } catch (Exception e) {
            errcode = 1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return output.toJSONString();
    }

    public String update(String table_name, String json) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();

        Document inputJson = Document.parse(json);
        String id = inputJson.getString("id");

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);

        try {
            database.update(table_name, condition, inputJson);
            errcode = 0;
        } catch (Exception e) {
            errcode = 1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);

        return output.toJSONString();
    }

    public Response buildResponse(String output) {
        Response.ResponseBuilder builder;
        builder = Response.ok(output);
        builder.header("Access-Control-Allow-Origin", "*");
        builder.header("Access-Control-Allow-Methods", "*");
        return builder.build();
    }
}
