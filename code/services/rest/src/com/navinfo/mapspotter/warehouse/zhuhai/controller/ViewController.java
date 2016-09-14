package com.navinfo.mapspotter.warehouse.zhuhai.controller;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.warehouse.zhuhai.service.ViewService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
