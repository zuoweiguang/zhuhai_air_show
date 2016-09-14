package com.navinfo.mapspotter.process.topic.poihang;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/7.
 */
public class Constants {

    public static Map<String, Double> hang_borderlines = new HashMap();
    public static Map<String, String> targetKindCodeMap = new HashMap();

    public static void initMap() {
        hang_borderlines.put(TENCENT_CP, hang_borderline);
        hang_borderlines.put(BAIDU_CP, baidu_hang_borderline);
        hang_borderlines.put(SOGOU_CP, hang_borderline);
        hang_borderlines.put(SOUFUN_CP, hang_borderline);
        hang_borderlines.put(ELONG_CP, hang_borderline);
        hang_borderlines.put(CTRIP_CP, ctrp_hang_borderline);
        hang_borderlines.put(QUNAR_CP, qunar_hang_borderline);
        hang_borderlines.put(DIANPING_CP, hang_borderline);
        hang_borderlines.put(DEALERSHIP_CP, dealership_hang_borderline);
        hang_borderlines.put(SOGOPARK_CP, sogopark_hang_borderline);
        hang_borderlines.put(BAIDUSELF_CP, baiduself_hang_borderline);
        targetKindCodeMap.put(Constants.DEALERSHIP_CP, Constants.DEALERSHIP_TargetKindCode);
        targetKindCodeMap.put(Constants.SOGOPARK_CP, Constants.SOGOPARK_TargetKindCode);

    }

    /***************CPNAME*****************/
    /**
     * 腾讯
     */
    public static final String TENCENT_CP="tencent";
    /**
     * 百度
     */
    public static final String BAIDU_CP= "baidu";/**
     /**
     * 百度自身数据
     */
    public static final String BAIDUSELF_CP= "baiduself";
    /**
     * 搜狗
     */
    public static final String SOGOU_CP="sogou";
    /**
     * 搜房
     */
    public static final String SOUFUN_CP="soufun";
    /**
     * 艺龙
     */
    public static final String ELONG_CP="elong";

    /**
     * 携程
     */
    public static final String CTRIP_CP="ctrp";
    /**
     * 去哪儿
     */
    public static final String QUNAR_CP="qunar";
    /**
     * 大众点评
     */
    public static final String DIANPING_CP="dianping";
    /**
     * 代理店（秋季代理店）
     */
    public static final String DEALERSHIP_CP="dealership";
    /**
     * 搜狗停车场
     */
    public static final String SOGOPARK_CP="sogoPark";

    /**
     * 代理店（秋季代理店）
     */

    public static final String DEALERSHIP_TargetKindCode="140101,140103,140104,140201,140203,140301,140302,220100,130603,210103";
    /**
     * 搜狗停车场
     */
    public static final String SOGOPARK_TargetKindCode="230210,230211,230212,230213,230214";

    /**
     * 业务下挂阈值
     */
    public static final double hang_borderline = 0.7663246784891400;
    public static final double baidu_hang_borderline = 0.7499999970197677;
    public static final double qunar_hang_borderline = 0.7044805139303207;
    public static final double ctrp_hang_borderline = 0.6322706957935077;
    public static final double baiduself_hang_borderline = 0.7301976226737175;
    public static final double dealership_hang_borderline = 0.7580378185780531;
    public static final double sogopark_hang_borderline = 0.7663246784891400;

    public static final String MR_MAIN_SEPARATOR = "!@;";
    /**
     * IS_GEOCODING
     */
    public static final String IS_GEOCODING="1";
}
