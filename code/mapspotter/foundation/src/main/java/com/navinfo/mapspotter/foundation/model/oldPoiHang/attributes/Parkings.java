package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class Parkings {

    private String tollStd;

    private String tollDes;

    private String tollWay;

    private String openTime;

    private int totalNum;

    private String payment;

    private String remark;

    private String buildingType;

    public String getTollStd() {
        return tollStd;
    }

    public void setTollStd(String tollStd) {
        this.tollStd = tollStd;
    }

    public String getTollDes() {
        return tollDes;
    }

    public void setTollDes(String tollDes) {
        this.tollDes = tollDes;
    }

    public String getTollWay() {
        return tollWay;
    }

    public void setTollWay(String tollWay) {
        this.tollWay = tollWay;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }


    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }
}
