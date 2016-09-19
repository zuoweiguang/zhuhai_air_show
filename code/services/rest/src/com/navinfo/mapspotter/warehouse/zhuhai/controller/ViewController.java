package com.navinfo.mapspotter.warehouse.zhuhai.controller;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.warehouse.zhuhai.service.ViewService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zuoweiguang on 2016/9/7.
 */

@Path("/controller")
public class ViewController {

    private ViewService viewService = ViewService.getInstance();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is zhuhai controller";
    }

    @GET
    @Path("/trafficevents/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getTafficEvents(@PathParam("x") int x,
                                    @PathParam("y") int y,
                                    @PathParam("z") int z) {
        byte[] result = viewService.getTrafficEvent(z, x, y);
        return buildResponse(result);
    }

    @GET
    @Path("/forecast_halfhour/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getForecastsHalfhour(@PathParam("x") int x,
                                         @PathParam("y") int y,
                                         @PathParam("z") int z) {
        byte[] result = viewService.getForecasts(z, x, y, "halfhour");
        return buildResponse(result);
    }

    @GET
    @Path("/forecast_onehour/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getForecastsOnehour(@PathParam("x") int x,
                                        @PathParam("y") int y,
                                        @PathParam("z") int z) {
        byte[] result = viewService.getForecasts(z, x, y, "onehour");
        return buildResponse(result);
    }

    @GET
    @Path("/traffic/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getTraffic(@PathParam("x") int x,
                               @PathParam("y") int y,
                               @PathParam("z") int z) {
        byte[] result = viewService.getTraffic(z, x, y);
        return buildResponse(result);
    }

    @GET
    @Path("/staff/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getStaff(@PathParam("x") int x,
                             @PathParam("y") int y,
                             @PathParam("z") int z) {
        byte[] result = viewService.getStaff(z, x, y);
        return buildResponse(result);
    }

    @POST
    @Path("/staffRegister")
    @Produces("text/plain;charset=UTF-8")
    public Response addStaff(@FormParam("mobile_phone") String mobile_phone,
                             @FormParam("password") String password,
                             @FormParam("confirm_password") String confirm_password,
                             @FormParam("user_name") String user_name,
                             @FormParam("user_type") int user_type,
                             @FormParam("id_card") String id_card,
                             @FormParam("sex") int sex,
                             @FormParam("age") int age,
                             @FormParam("address") String address) {
        String result = viewService.addStaff(mobile_phone, password, confirm_password, user_name,
                user_type, id_card, sex, age, address);
        return buildResponse(result);
    }

    @GET
    @Path("/staffUploadLocation/{mobile_phone}/{lon}/{lat}")
    @Produces("text/plain;charset=UTF-8")
    public Response staffUploadLocation(@PathParam("mobile_phone") String mobile_phone,
                                        @PathParam("lon") double lon,
                                        @PathParam("lat") double lat) {
        int result = viewService.staffUploadLocation(mobile_phone, lon, lat);
        return buildResponse(result);
    }


    @GET
    @Path("/parking/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getParking(@PathParam("x") int x,
                               @PathParam("y") int y,
                               @PathParam("z") int z) {
        byte[] result = viewService.getParking(z, x, y);
        return buildResponse(result);
    }

    @GET
    @Path("/bus/{z}/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public Response getBus(@PathParam("x") int x,
                           @PathParam("y") int y,
                           @PathParam("z") int z) {
        byte[] result = null;
        return buildResponse(result);
    }


    private Response buildResponse(int result) {
        Response.ResponseBuilder builder;
        builder = Response.ok().entity(result);
        builder.header("Access-Control-Allow-Origin", "*");
        builder.header("Access-Control-Allow-Methods", "GET");

        return builder.build();
    }

    private Response buildResponse(String result) {
        Response.ResponseBuilder builder;
        builder = Response.ok().entity(result);
        builder.header("Access-Control-Allow-Origin", "*");
        builder.header("Access-Control-Allow-Methods", "GET");

        return builder.build();
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

    private Response buildResponse(JSONObject list) {
        Response.ResponseBuilder builder;

        try {
            if (null != list && list.size() > 0) {
                builder = Response.ok().entity(list.toString());
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
