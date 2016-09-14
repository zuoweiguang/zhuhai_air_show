package com.navinfo.mapspotter.warehouse.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cuiliang on 2016/6/17.
 */
@XmlRootElement
public class OutputBean {

    public String errcode;

    public String errmsg;

    public DataBean data;

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }
    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    @XmlElement
    public DataBean getData() {
        return data;
    }
    public void setData(DataBean data) {
        this.data = data;
    }
}
