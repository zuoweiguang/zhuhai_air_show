package com.navinfo.mapspotter.foundation.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 墨卡托坐标与经纬度坐标转换类
 * Created by cuiliang on 2015/12/30.
 */
public class MercatorUtil {

    private int level = 12;

    final private static double R_MAJOR = 6378137.0;
    final private static double R_MINOR = 6356752.3142;
    final private static double M_PI = Math.PI;
    final private static int tileSize = 256;
    final private static double initialResolution = 2 * M_PI * R_MAJOR / tileSize;
    final private static double originShift = 2 * M_PI * R_MAJOR / 2.0;

    /**
     * @param level 传入墨卡托等级号
     */
    public MercatorUtil(int level) {
        this.level = level;
    }

    /**
     * 坐标转瓦片号
     *
     * @param lon 经度
     * @param lat 纬度
     * @return 瓦片号
     */
    public String lonLat2MCode(double lon, double lat) {
        double[] a = lonLat2Meter(lon, lat);
        int[] tile = meter2Tile(a[0], a[1], this.level);
        String title = "" + (tile[0]) + "_" + (tile[1]);
        return title;
    }

    /**
     * 根据坐标计算扩圈墨卡托list
     *
     * @param lon     经度
     * @param lat     纬度
     * @param ringNum 扩大圈数
     * @return
     */
    public List<String> lonLat2MCodeList(double lon, double lat, int ringNum) {
        List<String> mcodes = new ArrayList<>();
        double[] a = lonLat2Meter(lon, lat);
        int[] tile = meter2Tile(a[0], a[1], this.level);
        int min = -ringNum;
        int max = ringNum + 1;
        String mcode;
        for (int i = min; i < max; i++) {
            for (int j = min; j < max; j++) {
                mcode = "" + (tile[0] + i) + "_" + (tile[1] + j);
                mcodes.add(mcode);
            }
        }
        return mcodes;
    }

    /**
     * 瓦片号转坐标框(左上/右下)
     *
     * @param mcode 瓦片号
     * @return 坐标数组
     */
    public double[] mercatorBound(String mcode) {
        double m_res = Math.pow(2, this.level) / 2.0;
        String[] splits = mcode.split("_");
        double tx = Double.parseDouble(splits[0]);
        double ty = Double.parseDouble(splits[1]);
        double minlon = (tx / m_res - 1) * 180;
        double maxlon = ((tx + 1) / m_res - 1) * 180;
        double my = 1 - ty / m_res;
        double maxlat = Math.atan(Math.exp(my * M_PI)) * 360 / M_PI - 90;
        my = 1 - (ty + 1) / m_res;
        double minlat = Math.atan(Math.exp(my * M_PI)) * 360 / M_PI - 90;
        return new double[]{minlon, minlat, maxlon, maxlat};
    }

    /**
     * 经纬度转墨卡托坐标
     *
     * @param lon 经度
     * @param lat 纬度
     * @return 墨卡托坐标
     */
    public static double[] lonLat2Meter(double lon, double lat) {
        double x;
        double y;
        x = lon * originShift / 180.0;
        y = Math.log(Math.tan((90 + lat) * M_PI / 360.0)) / (M_PI / 180.0);
        y = y * originShift / 180.0;
        double a[] = {x, y};
        return a;
    }

    /**
     * 墨卡托坐标转经纬度
     *
     * @param x 墨卡托x坐标
     * @param y 墨卡托y坐标
     * @return 经纬度
     */
    public static double[] meter2LonLat(double x, double y) {
        double lat;
        double lon;
        lon = (x / originShift) * 180.0;
        lat = (y / originShift) * 180.0;
        lat = 180 / M_PI * (2 * Math.atan(Math.exp(lat * M_PI / 180.0)) - M_PI / 2.0);
        double a[] = {lon, lat};
        return a;
    }

    public static int[] meter2Tile(double mx, double my, int zoom) {
        double px, py;
        int[] a = pixelsToTile(metersToPixels(mx, my, zoom));
        return a;
    }

    private static double pixelToMeterX(double px, int zoom) {
        double res = resolution(zoom);
        return px * res - originShift;
    }

    private static double pixelToMeterY(double py, int zoom) {
        double res = resolution(zoom);
        return -py * res + originShift;
    }

    // Wkb::Box2D Mercator::TileBounds(int tx, int ty, int zoom)
    private static double[] metersToPixels(double mx, double my, int zoom) {
        double px;
        double py;
        double res = resolution(zoom);

        px = (mx + originShift) / res;
        py = (-my + originShift) / res;
        double a[] = {px, py};
        return a;
    }

    private static double resolution(int zoom) {
        return initialResolution / Math.pow(2, zoom);
    }

    private static int[] pixelsToTile(double px, double py) {
        int tx;
        int ty;
        tx = (int) Math.ceil(px / (double) tileSize) - 1;
        ty = (int) Math.ceil(py / (double) tileSize) - 1;
        int a[] = {tx, ty};
        return a;
    }

    private static int[] pixelsToTile(double[] p) {
        return pixelsToTile(p[0], p[1]);
    }
}
