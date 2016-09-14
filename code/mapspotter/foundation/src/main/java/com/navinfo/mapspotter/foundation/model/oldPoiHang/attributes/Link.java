package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

import java.util.List;

public class Link {

    private String rowkey;

    private String geometry;

    private int pid;

    private String fid;

    private String meshid;

    private int kind;

    private int direct;

    private int appInfo;

    private int tollInfo;

    private int multiDigitized;

    private int specialTraffic;

    private int fc;

    private int laneNum;

    private int laneLeft;

    private int laneRight;

    private int isViaduct;

    private int paveStatus;

    private String name;

    private String adminCodeLeft;

    private String adminCodeRight;

    private List<Forms> forms;


    public String getAdminCodeLeft() {
        return adminCodeLeft;
    }

    public void setAdminCodeLeft(String adminCodeLeft) {
        this.adminCodeLeft = adminCodeLeft;
    }

    public String getAdminCodeRight() {
        return adminCodeRight;
    }

    public void setAdminCodeRight(String adminCodeRight) {
        this.adminCodeRight = adminCodeRight;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getMeshid() {
        return meshid;
    }

    public void setMeshid(String meshid) {
        this.meshid = meshid;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

    public int getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(int appInfo) {
        this.appInfo = appInfo;
    }

    public int getTollInfo() {
        return tollInfo;
    }

    public void setTollInfo(int tollInfo) {
        this.tollInfo = tollInfo;
    }

    public int getMultiDigitized() {
        return multiDigitized;
    }

    public void setMultiDigitized(int multiDigitized) {
        this.multiDigitized = multiDigitized;
    }

    public int getSpecialTraffic() {
        return specialTraffic;
    }

    public void setSpecialTraffic(int specialTraffic) {
        this.specialTraffic = specialTraffic;
    }

    public int getFc() {
        return fc;
    }

    public void setFc(int fc) {
        this.fc = fc;
    }

    public int getLaneNum() {
        return laneNum;
    }

    public void setLaneNum(int laneNum) {
        this.laneNum = laneNum;
    }

    public int getLaneLeft() {
        return laneLeft;
    }

    public void setLaneLeft(int laneLeft) {
        this.laneLeft = laneLeft;
    }

    public int getLaneRight() {
        return laneRight;
    }

    public void setLaneRight(int laneRight) {
        this.laneRight = laneRight;
    }

    public int getIsViaduct() {
        return isViaduct;
    }

    public void setIsViaduct(int isViaduct) {
        this.isViaduct = isViaduct;
    }

    public int getPaveStatus() {
        return paveStatus;
    }

    public void setPaveStatus(int paveStatus) {
        this.paveStatus = paveStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Forms> getForms() {
        return forms;
    }

    public void setForms(List<Forms> forms) {
        this.forms = forms;
    }


}
