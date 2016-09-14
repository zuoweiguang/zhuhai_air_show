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
        double a[] = {bd_lon, bd_lat};
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
        double a[] = {gg_lon, gg_lat};
        return a;
    }

    /**
     * 根据经纬度，获取两点间的距离
     *
     * @author zhijun.wu
     * @param lng1 经度
     * @param lat1 纬度
     * @param lng2
     * @param lat2
     * @return
     *
     * @date 2011-8-10
     */
    public static double distanceByLngLat(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = lat1 * Math.PI / 180;
        double radLat2 = lat2 * Math.PI / 180;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180 - lng2 * Math.PI / 180;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1)
                * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378137.0;// 取WGS84标准参考椭球中的地球长半径(单位:m)
        s = Math.round(s * 10000) / 10000;

        return s;
    }
}
