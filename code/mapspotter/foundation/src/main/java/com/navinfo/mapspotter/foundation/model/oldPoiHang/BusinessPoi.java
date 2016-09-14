package com.navinfo.mapspotter.foundation.model.oldPoiHang;

import com.alibaba.fastjson.JSONObject;
/**
 * Created by cuiliang on 2016/2/19.
 */
public class BusinessPoi {

    private String uuid="";
    private String pid = "";// -- 四维pid

    private String name="";
    private String addr="";
    /**
     * Longitude
     */
    private Double x=0.0;
    /**
     * Latitude
     */
    private Double y=0.0;
    private String tel="";
    private String postCode="";
    private String kindCode = "";
    private String rowkey;
    private String provnm="";
    public String getProvnm() {
        return provnm;
    }
    public void setProvnm(String provnm) {
        this.provnm = provnm;
    }
    public String getRowkey() {
        return rowkey;
    }
    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getKindCode() {
        return kindCode;
    }
    public void setKindCode(String kindCode) {
        this.kindCode = kindCode;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddr() {
        return addr;
    }
    public void setAddr(String addr) {
        this.addr = addr;
    }
    public Double getX() {
        return x;
    }
    public void setX(Double x) {
        this.x = x;
    }
    public Double getY() {
        return y;
    }
    public void setY(Double y) {
        this.y = y;
    }
    public String getTel() {
        return tel;
    }
    public void setTel(String tel) {
        this.tel = tel;
    }
    public String getPostCode() {
        return postCode;
    }
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
    //	public String getType() {
//		return type;
//	}
//	public void setType(String type) {
//		this.type = type;
//	}
    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }

    private String adminCode="";

    public String getAdminCode() {
        return adminCode;
    }
    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }
    public JSONObject toJson() {
        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(this);
        return jsonObject;
    }
}
