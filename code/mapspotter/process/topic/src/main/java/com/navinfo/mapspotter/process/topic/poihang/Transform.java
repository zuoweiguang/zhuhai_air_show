package com.navinfo.mapspotter.process.topic.poihang;

import com.navinfo.mapspotter.foundation.model.oldPoiHang.BusinessPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.FastSource;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.RecoPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes.Contact;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes.Poi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONArray;



/**
 * Created by cuiliang on 2016/2/20.
 */
public class Transform {

    static Map<String, String> m = new HashMap();
    static {
        // 华北（北京、天津、河北、山西、内蒙古）、
        m.put("北京", "北京");
        m.put("天津", "天津");
        m.put("河北", "河北");
        m.put("山西", "山西");
        m.put("内蒙古", "内蒙古");
        // 华东（上海、山东、江苏、安徽、江西、浙江、福建、台湾）、
        m.put("上海", "上海");
        m.put("山东", "山东");
        m.put("江苏", "江苏");
        m.put("安徽", "安徽");
        m.put("江西", "江西");
        m.put("浙江", "浙江");
        m.put("福建", "福建");
        m.put("台湾", "台湾");
        // 华中（湖北、湖南、河南）、
        m.put("湖北", "湖北");
        m.put("湖南", "湖南");
        m.put("河南", "河南");
        // 华南（广东、广西、海南、香港、澳门）、
        m.put("广东", "广东");
        m.put("广西", "广西");
        m.put("海南", "海南");
        m.put("香港", "香港");
        m.put("澳门", "澳门");
        // 西南（重庆、四川、贵州、云南、西藏）、
        m.put("重庆", "重庆");
        m.put("四川", "四川");
        m.put("贵州", "贵州");
        m.put("云南", "云南");
        m.put("西藏", "西藏");
        // 西北（陕西、甘肃、宁夏、新疆、青海）、
        m.put("陕西", "陕西");
        m.put("甘肃", "甘肃");
        m.put("宁夏", "宁夏");
        m.put("新疆", "新疆");
        m.put("青海", "青海");
        // 东北（黑龙江、吉林、辽宁）
        m.put("黑龙江", "黑龙江");
        m.put("吉林", "吉林");
        m.put("辽宁", "辽宁");
    }

    public static String getP(String name) {
        if (name == null || name.equals("") || name.length() < 2)
            return "";
        String t = name.substring(0, 2);
        if (m.get(t) != null)
            return t;
        return "";
    }

    public static String getP(Poi poi) {
        String p1 = getP(poi.getAddress());
        return p1;
    }

    public static BusinessPoi makeBusinessPoi(Poi poi) {

        BusinessPoi fastSource;
        fastSource = new BusinessPoi();
        fastSource.setAddr(poi.getAddress());
        fastSource.setName(poi.getName());
        fastSource.setUuid("");

        List<Contact> tels = JSONArray.parseArray(
                JSONArray.toJSONString(poi.getContacts()), Contact.class);

        String tel = "";
        for (Contact c : tels) {
            tel += c.getNumber() + ";";
        }
        tel = tel.replace("+86", "");
        fastSource.setTel(tel);

        fastSource.setKindCode(poi.getKindCode());
        fastSource.setPostCode(poi.getPostCode());
        fastSource.setX(Double.valueOf(poi.getLocation().getLongitude()));
        fastSource.setY(Double.valueOf(poi.getLocation().getLatitude()));
        fastSource.setPid("" + poi.getPid());
        fastSource.setAdminCode(poi.getAdminCode());
        fastSource.setRowkey(poi.getRowkey());
        return fastSource;
    }

    public static RecoPoi makeRecoPoi(Poi poi) {

        RecoPoi recoPoi = new RecoPoi();

        FastSource fastSource;
        fastSource = new FastSource();
        fastSource.setAddr(poi.getAddress());
        fastSource.setName(poi.getName());
        fastSource.setUuid("");

        List<Contact> tels = JSONArray.parseArray(
                JSONArray.toJSONString(poi.getContacts()), Contact.class);

        String tel = "";
        for (Contact c : tels) {
            tel += c.getNumber() + ";";
        }
        tel = tel.replace("+86", "");
        fastSource.setTel(tel);

        fastSource.setKindCode(poi.getKindCode());
        fastSource.setPostCode(poi.getPostCode());
        fastSource.setX(Double.valueOf(poi.getLocation().getLongitude()));
        fastSource.setY(Double.valueOf(poi.getLocation().getLatitude()));
        fastSource.setPid("" + poi.getPid());
        recoPoi.setRowkey("" + poi.getPid());
        recoPoi.getSources().add(fastSource);
        return recoPoi;
    }

    private static FastSource makeFastSource(Poi poi) {
        FastSource fastSource;
        fastSource = new FastSource();
        fastSource.setAddr(poi.getAddress());
        fastSource.setName(poi.getName());
        fastSource.setUuid("");
        fastSource.setRowkey(poi.getRowkey());
        List<Contact> tels = JSONArray.parseArray(
                JSONArray.toJSONString(poi.getContacts()), Contact.class);

        String tel = "";
        for (Contact c : tels) {
            tel += c.getNumber() + ";";
        }
        tel = tel.replace("+86", "");
        fastSource.setTel(tel);

        fastSource.setKindCode(poi.getKindCode());
        fastSource.setPostCode(poi.getPostCode());
        fastSource.setX(Double.valueOf(poi.getLocation().getLongitude()));
        fastSource.setY(Double.valueOf(poi.getLocation().getLatitude()));

        fastSource.setProvnm(getP(poi));
        return fastSource;
    }

//    public static FastSource makeFastSource(Source source) {
//        FastSource fastSource;
//        fastSource = new FastSource();
//        fastSource.setAddr(source.getMatchs().getMatchs_address());
//        fastSource.setName(source.getMatchs().getMatchs_name());
//        fastSource.setUuid(source.getOriginalContent().getString("uuid"));
//        fastSource.setTel(source.getMatchs().getMatchs_telephone());
//        fastSource.setPostCode(source.getMatchs().getMatchs_postcode());
//        fastSource.setX(Double
//                .valueOf(source.getMatchs().getMatchs_longitude()));
//        fastSource
//                .setY(Double.valueOf(source.getMatchs().getMatchs_latitude()));
//        fastSource.setGcoding(source.getMatchs().getIsGeocoding());
//        fastSource.setPid(source.getMatchs().getNiPid());
//        fastSource.setKindCode(source.getMatchs().getMatchs_kindcode());
//        fastSource.setRowkey(source.getRowkey());
//        if (source.getCpName().equals(SourceConstant.DEALERSHIP_CP))
//            fastSource.setProvnm(getP(source.getOriginalContent().getString(
//                    "province")));
//        return fastSource;
//    }
}
