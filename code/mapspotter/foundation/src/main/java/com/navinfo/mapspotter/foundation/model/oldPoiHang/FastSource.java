package com.navinfo.mapspotter.foundation.model.oldPoiHang;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by cuiliang on 2016/2/19.
 */
public class FastSource extends BusinessPoi {
    public String makeCpName() {
        if (this.getRowkey() == null || this.getRowkey().equals(""))
            return null;
        String a[] = this.getRowkey().split("_");
        return a[0];
    }

    private long itemID = 0;
    private String gcoding = "0"; // 是否geocoding 0:不使用geocoding

    private int relatedStatus;
    private String confirmDate;

    public long getItemID() {
        return itemID;
    }

    public void setItemID(long itemID) {
        this.itemID = itemID;
    }

    public String getGcoding() {
        return gcoding;
    }

    public void setGcoding(String gcoding) {
        this.gcoding = gcoding;
    }

    public int getRelatedStatus() {
        return relatedStatus;
    }

    public void setRelatedStatus(int relatedStatus) {
        this.relatedStatus = relatedStatus;
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    private String remark;


    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(this);
        return jsonObject;
    }

}
