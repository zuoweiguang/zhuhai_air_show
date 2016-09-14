package com.navinfo.mapspotter.foundation.util;


/**
 * 百度坐标加密解密
 * Created by cuiliang on 2015/12/30.
 */
public class CoordinateUtil {

    static final double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    static final double a = 6378245.0;
    static final double ee = 0.00669342162296594323;

    /**
     * 百度坐标加密(火星坐标转百度坐标)
     *
     * @param gg_lon 经度
     * @param gg_lat 纬度
     * @return 百度坐标经纬度
     */
    public static double[] bd_encrypt(double gg_lon, double gg_lat) {
        double bd_lat;
        double bd_lon;
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        bd_lon = z * Math.cos(theta) + 0.0065;
        bd_lat = z * Math.sin(theta) + 0.006;
        double a[] = {bd_lat, bd_lon};
        return a;
    }

    /**
     * 百度坐标解密(百度坐标转火星坐标)
     *
     * @param bd_lon 百度坐标经度
     * @param bd_lat 百度坐标纬度
     * @return 经纬度
     */
    public static double[] bd_decrypt(double bd_lon, double bd_lat) {
        double gg_lat;
        double gg_lon;
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        gg_lon = z * Math.cos(theta);
        gg_lat = z * Math.sin(theta);
        double a[] = {gg_lat, gg_lon};
        return a;
    }
}
