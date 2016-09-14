package com.navinfo.mapspotter.warehouse;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.process.storage.vectortile.ProtobufVisitor;
import com.navinfo.mapspotter.warehouse.connection.DBPool;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by SongHuiXing on 6/7 0007.
 */
@Path("/view")
public class ViewService {

    private ProtobufVisitor visitor = new ProtobufVisitor(DBPool.getInstance().getMongo(), DBPool.getInstance().getPostGIS());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is view service";
    }

    @GET
    @Path("/road/{z}/{x}/{y}")
    public Response getRoad(@PathParam("x") int x,
                            @PathParam("y") int y,
                            @PathParam("z") int z) {
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Road);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/admin/{z}/{x}/{y}")
    public Response getAdmin(@PathParam("x") int x,
                             @PathParam("y") int y,
                             @PathParam("z") int z) {
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Admin);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/background/{z}/{x}/{y}")
    public Response getBackground(@PathParam("x") int x,
                                  @PathParam("y") int y,
                                  @PathParam("z") int z) {
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Background);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/poi/{z}/{x}/{y}")
    public Response getPoi(@PathParam("x") int x,
                           @PathParam("y") int y,
                           @PathParam("z") int z) {
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Poi);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/poi/heatmap/month/{z}/{x}/{y}")
    public Response getPoiMonthHeatData(@PathParam("x") int x,
                                        @PathParam("y") int y,
                                        @PathParam("z") int z){

        return buildResponse(new byte[0]);
    }

    @GET
    @Path("/ms/{z}/{x}/{y}")
    public Response getDigConstruction(@PathParam("x") int x,
                                       @PathParam("y") int y,
                                       @PathParam("z") int z) {
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Dig);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/infomation/{z}/{x}/{y}")
    public Response getInformation(@PathParam("x") int x,
                                   @PathParam("y") int y,
                                   @PathParam("z") int z,
                                   @QueryParam("condition") String condition) {
//        http://localhost:8080/rest/view/infomation/14/13894/5968?condition=null
        byte[] targetPbf = visitor.getInformation(z, x, y, WarehouseDataType.SourceType.Information, condition);
        return buildResponse(targetPbf);
    }

    @GET
    @Path("/poiHeatMap/{z}/{x}/{y}")
    public Response getPoiHeatMap(@PathParam("x") int x,
                                   @PathParam("y") int y,
                                   @PathParam("z") int z,
                                   @QueryParam("condition") String condition) {
        byte[] targetPbf = visitor.getPoiHeatMapProtobuf(z, x, y, WarehouseDataType.SourceType.PoiHeatMap);
        return buildResponse(targetPbf);
    }

    @GET
    @Path("/block/{z}/{x}/{y}")
    public Response getBlockHistory(@PathParam("x") int x,
                                    @PathParam("y") int y,
                                    @PathParam("z") int z){
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Block);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/traffic/{z}/{x}/{y}")
    public Response getTrafficStatus(@PathParam("x") int x,
                                    @PathParam("y") int y,
                                    @PathParam("z") int z){
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.Traffic);

        return buildResponse(targetPbf);
    }

    @GET
    @Path("/baotoutraffic/{z}/{x}/{y}")
    public Response getBaoTouTrafficStatus(@PathParam("x") int x,
                                         @PathParam("y") int y,
                                         @PathParam("z") int z){
        byte[] targetPbf = visitor.getProtobuf(z, x, y, WarehouseDataType.SourceType.BaoTouTraffic);

        return buildResponse(targetPbf);
    }


    private Response buildResponse(byte[] pbf) {
        Response.ResponseBuilder builder;

        try {
            if (null != pbf && pbf.length > 0) {
                builder = Response.ok().entity(pbf);
            } else {
                builder = Response.noContent();
            }
        } catch (Exception e) {
            builder = Response.status(404);
        }

        builder.header("Access-Control-Allow-Origin", "*");
        builder.header("Access-Control-Allow-Methods", "GET");

        return builder.build();
    }
}
