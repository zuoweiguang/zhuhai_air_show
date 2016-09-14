package com.navinfo.mapspotter.process.topic.poihang;

import com.navinfo.mapspotter.foundation.algorithm.string.Levenshtein;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.BusinessPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.FastSource;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes.Poi;
import com.navinfo.mapspotter.foundation.util.CoordinateUtil;

import javax.xml.transform.Source;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/2/20.
 */
public class LevenshteinExtend {
    public static double getSimilarityRatioModify(FastSource s, BusinessPoi fp,
                                                  Map<String, Double> similarityMap) {

        if (fp == null || s == null)
            return 0;
        double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
        double x1, y1, x2, y2;
        double d;
        double tempc = 0;

        double r1_weight = similarityMap.get("r1").doubleValue();
        double r2_weight = similarityMap.get("r2").doubleValue();
        double r3_weight = similarityMap.get("r3").doubleValue();
        double r4_weight = similarityMap.get("r4").doubleValue();
        double r5_weight = similarityMap.get("r5").doubleValue();
        double minWeight = similarityMap.get("minWeight").doubleValue();
        // System.out.println("权重信息：    r1_weight: "+r1_weight+" r2_weight: "+r2_weight+" r3_weight: "+r3_weight+" r4_weight: "+r4_weight+" r5_weight: "+r5_weight);

        // 判断fastsource源与对应的businesspoi是否为同一种类别
        String sourceKindCode = s.getKindCode();
        String poiKindCode = fp.getKindCode();

        if (sourceKindCode == null || "".equals(sourceKindCode)
                || poiKindCode == null || "".equals(poiKindCode)) {
            if (!PoiPoitfixUtil.checkName(s.getName(), fp.getName())) {
                return 0.0;
            }
        } else if (!sourceKindCode.contains(poiKindCode)) {
            return 0.0;
        }

        // 计算距离的相似度。（对应算法：欧式距离相似度）
        if ((!s.getY().equals("")) && (!s.getX().equals("")))
            try {
                x1 = Double.valueOf(s.getY());
                y1 = Double.valueOf(s.getX());
                x2 = Double.valueOf(fp.getY());
                y2 = Double.valueOf(fp.getX());
                d = CoordinateUtil.distanceByLngLat(y1, x1, y2, x2);
                // 停车场时增大距离的权重
                if (sourceKindCode != null
                        && !"".equals(sourceKindCode)
                        && Constants.SOGOPARK_TargetKindCode
                        .contains(sourceKindCode)) {
                    r1 = 90 / (90 + d);
                } else {
                    r1 = 1 / (1 + d);
                }
                if (r1 != 0)
                    tempc += r1_weight;
            } catch (Exception e) {
                e.printStackTrace();
            }

        // 计算名称、地址、电话、邮编的相似度。（对应算法：编辑距离公式）
        if ((s.getName() != null) && (fp.getName() != null)
                && (!s.getName().equals("")) && (!fp.getName().equals(""))) {
            // 当数据源的名称为停用词时，返回0

            boolean stopFlag = (StopwordsConstant.getPostfixMap()).get(s
                    .getName()) == null ? false : (Boolean) (StopwordsConstant
                    .getPostfixMap()).get(s.getName());
            if (stopFlag) {
                r2 = 0;
            } else {
                r2 = Levenshtein.similarity(s.getName(), fp.getName());
            }
            if (r2 != 0)
                tempc += r2_weight;
        }

        if ((s.getAddr() != null) && (fp.getAddr() != null)
                && (!s.getAddr().equals("")) && (!fp.getAddr().equals(""))) {
            r3 = Levenshtein.similarity(s.getAddr(), fp.getAddr());

            if (fp.getName() != null && !"".equals(fp.getName())) {
                int length2 = fp.getName().length();
                int length = s.getAddr().length();
                if (length / length2 <= 1 || length2 / length <= 1) {
                    float r3Temp = Levenshtein.similarity(s.getAddr(), fp.getName());
                    if (r3Temp > r3) {
                        r3 = r3Temp;
                    }
                }
            }

            if (r3 != 0)
                tempc += r3_weight;
        }

        if ((s.getPostCode() != null) && (fp.getPostCode() != null)
                && (!s.getPostCode().equals(""))
                && (!fp.getPostCode().equals(""))) {
            r4 = Levenshtein.similarity(s.getPostCode(), fp.getPostCode());
            if (r4 != 0)
                tempc += r4_weight;
        }

        // 不同的电话之间为独立事件
        if ((s.getTel() != null) && (fp.getTel() != null)
                && (!s.getTel().equals("")) && (!fp.getTel().equals(""))) {
            String sourcePhone[] = (s.getTel()).split(";");
            String poiPhone[] = (fp.getTel()).split(";");
            if (sourcePhone.length > 0 && poiPhone.length > 0) {
                double r5_temp = 1;
                for (int i = 0; i < sourcePhone.length; i++) {
                    double temp = 0;
                    for (int j = 0; j < poiPhone.length; j++) {
                        String number = poiPhone[j];
                        if (number != null && !"".equals(number))
                            number = number.replace("+86", "").replaceAll(
                                    "\\D", "");
                        String str = sourcePhone[i];
                        if (str != null && !"".equals(str))
                            str = str.replace("+86", "").replaceAll("\\D", "");
                        double t = 0;
                        if (str != null && number != null && (!str.equals(""))
                                && (!number.equals(""))) {
                            if (str.equals(number))
                                return 1;
                        } else {
                            r5_weight = 0;
                        }

                    }

                }
                r5 = 0;
                tempc += r5_weight;
            }

        }

        double similarity1 = 1 - (1 - r1) * (1 - r2)
                * (1 - r3_weight * r3 - r4_weight * r4) * (1 - r5);
        double similarity2 = 0.0;
        if (tempc < minWeight) {
            similarity2 = (r1_weight * r1 + r2_weight * r2 + r3_weight * r3
                    + r4_weight * r4 + r5_weight * r5);
        } else {
            similarity2 = (r1_weight * r1 + r2_weight * r2 + r3_weight * r3
                    + r4_weight * r4 + r5_weight * r5)
                    / tempc;
        }

        return (similarity1 + similarity2) / 2.0;
    }

}

class PoiPoitfixUtil {
    public static Long getIndex(String t) {
        Long value = (long) -1;
        if (t == null)
            return value;

        if (t.length() >= 4) {
            value = PoiPoitfixConstant.postfixMap.get(t.substring(t.length() - 4));
            if (value != null)
                return value;
        }
        if (t.length() >= 3) {
            value = PoiPoitfixConstant.postfixMap.get(t.substring(t.length() - 3));
            if (value != null)
                return value;
        }
        if (t.length() >= 2) {
            value = PoiPoitfixConstant.postfixMap.get(t.substring(t.length() - 2));
            if (value != null)
                return value;
        }
        return -1L;
    }

    public static boolean checkName(String s1, String s2) {
        Long value1, value2;
        value1 = getIndex(s1);
        value2 = getIndex(s2);
        if (value1 == -1L || value2 == -1L)
            return true;
        return value1 == value2;
    }

}