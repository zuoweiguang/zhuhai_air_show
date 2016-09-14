package com.navinfo.mapspotter.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.IOException;

/**
 * Poi热力图点模型
 *
 * Created by gaojian on 2016/2/1.
 */
public class PoiHeatMap {
    private int level = 0;
    private double lon = 0.0;
    private double lat = 0.0;
    private int count = 0;

    @JsonProperty("level")
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    @JsonProperty("lon")
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    @JsonProperty("count")
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        try {
            return JsonUtil.getInstance().write2String(this);
        } catch (JsonProcessingException e) {
            Logger.getLogger(PoiHeatMap.class).error(e);
            return null;
        }
    }

    public static PoiHeatMap parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, PoiHeatMap.class);
        } catch (IOException e) {
            Logger.getLogger(PoiHeatMap.class).error(e);
            return null;
        }
    }
}
