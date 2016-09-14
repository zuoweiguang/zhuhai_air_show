package com.navinfo.mapspotter.foundation.model;

/**
 * 行政区划对象
 *
 * Created by gaojian on 2016/1/26.
 */
public class AdminBlock {
    private String code;
    private String town;
    private String city;
    private String province;

    public AdminBlock(String code, String town, String city, String province) {
        this.code = code;
        this.town = town;
        this.city = city;
        this.province = province;
    }

    public String getCode() {
        return code;
    }

    public String getTown() {
        return town;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }
}
