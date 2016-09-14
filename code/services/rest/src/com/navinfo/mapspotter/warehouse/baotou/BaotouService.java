package com.navinfo.mapspotter.warehouse.baotou;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by zuoweiguang on 2016/8/10.
 */
@Path("/baotou")
public class BaotouService extends ServiceAbstract {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is baotou view";
    }

    @GET
    @Path("/trafficevents")
    @Produces("text/plain;charset=UTF-8")
    public Response getTafficEvents() {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        JSONArray jsonArray = new JSONArray();

        String traffic_events = "traffic_events";
        try {
            JSONArray trafficevents = queryJsonArray(traffic_events);
            for (int i = 0; i < trafficevents.size(); i++) {
                jsonArray.add(trafficevents.get(i));
            }
        } catch (Exception e) {
            errcode = -1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", jsonArray);
        return buildResponse(output.toJSONString());
    }

    @GET
    @Path("/eventsplaces")
    @Produces("text/plain;charset=UTF-8")
    public Response getEventsPlaces() {
        output = new JSONObject();
        errcode = 1;
        errmsg = "";
        JSONArray jsonArray = new JSONArray();

        String baotoueventsplaces = "baotoueventsplaces";
        try {
            JSONArray eventsplaces = queryJsonArray(baotoueventsplaces);
            for (int i = 0; i < eventsplaces.size(); i++) {
                jsonArray.add(eventsplaces.get(i));
            }
        } catch (Exception e) {
            errcode = -1;
            errmsg = e.getMessage();
            e.printStackTrace();
        }
        output.put("errcode", errcode);
        output.put("errmsg", errmsg);
        output.put("data", jsonArray);
        return buildResponse(output.toJSONString());
    }

}
