package com.navinfo.mapspotter.warehouse.manager;

import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.warehouse.connection.DBPool;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by cuiliang on 2016/6/17.
 */
@Path("/layer")
public class LayerService extends ServiceAbstract {

    public static String table_name = "layer";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is layer service";
    }

    @POST
    @Path("add")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response addLayer(String json) {
        return buildResponse(add(table_name, json));
    }


    @POST
    @Path("update")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response updateLayer(String json) {
        return buildResponse(update(table_name, json));
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response delLayer(String json) {
        Document inputJson = Document.parse(json);
        String id = inputJson.getString("id");
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(delete(table_name, condition));
    }

    @GET
    @Path("list/{soluID}")
    @Produces("text/plain;charset=UTF-8")
    public Response getLayerBySoluID(@PathParam("soluID") String soluID) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "soluID", soluID);
        return buildResponse(queryList(table_name, condition, "zindex"));
    }

    @GET
    @Path("/{id}")
    @Produces("text/plain;charset=UTF-8")
    public Response getLayerByID(@PathParam("id") String id) {
        MongoOperator condition = new MongoOperator();
        condition.and(MongoOperator.FilterType.EQ, "id", id);
        return buildResponse(query(table_name, condition));
    }

    public static void main(String args[]) {
        DBPool.getInstance().initDatabaseConnections();
        LayerService service = new LayerService();
        service.addLayer("{\"name\":\"道路层\",\"type\":\"line\",\"sourceID\":\"1a2b3130-860d-4a02-8276-821a478d7d38\",\"source\":\"road\",\"layout\":{\"line-cap\":\"round\",\"line-join\":\"round\",\"visibility\":\"visible\"},\"paint\":{\"line-color\":\"#ff0000\",\"line-width\":{\"base\":1.2,\"stops\":[[5,0.1],[6,0.2],[7,1.5],[20,18]]}},\"filter\":[],\"desc\":\"道路\",\"source-layer\":\"Road\",\"maxzoom\":17,\"minzoom\":3,\"interactive\":true,\"soluID\":\"be206647-52e2-4847-a542-28d699b42964\"}");
    }
}
