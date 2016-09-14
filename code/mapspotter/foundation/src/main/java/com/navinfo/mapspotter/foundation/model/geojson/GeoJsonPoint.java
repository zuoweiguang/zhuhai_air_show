package com.navinfo.mapspotter.foundation.model.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.IOException;

/**
 * Created by gaojian on 2016/3/4.
 */
public class GeoJsonPoint {
    private String type = "Point";
    private double[] coordinates = new double[2];

    public GeoJsonPoint() {

    }

    public GeoJsonPoint(double x, double y) {
        coordinates[0] = x;
        coordinates[1] = y;
    }

    public String getType() {
        return type;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double x, double y) {
        coordinates[0] = x;
        coordinates[1] = y;
    }

    @Override
    public String toString() {
        try {
            return JsonUtil.getInstance().write2String(this);
        } catch (JsonProcessingException e) {
            Logger.getLogger(GeoJsonPoint.class).error(e);
            return null;
        }
    }

    public static GeoJsonPoint parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, GeoJsonPoint.class);
        } catch (IOException e) {
            Logger.getLogger(GeoJsonPoint.class).error(e);
            return null;
        }
    }
}
