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
public class GeoJsonPolygon {
    private String type = "Polygon";
    private List< List<double[]> > coordinates = new ArrayList<>();

    public GeoJsonPolygon() {
        coordinates.add(new ArrayList<double[]>());
    }

    public String getType() {
        return type;
    }

    public List< List<double[]> > getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(double x, double y) {
        coordinates.get(0).add(new double[] { x, y });
    }

    @Override
    public String toString() {
        try {
            return JsonUtil.getInstance().write2String(this);
        } catch (JsonProcessingException e) {
            Logger.getLogger(GeoJsonPolygon.class).error(e);
            return null;
        }
    }

    public static GeoJsonPolygon parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, GeoJsonPolygon.class);
        } catch (IOException e) {
            Logger.getLogger(GeoJsonPolygon.class).error(e);
            return null;
        }
    }
}
