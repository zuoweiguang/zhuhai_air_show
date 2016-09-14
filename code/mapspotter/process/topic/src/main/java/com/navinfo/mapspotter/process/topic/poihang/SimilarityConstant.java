package com.navinfo.mapspotter.process.topic.poihang;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by cuiliang on 2016/2/20.
 */
public class SimilarityConstant {

    /**
     * 距离权重
     */
    private static double r1Default = 0.2;

    /**
     * 名称权重
     */
    private static double r2Default = 0.3;

    /**
     * 地址权重
     */
    private static double r3Default = 0.27;

    /**
     * 邮编权重
     */
    private static double r4Default = 0.03;

    /**
     * 电话权重
     */
    private static double r5Default = 0.2;

    /**
     * 最小权重值
     */
    private static double minWeightDefault = 0.3;

    /**
     * 业务下挂阈值
     */
    private static final double hang_borderline = 0.7663246784891400;
    private static final double baidu_hang_borderline = 0.7499999970197677;
    private static final double qunar_hang_borderline = 0.7044805139303207;
    private static final double ctrp_hang_borderline = 0.6322706957935077;
    private static final double baiduself_hang_borderline = 0.7301976226737175;
    private static final double dealership_hang_borderline = 0.7580378185780531;
    private static final double sogopark_hang_borderline = 0.7663246784891400;


    public static Map<String, Map<String, Double>> similarityMap;


    public static void initSimilarityMap(String similarityJson) {
        String cpName = Constants.BAIDU_CP;
        String r1 = null;
        String r2 = null;
        String r3 = null;
        String r4 = null;
        String r5 = null;
        String minWeight = null;
        String hangBorderLine = null;
        double r1_temp = r1Default;
        double r2_temp = r2Default;
        double r3_temp = r3Default;
        double r4_temp = r4Default;
        double r5_temp = r5Default;
        double minWeight_temp = minWeightDefault;
        double hangBorderLine_temp = hang_borderline;

        Map<String, Map<String, Double>> simMap = new HashMap<String, Map<String, Double>>();

        //设置默认simiMap
        Map<String, Double> defaultMap = new HashMap<String, Double>();
        defaultMap.put("r1", r1_temp);
        defaultMap.put("r2", r2_temp);
        defaultMap.put("r3", r3_temp);
        defaultMap.put("r4", r4_temp);
        defaultMap.put("r5", r5_temp);
        defaultMap.put("minWeight", minWeight_temp);
        defaultMap.put("hangBorderLine", hangBorderLine_temp);
        simMap.put(cpName, defaultMap);


        JSONArray fromObject = JSONArray.parseArray(similarityJson);


        if (fromObject != null && fromObject.size() > 0) {
            for (int i = 0; i < fromObject.size(); i++) {
                JSONObject obj = (JSONObject) fromObject.get(i);
                cpName = (obj != null && obj.getString("cpName") != null) ? obj.getString("cpName") : Constants.BAIDU_CP;
                r1 = (obj != null && obj.getString("r1") != null) ? obj.getString("r1") : null;
                r2 = (obj != null && obj.getString("r2") != null) ? obj.getString("r2") : null;
                r3 = (obj != null && obj.getString("r3") != null) ? obj.getString("r3") : null;
                r4 = (obj != null && obj.getString("r4") != null) ? obj.getString("r4") : null;
                r5 = (obj != null && obj.getString("r5") != null) ? obj.getString("r5") : null;
                minWeight = (obj != null && obj.getString("minWeight") != null) ? obj.getString("minWeight") : null;
                hangBorderLine = (obj != null && obj.getString("hangBorderLine") != null) ? obj.getString("hangBorderLine") : null;

                if (cpName.equals(Constants.BAIDUSELF_CP)) hangBorderLine_temp = baiduself_hang_borderline;
                if (cpName.equals(Constants.CTRIP_CP)) hangBorderLine_temp = ctrp_hang_borderline;
                if (cpName.equals(Constants.SOGOPARK_CP)) hangBorderLine_temp = sogopark_hang_borderline;
                if (cpName.equals(Constants.BAIDU_CP)) hangBorderLine_temp = baidu_hang_borderline;
                if (cpName.equals(Constants.QUNAR_CP)) hangBorderLine_temp = qunar_hang_borderline;
                if (cpName.equals(Constants.DEALERSHIP_CP)) hangBorderLine_temp = dealership_hang_borderline;

                if (r1 != null && !"".equals(r1)) r1_temp = Double.valueOf(r1);
                if (r2 != null && !"".equals(r1)) r2_temp = Double.valueOf(r2);
                if (r3 != null && !"".equals(r3)) r3_temp = Double.valueOf(r3);
                if (r4 != null && !"".equals(r4)) r4_temp = Double.valueOf(r4);
                if (r5 != null && !"".equals(r5)) r5_temp = Double.valueOf(r5);
                if (minWeight != null && !"".equals(minWeight)) minWeight_temp = Double.valueOf(minWeight);
                if (hangBorderLine != null && !"".equals(hangBorderLine))
                    hangBorderLine_temp = Double.valueOf(hangBorderLine);

                Map<String, Double> map = new HashMap<String, Double>();
                map.put("r1", r1_temp);
                map.put("r2", r2_temp);
                map.put("r3", r3_temp);
                map.put("r4", r4_temp);
                map.put("r5", r5_temp);
                map.put("minWeight", minWeight_temp);
                map.put("hangBorderLine", hangBorderLine_temp);
                simMap.put(cpName, map);
            }
        }

        similarityMap = simMap;
    }
}
