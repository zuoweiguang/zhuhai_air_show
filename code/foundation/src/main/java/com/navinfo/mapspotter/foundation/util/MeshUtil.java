package com.navinfo.mapspotter.foundation.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 图幅与经纬度坐标的转换
 * Created by cuiliang on 2015/12/30.
 */
public class MeshUtil {
    /**
     * 坐标转图幅号
     *
     * @param lon x
     * @param lat y
     * @return meshId
     */
    public static String coordinate2Mesh(double lon, double lat) {
        double _w;
        int _m1m2, _m3m4, _m5, _m6;
        int _j1;
        double _j2;
        _w = lat;
        _m1m2 = (int) (_w * 1.5);

        _j1 = (int) lon;
        _j2 = lon - _j1;

        _m3m4 = _j1 - 60;
        _m5 = (int) ((_w - _m1m2 / 1.5) * 12.0);
        _m6 = (int) (_j2 * 8.0);
        StringBuilder builder = new StringBuilder();
        builder.append(_m1m2).append(_m3m4).append(_m5).append(_m6);
        String meshId = builder.toString();
        if (meshId.length() == 5) {
            meshId = "0" + meshId;
        }
        meshId = StringUtil.fillLeft(meshId, "0", 6);
        return meshId;
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

    /**
     * 图幅号转坐标
     *
     * @param meshId
     * @return Coordinate
     */
    public static double[] mesh2Coordinate(String meshId) {
        int _m1 = Integer.valueOf(meshId.substring(0, 1));
        int _m2 = Integer.valueOf(meshId.substring(1, 2));
        int _m3 = Integer.valueOf(meshId.substring(2, 3));
        int _m4 = Integer.valueOf(meshId.substring(3, 4));
        int _m5 = Integer.valueOf(meshId.substring(4, 5));
        int _m6 = Integer.valueOf(meshId.substring(5, 6));
        int _lbx = (_m3 * 10 + _m4) * 3600 + _m6 * 450 + 60 * 3600;
        int _lby = (_m1 * 10 + _m2) * 2400 + _m5 * 300;
        int cX = _lbx + 450 / 2;
        int cY = _lby + 300 / 2;
        return new double[]{cX / 3600d, cY / 3600d};

    }

    /**
     * 坐标转扩圈图幅
     *
     * @param lng x
     * @param lat y
     * @return 3*3图幅列表
     */
    public static List<String> coordinate2MeshList(double lng, double lat) {
        double[] coord = mesh2Coordinate(coordinate2Mesh(lng, lat));
        List<String> meshList = new ArrayList<String>();
        double x = coord[0];
        double y = coord[1];

        String mesh_1 = coordinate2Mesh(x - 0.125d, y - 0.25 / 3d);
        String mesh_2 = coordinate2Mesh(x, y - 0.25 / 3d);
        String mesh_3 = coordinate2Mesh(x + 0.125d, y - 0.25 / 3d);

        String mesh_4 = coordinate2Mesh(x - 0.125d, y);
        String mesh_5 = coordinate2Mesh(x, y);
        String mesh_6 = coordinate2Mesh(x + 0.125d, y);

        String mesh_7 = coordinate2Mesh(x - 0.125d, y + 0.25 / 3d);
        String mesh_8 = coordinate2Mesh(x, y + 0.25 / 3d);
        String mesh_9 = coordinate2Mesh(x + 0.125d, y + 0.25 / 3d);
        if (mesh_1 != null)
            meshList.add(mesh_1);
        if (mesh_2 != null)
            meshList.add(mesh_2);
        if (mesh_3 != null)
            meshList.add(mesh_3);
        if (mesh_4 != null)
            meshList.add(mesh_4);
        if (mesh_5 != null)
            meshList.add(mesh_5);
        if (mesh_6 != null)
            meshList.add(mesh_6);
        if (mesh_7 != null)
            meshList.add(mesh_7);
        if (mesh_8 != null)
            meshList.add(mesh_8);
        if (mesh_9 != null)
            meshList.add(mesh_9);
        return meshList;
    }
}
