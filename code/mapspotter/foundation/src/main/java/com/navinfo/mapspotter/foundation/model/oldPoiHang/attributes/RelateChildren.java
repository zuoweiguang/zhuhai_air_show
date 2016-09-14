package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class RelateChildren {

//	private String rowkey;
//	
//	private String pid;

    private int type;

    private String childRowkey;

    private int childPid;

    private String childFid;

    public String getChildFid() {
        return childFid;
    }

    public void setChildFid(String childFid) {
        this.childFid = childFid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getChildRowkey() {
        return childRowkey;
    }

    public void setChildRowkey(String childRowkey) {
        this.childRowkey = childRowkey;
    }

    public int getChildPid() {
        return childPid;
    }

    public void setChildPid(int childPid) {
        this.childPid = childPid;
    }


}
