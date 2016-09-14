package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.MongoDBCursor;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiliang on 2016/6/18.
 */
@Path("/style")
public class StyleService extends ServiceAbstract {

    public static String table_name = "style";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is layer dataSource";
    }

    @GET
    @Path("default")
    @Produces("text/plain;charset=UTF-8")
    public Response getDefaultStyle(@QueryParam("source") String sourcename,
                                  @QueryParam("srclayer") String sourcelyr){
        String styleJson = getDefaultStyles(sourcename, sourcelyr);

        if(null != styleJson){
            return buildResponse(styleJson);
        } else {
            return Response.noContent().build();
        }
    }


    @POST
    @Path("add")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response addStyle(String json) {
        return buildResponse(add(table_name, json));
    }


    @PUT
    @Path("update")
    @Produces("text/plain;charset=UTF-8")
    public Response modStyle(@FormParam("id") String id,
                             @FormParam("name") String name,
                             @FormParam("paint") String paint,
                             @FormParam("layout") String layout) {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);

        try {
            Map style = new HashMap();
            style.put("name", name);
            style.put("paint", paint);
            style.put("layout", layout);
            database.update(table_name, condition, style);
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
    public Response updateStyle(String json) {
        return buildResponse(update(table_name, json));
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response delStyle(String json) {

        Document inputJson = Document.parse(json);
        String id = inputJson.getString("id");

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(delete(table_name, condition));
    }

    @GET
    @Path("/{id}")
    @Produces("text/plain;charset=UTF-8")
    public Response getStyleByID(@PathParam("id") String id) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(query(table_name, condition));
    }

    private String getDefaultStyles(String source, String layer){
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "source", source);
        condition.and(MongoOperator.FilterType.EQ, "source_layer", layer);

        MongoDBCursor cursor = (MongoDBCursor) database.query(table_name, condition);
        while (cursor.next()) {
            try {
                Map<String, String> content = cursor.convert();

                return content.get("styles");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
