package com.navinfo.mapspotter.foundation.model.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaojian on 2016/3/4.
 */
public class GeoJsonLineString {
    private String type = "LineString";
    private List<double[]> coordinates = new ArrayList<>();

    public String getType() {
        return type;
    }

    public List<double[]> getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(double x, double y) {
        coordinates.add(new double[] { x, y });
    }

    @Override
    public String toString() {
        try {
            return JsonUtil.getInstance().write2String(this);
        } catch (JsonProcessingException e) {
            Logger.getLogger(GeoJsonLineString.class).error(e);
            return null;
        }
    }

    public static GeoJsonLineString parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, GeoJsonLineString.class);
        } catch (IOException e) {
            Logger.getLogger(GeoJsonLineString.class).error(e);
            return null;
        }
    }
}
