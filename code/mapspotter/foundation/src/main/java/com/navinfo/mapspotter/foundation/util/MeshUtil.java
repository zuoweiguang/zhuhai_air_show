package com.navinfo.mapspotter.foundation.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.List;

/**
 * 图幅与经纬度坐标的转换
 * Created by cuiliang on 2015/12/30.
 */
public class MeshUtil {
    /**
     * 根据纬度计算该点位于理想图幅分割的行序号
     *
     * @param dLatitude 纬度，单位“度”
     * @return 行序号和余数
     */
    private static int[] calculateIdealRowIndex(double dLatitude) {
        //相对区域纬度 = 绝对纬度 - 0.0
        double regionLatitude = dLatitude - 0.0;

        //相对的以秒为单位的纬度
        double secondLatitude = regionLatitude * 3600;

        //为避免浮点数的内存影响，将秒*10的三次方(由于0.00001度为0.036秒)
        long longsecond = (long) Math.floor(secondLatitude * 1000);

        int index = (int) (longsecond / 300000);
        int remainder = (int) (longsecond % 300000);

        return new int[]{index, remainder};
    }

    /**
     * 根据经度计算该点位于理想图幅分割的列序号
     *
     * @param dLongitude 经度，单位“度”
     * @return 列序号和余数
     */
    private static int[] calculateIdealColumnIndex(double dLongitude) {
        //相对区域经度 = 绝对经度 - 60.0
        double regionLongitude = dLongitude - 60.0;

        //相对的以秒为单位的经度
        double secondLongitude = regionLongitude * 3600;

        //为避免浮点数的内存影响，将秒*10的三次方(由于0.00001度为0.036秒)
        long longsecond = (long) Math.floor(secondLongitude * 1000);

        int index = (int) (longsecond / 450000);
        int remainder = (int) (longsecond % 450000);

        return new int[]{index, remainder};
    }

    /**
     * 根据纬度计算该点位于实际图幅分割的行序号
     *
     * @param dLatitude 纬度，单位“度”
     * @return 行序号和余数
     */
    private static int calculateRealRowIndex(double dLatitude) {
        //理想行号
        int[] idealRow = calculateIdealRowIndex(dLatitude);

        int realRow = idealRow[0];
        int remainder = idealRow[1];

        switch (realRow % 3)//三个一组的余数
        {
            case 0: //第一行
            {
                if (300000 - remainder <= 12) //余数距离上框小于0.012秒
                    realRow++;
            }
            break;
            case 1: //第二行
                break;
            case 2: //第三行
            {
                if (remainder < 12) //余数距离下框小于等于0.012秒
                    realRow--;
            }
            break;
        }

        return realRow;
    }

    /**
     * 根据经度计算该点位于实际图幅分割的列序号
     *
     * @param dLongitude 经度，单位“度”
     * @return 列序号和余数
     */
    private static int calculateRealColumnIndex(double dLongitude) {
        return calculateIdealColumnIndex(dLongitude)[0];
    }

    /**
     * 根据行列序号生成图幅号
     * @param rowInx
     * @param colInx
     * @return
     */
    private static String formatMeshId(int rowInx, int colInx) {
        int M1M2 = rowInx / 8;
        if (M1M2 <= 0 || M1M2 > 99) {
            return null;
        }

        int M3M4 = colInx / 8;
        if (M3M4 <= 0 || M3M4 > 99) {
            return null;
        }

        int M5 = rowInx % 8;

        int M6 = colInx % 8;

        return String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
    }

    /**
     * 坐标转图幅号
     *
     * @param lon x
     * @param lat y
     * @return meshId
     */
    public static String coordinate2Mesh(double lon, double lat) {
        int rowInx = calculateRealRowIndex(lat);

        int colInx = calculateRealColumnIndex(lon);

        return formatMeshId(rowInx, colInx);
    }

    /**
     * 坐标转图幅号
     *
     * @param lonLat
     * @return meshId
     */
    public static String coordinate2Mesh(double[] lonLat) {
        double lon = lonLat[0];
        double lat = lonLat[1];
        return coordinate2Mesh(lon, lat);
    }


    private static int parseRealRowIndex(String meshId) {
        int M1M2 = Integer.parseInt(meshId.substring(0, 2));

        int M5 = Integer.parseInt(meshId.substring(4, 5));

        return M1M2 * 8 + M5;
    }

    private static int parseRealColumnIndex(String meshId) {
        int M3M4 = Integer.parseInt(meshId.substring(2, 4));

        int M6 = Integer.parseInt(meshId.substring(5, 6));

        return M3M4 * 8 + M6;
    }

    /**
     * 计算图幅边框
     *
     * @param meshId 图幅号
     * @return Envelope
     */
    public static Envelope getMeshBound(String meshId) {
        int rowInx = parseRealRowIndex(meshId);
        int colInx = parseRealColumnIndex(meshId);
        double minLon = colInx * 450 + 60 * 3600;
        double minLat = rowInx * 300;
        double maxLon = minLon + 450;
        double maxLat = minLat + 300;
        return new Envelope(minLon/3600D, maxLon/3600D, minLat/3600D, maxLat/3600D);
    }

    /**
     * 图幅中心坐标
     *
     * @param meshId 图幅号
     * @return Coordinate
     */
    public static Coordinate mesh2Coordinate(String meshId) {
        return getMeshBound(meshId).centre();
    }

    /**
     * 坐标转扩圈图幅
     *
     * @param lng x
     * @param lat y
     * @return 3*3图幅列表
     */
    public static List<String> coordinate2MeshList(double lng, double lat) {
        int rowInx = calculateRealRowIndex(lat);
        int colInx = calculateRealColumnIndex(lng);

        return formatMeshIds(rowInx-1, rowInx+1, colInx-1, colInx+1);
    }

    public static List<String> bound2Meshes(Envelope bound) {
        int rowMin = calculateRealRowIndex(bound.getMinY());
        int rowMax = calculateRealRowIndex(bound.getMaxY());
        int colMin = calculateRealColumnIndex(bound.getMinX());
        int colMax = calculateRealColumnIndex(bound.getMaxX());

        return formatMeshIds(rowMin, rowMax, colMin, colMax);
    }

    private static List<String> formatMeshIds(int rowMin, int rowMax, int colMin, int colMax) {
        List<String> meshList = new ArrayList<>();

        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                String mesh = formatMeshId(row, col);
                if (mesh != null) meshList.add(mesh);
            }
        }

        return meshList;
    }

    /**
     * Grid号码转换地理范围
     * @param gridId
     * @return box[minx, miny, maxx, maxy]
     */
    public static double[] grid2Rect(String gridId){

        int num = Integer.parseInt(gridId);
        gridId = String.format("%08d", num);

        int m1 = Integer.valueOf(gridId.substring(0, 1));
        int m2 = Integer.valueOf(gridId.substring(1, 2));
        int m3 = Integer.valueOf(gridId.substring(2, 3));
        int m4 = Integer.valueOf(gridId.substring(3, 4));
        int m5 = Integer.valueOf(gridId.substring(4, 5));
        int m6 = Integer.valueOf(gridId.substring(5, 6));
        int m7 = Integer.valueOf(gridId.substring(6, 7));
        int m8 = Integer.valueOf(gridId.substring(7, 8));

        double minx = (m3 * 10 + m4) + (m6 * 450 + m8*450/4.0)/3600 + 60;
        double miny = ((m1 * 10 + m2) * 2400 + m5 * 300 + m7*300/4)/3600.0;

        double maxx = minx + 0.03125;
        double maxy = miny + (1.0/(12*4));

        return new double[]{minx, miny, maxx, maxy};
    }

}
