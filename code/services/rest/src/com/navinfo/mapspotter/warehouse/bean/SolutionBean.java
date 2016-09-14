package com.navinfo.mapspotter.warehouse.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cuiliang on 2016/6/17.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SolutionBean implements DataBean{
    public String id;
    public String name;
    public String desc;
    public String userID;

    public SolutionBean() {

    }

    public SolutionBean(String id, String name, String desc, String userID) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @XmlElement(name="my-name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
