package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.MongoDBCursor;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/6/17.
 */
@Path("/dataSource")
public class DataSourceService extends ServiceAbstract {

    public static String table_name = "dataSource";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is layer dataSource";
    }

    @POST
    @Path("add")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response addDataSource(String json) {
        return buildResponse(add(table_name, json));
    }

    public Response addDataSource(@FormParam("source") String source,
                                  @FormParam("type") String type,
                                  @FormParam("source-layers") String source_layers,
                                  @FormParam("tiles") String tiles,
                                  @FormParam("url") String url,
                                  @FormParam("tileSize") int tileSize,
                                  @FormParam("editable") boolean editable,
                                  @FormParam("userID") String userID,
                                  @FormParam("desc") String desc,
                                  @FormParam("minzoom") int minzoom,
                                  @FormParam("maxzoom") int maxzoom) {

        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        try {
            Map dataSource = new HashMap();
            dataSource.put("id", StringUtil.uuid());
            dataSource.put("source", source);
            dataSource.put("type", type);
            dataSource.put("source-layers", JSONArray.parse(source_layers));
            if (tiles != null && !tiles.trim().equals(""))
                dataSource.put("tiles", JSONArray.parse(tiles));
            if (url != null && !url.trim().equals(""))
                dataSource.put("url", url);
            if (tileSize != 0)
                dataSource.put("tileSize", tileSize);
            dataSource.put("editable", editable);
            dataSource.put("userID", userID);
            dataSource.put("desc", desc);
            dataSource.put("minzoom", minzoom);
            dataSource.put("maxzoom", maxzoom);
            database.insert(table_name, dataSource);
            errcode = 0;
        } catch (Exception e) {
            e.printStackTrace();
            errcode = 1;
            errmsg = e.getMessage();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);

        return buildResponse(output.toJSONString());
    }


    @POST
    @Path("update")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response updateDataSource(String json) {
        return buildResponse(update(table_name, json));
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response delDataSource(String json) {

        Document inputJson = Document.parse(json);
        String id = inputJson.getString("id");

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(delete(table_name, condition));
    }

    @GET
    @Path("list/{userID}")
    @Produces("text/plain;charset=UTF-8")
    public Response getDataSourceUserID(@PathParam("userID") String userID) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "userID", userID);
        condition.or(MongoOperator.FilterType.EQ, "userID", "");
        //System.out.print(queryList(table_name, condition));
        return buildResponse(queryList(table_name, condition));
    }

    @GET
    @Path("/{id}")
    @Produces("text/plain;charset=UTF-8")
    public Response getDataSourceByID(@PathParam("id") String id) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(query(table_name, condition));
    }

    @GET
    @Path("layer/{id}")
    @Produces("text/plain;charset=UTF-8")
    public Response getDataSourceLayersByID(@PathParam("id") String id) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);

        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        List<String> fields = new ArrayList();
        fields.add("source-layers.id");
        fields.add("source-layers.type");
        fields.add("source-layers.name");

        try {
            MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, fields, condition);
            while (cursor.next()) {
                data.put("source-layers", cursor.get("source-layers"));
            }
            errcode = 0;
        } catch (Exception e) {
            e.printStackTrace();
            errcode = 1;
            errmsg = e.getMessage();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return buildResponse(output.toJSONString());

    }

    @GET
    @Path("layer/{id}/{layer}")
    @Produces("text/plain;charset=UTF-8")
    public Response getDataSourceLayersAttrBySourceLayer(@PathParam("id") String id,
                                                         @PathParam("layer") String layer) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        condition.and(MongoOperator.FilterType.EQ, "source-layers.id", layer);
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        List<String> fields = new ArrayList();
        fields.add("source-layers.$");

        try {

            MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, fields, condition);
            while (cursor.next()) {
                JSONArray attrsJson = new JSONArray();
                ArrayList<Document> layers = (ArrayList) cursor.get("source-layers");
                for (Document doc : layers) {
                    ArrayList<Document> attrs = (ArrayList) doc.get("attrs");
                    for (Document attr : attrs) {
                        JSONObject attrJson = new JSONObject();
                        attrJson.put("id", attr.get("id"));
                        attrJson.put("name", attr.get("name"));
                        attrJson.put("attr", attr.get("attr"));
                        attrsJson.add(attrJson);
                    }
                }
                data.put("attrs", attrsJson);
            }
            errcode = 0;
        } catch (Exception e) {
            e.printStackTrace();
            errcode = 1;
            errmsg = e.getMessage();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return buildResponse(output.toJSONString());

    }


    @GET
    @Path("layer/{id}/{layer}/{attr}")
    @Produces("text/plain;charset=UTF-8")
    public Response getDataSourceLayersAttrValueBySourceLayerAttr(@PathParam("id") String id,
                                                                  @PathParam("layer") String layer,
                                                                  @PathParam("attr") String attr) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        condition.and(MongoOperator.FilterType.EQ, "source-layers.id", layer);
        condition.and(MongoOperator.FilterType.EQ, "source-layers.attrs.id", attr);
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();
        List<String> fields = new ArrayList();
        fields.add("source-layers.$");

        try {

            MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, fields, condition);
            while (cursor.next()) {
                ArrayList<Document> layers = (ArrayList) cursor.get("source-layers");
                for (Document doc : layers) {
                    ArrayList<Document> attrs = (ArrayList) doc.get("attrs");
                    for (Document docAttr : attrs) {
                        if (docAttr.getString("id").equals(attr)) {
                            data.put(docAttr.getString("id"), docAttr.get("attr"));
                        }
                    }
                }
            }
            errcode = 0;
        } catch (Exception e) {
            e.printStackTrace();
            errcode = 1;
            errmsg = e.getMessage();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);
        return buildResponse(output.toJSONString());

    }

//    public void organizationJson(MongoDBCursor cursor, JSONObject json) throws Exception {
//        Map<String, Object> map = cursor.convert();
//        map.remove("_id");
//        map.remove("source-layers");
//        json.putAll(map);
//    }

    public static void main(String args[]) {
        DataSourceService service = new DataSourceService();

        service.getDataSourceUserID("123");


    }
}
