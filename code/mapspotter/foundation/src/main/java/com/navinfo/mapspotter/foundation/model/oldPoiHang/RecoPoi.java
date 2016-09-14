package com.navinfo.mapspotter.foundation.model.oldPoiHang;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;


/**
 * @author wangxiaojian
 *         推荐mr 瘦身poi
 */
public class RecoPoi {
    private long itemID;
    private String rowkey;
    List<FastSource> sources = new ArrayList();

    public long getItemID() {
        return itemID;
    }

    public void setItemID(long itemID) {
        this.itemID = itemID;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public List<FastSource> getSources() {
        return sources;
    }

    public void setSources(List<FastSource> sources) {
        this.sources = sources;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(this);
        return jsonObject;
    }
}
