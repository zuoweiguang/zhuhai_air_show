package com.navinfo.mapspotter.process.analysis.poistat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;

import java.io.IOException;

/**
 * Created by gaojian on 2016/2/2.
 */
public class AreaCountResult {
    private String areaId = null;
    private String town = null;
    private String city = null;
    private String province = null;
    private int count = 0;

    @JsonProperty("areaId")
    public String getAreaId() {
        return areaId;
    }
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    @JsonProperty("town")
    public String getTown() {
        return town;
    }
    public void setTown(String town) {
        this.town = town;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("province")
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
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
            return null;
        }
    }

    public static AreaCountResult parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, AreaCountResult.class);
        } catch (IOException e) {
            return null;
        }
    }
}
