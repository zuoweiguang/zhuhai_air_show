package com.navinfo.mapspotter.process.analysis.poistat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;

/**
 * Created by ZhangJin1207 on 2016/4/6.
 */
public class BlockInfo{
    String blockid;
    String province;
    String city;
    String county;
    String area;
    String wkt;
    Geometry geom;
    int count;

    @JsonProperty("blockid")
    public String getBlockid() {
        return blockid;
    }
    public void setBlockid(String blockid) {

        this.blockid = blockid;
    }

    @JsonProperty("province")
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("county")
    public String getCounty() {
        return county;
    }
    public void setCounty(String county) {
        this.county = county;
    }

    @JsonProperty("area")
    public String getArea(){
        return area;
    }
    public void setArea (String area){
        this.area = area;
    }

    @JsonProperty("wkt")
    public String getWkt(){return wkt;}
    public void setWkt(String wkt){this.wkt = wkt;}

    @JsonProperty("count")
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public Geometry getGeom() {
        return geom;
    }
    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
    public void add(int count){
        this.count += count;
    }

    @Override
    public String toString(){
        try {
            return JsonUtil.getInstance().write2String(this);
        }catch (JsonProcessingException e){
            return null;
        }
    }

    public static BlockInfo parse(String strJson){
        try{
            return JsonUtil.getInstance().readValue(strJson ,BlockInfo.class);
        }catch (IOException e){
            return null;
        }
    }
}
