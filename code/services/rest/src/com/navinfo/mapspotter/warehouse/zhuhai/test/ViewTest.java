package com.navinfo.mapspotter.warehouse.zhuhai.test;

import com.mercator.TileUtils;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.warehouse.zhuhai.util.DataSource;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zuoweiguang on 2016/9/18.
 */
public class ViewTest {

    public void getTiles(double lon, double lat) {
        String tile;
        try {
            Coordinate coord = new Coordinate(lon, lat);
            for (int i = 10; i <= 17; i++) {
                tile = MercatorUtil.lonLat2MCode(coord, i);
                System.out.println(i + "_" + tile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        ViewTest vt = new ViewTest();
        vt.getTiles(113.17691, 21.993519);

        //修改执勤人员坐标
        //db.user_info.update({"mobile_phone":"13691231236"}, {"$set": {"location": [113.174251,21.992313]}})

    }


}
