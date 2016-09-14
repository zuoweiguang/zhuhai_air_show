package com.navinfo.mapspotter.foundation.util;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.*;

/**
 * 道格拉斯普克抽希
 */
public class Douglas {
    private static GeometryFactory factory = new GeometryFactory();

    /**
     * 对矢量曲线进行压缩
     */
    public LineString compress(LineString line, double precision) {
        if(line.getNumPoints() < 3)
            return line;

        List<Coordinate> points = compress(line, 0, line.getNumPoints()-1, precision);

        Coordinate[] coordinates = new Coordinate[points.size()];
        points.toArray(coordinates);

        return factory.createLineString(coordinates);
    }

    private List<Coordinate> compress(LineString line, int fromIndex, int toIndex, double precision){
        List<Coordinate> sparse_pts = new ArrayList<>();

        Coordinate from = line.getCoordinateN(fromIndex);
        Coordinate to = line.getCoordinateN(toIndex);

        if ((fromIndex + 1) == toIndex) {
            sparse_pts.add(from);
            sparse_pts.add(to);
            return sparse_pts;
        }

        /**
         * 由起始点和终止点构成的直线方程一般式的系数
         */
        double A = (from.y - to.y)
                / Math.sqrt(Math.pow((from.y - to.y), 2)
                + Math.pow((from.x - to.x), 2));

        /**
         * 由起始点和终止点构成的直线方程一般式的系数
         */
        double B = (to.x - from.x)
                / Math.sqrt(Math.pow((from.y - to.y), 2)
                + Math.pow((from.x - to.x), 2));

        /**
         * 由起始点和终止点构成的直线方程一般式的系数
         */
        double C = (from.x * to.y - to.x * from.y)
                / Math.sqrt(Math.pow((from.y - to.y), 2)
                + Math.pow((from.x - to.x), 2));


        List<Double> distance = new ArrayList<>();

        int middleIndex = 0;
        double dmax = 0;

        for (int i = fromIndex + 1; i < toIndex; i++) {
            double dis = Math.abs(A * (line.getCoordinateN(i).x) + B * (line.getCoordinateN(i).y) + C)
                            / Math.sqrt(Math.pow(A, 2)
                            + Math.pow(B, 2));

            if(dis < dmax)
                continue;

            dmax = dis;
            middleIndex = i;
        }

        sparse_pts.add(from);

        if (dmax > precision) {
            List<Coordinate> fronts = compress(line, fromIndex, middleIndex, precision);
            fronts.remove(0);
            fronts.remove(fronts.size()-1);
            sparse_pts.addAll(fronts);

            List<Coordinate> backs = compress(line, middleIndex, toIndex, precision);
            backs.remove(backs.size()-1);
            sparse_pts.addAll(backs);
        }

        sparse_pts.add(to);

        return sparse_pts;
    }
}
