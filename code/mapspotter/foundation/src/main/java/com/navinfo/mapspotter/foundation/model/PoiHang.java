package com.navinfo.mapspotter.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.IOException;
import java.io.Serializable;

/**
 * POI下挂使用的模型
 * Created by gaojian on 2016/1/24.
 */
public class PoiHang implements Serializable {
    // POI属性
    private String pid = "";
    private double lon = 0.0;
    private double lat = 0.0;
    private String name = "";
    private String addr = "";
    private String tel = "";
    private String kind = "";
    private String admin = "";
    private String mesh = "";

    // 原始字段
    private double oLon = 0.0;
    private double oLat = 0.0;
    private String oName = "";
    private String oAddr = "";
    private String oTel = "";
    private int count = 0;

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
    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }
    @JsonProperty("admin")
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    @JsonProperty("mesh")
    public String getMesh() {
        return mesh;
    }
    public void setMesh(String mesh) {
        this.mesh = mesh;
    }
    @JsonProperty("oLon")
    public double getoLon() {
        return oLon;
    }
    public void setoLon(double oLon) {
        this.oLon = oLon;
    }
    @JsonProperty("oLat")
    public double getoLat() {
        return oLat;
    }
    public void setoLat(double oLat) {
        this.oLat = oLat;
    }
    @JsonProperty("oName")
    public String getoName() {
        return oName;
    }
    public void setoName(String oName) {
        this.oName = oName;
    }
    @JsonProperty("oAddr")
    public String getoAddr() {
        return oAddr;
    }
    public void setoAddr(String oAddr) {
        this.oAddr = oAddr;
    }
    @JsonProperty("oTel")
    public String getoTel() {
        return oTel;
    }
    public void setoTel(String oTel) {
        this.oTel = oTel;
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
            Logger.getLogger(PoiHang.class).error(e);
            return null;
        }
    }

    public static PoiHang parse(String jsonStr) {
        try {
            return JsonUtil.getInstance().readValue(jsonStr, PoiHang.class);
        } catch (IOException e) {
            Logger.getLogger(PoiHang.class).error(e);
            return null;
        }
    }
}
