package com.navinfo.mapspotter.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.IOException;

/**
 * POI统计结果模型
 * Created by gaojian on 2016/1/26.
 */
public class PoiStatResult {
    private String pid = "";
    private double lon = 0.0;
    private double lat = 0.0;
    private int count = 0;
    private String name = "";
    private String addr = "";
    private String tel = "";
    private String mesh = "";
    private String kind = "";
    private String kindName = "";
    private String kindMed = "";
    private String kindTop = "";
    private String admin = "";
    private String adminName = "";
    private String city = "";
    private String province = "";

    @JsonProperty("pid")
    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("addr")
    public String getAddr() {
        return addr;
    }
    public void setAddr(String addr) {
        this.addr = addr;
    }

    @JsonProperty("tel")
    public String getTel() {
        return tel;
    }
    public void setTel(String tel) {
        this.tel = tel;
    }

    @JsonProperty("mesh")
    public String getMesh() {
        return mesh;
    }
    public void setMesh(String mesh) {
        this.mesh = mesh;
    }

    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }

    @JsonProperty("kindName")
    public String getKindName() {
        return kindName;
    }
    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    @JsonProperty("kindMed")
    public String getKindMed() {
        return kindMed;
    }
    public void setKindMed(String kindMed) {
        this.kindMed = kindMed;
    }

    @JsonProperty("kindTop")
    public String getKindTop() {
        return kindTop;
    }
    public void setKindTop(String kindTop) {
        this.kindTop = kindTop;
    }

    @JsonProperty("admin")
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }

    @JsonProperty("adminName")
    public String getAdminName() {
        return adminName;
    }
    public void setAdminName(String adminName) {
        this.adminName = adminName;
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

    @Override
    public String toString() {
        try {
            return JsonUtil.getInstance().write2String(this);
        } catch (JsonProcessingException e) {
            Logger.getLogger(PoiStatResult.class).error(e);
            return null;
        }
    }

    public static PoiStatResult parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, PoiStatResult.class);
        } catch (IOException e) {
            Logger.getLogger(PoiStatResult.class).error(e);
            return null;
        }
    }
}
