package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by cuiliang on 2016/6/15.
 */
@Path("/solution")
public class SolutionService extends ServiceAbstract {

    public static String table_name = "solution";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is solution service";
    }

    @GET
    @Path("{id}")
    @Produces("text/plain;charset=UTF-8")
    public Response getSolutionByID(@PathParam("id") String id) {

        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        data = new JSONObject();

        try {
            MongoOperator soluCond = new MongoOperator();
            soluCond.and(MongoOperator.FilterType.EQ, "id", id);
            JSONObject solu = queryJsonObject(table_name, soluCond);
            MongoOperator layerCond = new MongoOperator();
            layerCond.and(MongoOperator.FilterType.EQ, "soluID", id);
            JSONArray layers = queryJsonArray("layer", layerCond, "zindex");
            solu.put("layers", layers);
            data.put(table_name, solu);
            errcode = 0;
        } catch (Exception e) {
            errcode = 1;
            errmsg = e.toString();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", data);

        return buildResponse(output.toJSONString());
    }

    @GET
    @Path("list/{userID}")
    @Produces("text/plain;charset=UTF-8")
    public Response getSolutionByUser(@PathParam("userID") String userID) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "userID", userID);
        return buildResponse(queryList(table_name, condition));
    }


    @POST
    @Path("add")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response addSolution(String json) {
        return buildResponse(add(table_name, json));
    }


    @POST
    @Path("update")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response updateSolution(String json) {
        return buildResponse(update(table_name, json));
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response delSolution(String json) {

        Document inputJson = Document.parse(json);
        String id = inputJson.getString("id");

        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);

        MongoOperator layerCond = new MongoOperator();
        layerCond.and(MongoOperator.FilterType.EQ, "soluID", id);
        delete("layer", layerCond);

        return buildResponse(delete(table_name, condition));
    }


    public static void main(String[] args) {
        SolutionService ss = new SolutionService();
    }
}
