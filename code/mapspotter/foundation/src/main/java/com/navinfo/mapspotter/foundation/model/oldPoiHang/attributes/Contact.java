package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class Contact {

    private String number;

    private String linkman;

    private int priority;

    private int type;

    private String weChatUrl;

    public String getWeChatUrl() {
        return weChatUrl;
    }

    public void setWeChatUrl(String weChatUrl) {
        this.weChatUrl = weChatUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        this.linkman = linkman;
    }

}
