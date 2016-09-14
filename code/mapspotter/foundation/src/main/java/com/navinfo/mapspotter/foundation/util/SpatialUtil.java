package com.navinfo.mapspotter.foundation.util;


import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.CentroidPoint;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import org.jblas.DoubleMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangJin1207 on 2016/1/5.
 * Modified by Songhuixing on 2016/01/11.
 */
public class SpatialUtil {

    /**
     * 判断两个矩形是否相交
     * @param env1 [minx, miny, maxx, maxy]
     * @param env2
     * @return
     */
    public static boolean isEnvelopeIntesect(double[] env1 , double[] env2){
        if(env1[0] > env2[2] || env2[0] > env1[2])
            return false;

        if(env1[1] > env2[3] || env2[1] > env1[3])
            return false;

        return true;
    }

    public static boolean isEnvelopeIntesect(int[] env1 , int[] env2){
        if(env1[0] > env2[2] || env2[0] > env1[2])
            return false;

        if(env1[1] > env2[3] || env2[1] > env1[3])
            return false;

        return true;
    }

    /**
     * 判断两个线段是否相交
     * @param line1
     * @param line2
     * @return
     */
    public static boolean isLineIntersect(double[] line1, double[] line2){

        double[] line1Env = new double[]{Math.min(line1[0], line1[2]),
                Math.min(line1[1], line1[3]),
                Math.max(line1[0], line1[2]),
                Math.max(line1[1], line1[3])};

        double[] line2Env = new double[]{Math.min(line2[0], line2[2]),
                Math.min(line2[1], line2[3]),
                Math.max(line2[0], line2[2]),
                Math.max(line2[1], line2[3])};

        if(!isEnvelopeIntesect(line1Env, line2Env))
            return false;

        return isStraddle(line1, line2) && isStraddle(line2, line1);
    }

    /**
     * 跨立实验
     * @param p
     * @param q
     * @return
     */
    public static boolean isStraddle(double[] p, double[] q){
        double[] p1_q1 = new double[]{p[0]-q[0], p[1]-q[1]};

        double[] p2_q1 = new double[]{p[2]-q[0], p[3]-q[1]};

        double[] q2_q1 = new double[]{q[2]-q[0], q[3]-q[1]};

        double p1_q1Xq2_q1 = cross(p1_q1, q2_q1);

        double q2_q1Xp2_q1 = cross(q2_q1, p2_q1);

        return p1_q1Xq2_q1 * q2_q1Xp2_q1 >=0;
    }

    /**
     * 原点矢量叉积
     * @param p
     * @param q
     * @return
     */
    public static double cross(double[] p, double[] q){
        return p[0]*q[1] - q[0]*p[1];
    }

    /**
     * 基于Base点的矢量的叉积
     * @param base  原点
     * @param p
     * @param q
     * @return
     */
    public static double cross(double[] base, double[] p, double[] q){
        return cross(base[0], base[1], p[0], p[1], q[0], q[1]);
    }

    public static double cross(double basex, double basey,
                               double px, double py,
                               double qx, double qy){
        return (px - basex) * (qy - basey) - (qx - basex) * (py - basey);
    }

    /**
     * 点是否在矩形内
     * @param x
     * @param y
     * @param env
     * @return
     */
    public static boolean isPointInEnvelope(double x, double y, double[] env){
        return x>= env[0] && x<=env[2] && y>=env[1] && y<= env[3];
    }

    public static boolean isPointInEnvelope(int x, int y, int[] env){
        return x>= env[0] && x<=env[2] && y>=env[1] && y<= env[3];
    }

    /**
     * 线段是否矩形与相交
     * @param line
     * @param env
     * @return
     */
    public static boolean isLineIntersectWithEnvelope(double[] line, double[] env){
        if(isPointInEnvelope(line[0], line[1], env) ||
                isPointInEnvelope(line[2], line[3], env))
            return true;

        if(isLineIntersect(line, new double[]{env[0], env[1], env[0], env[3]}))
            return true;

        if(isLineIntersect(line, new double[]{env[0], env[3], env[2], env[3]}))
            return true;

        if(isLineIntersect(line, new double[]{env[2], env[3], env[2], env[1]}))
            return true;

        if(isLineIntersect(line, new double[]{env[2], env[1], env[0], env[1]}))
            return true;

        return false;
    }
    /**
     * 计算两点间距离
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @return
     */
    public static double getDistance(double x0, double y0, double x1, double y1){
        return Math.sqrt(Math.pow((x1 - x0), 2) + Math.pow((y1 - y0), 2));
    }

    /**
     * 计算两个Geometry对象相交
      * @param geo1
     * @param geo2
     * @return
     */
    public static Geometry Intersection(Geometry geo1 , Geometry geo2){
        return geo1.intersection(geo2);
    }

    /**
     * 判断两个Geometry对象是否相交
     * @param geo1
     * @param geo2
     * @return
     */
    public static boolean Intersects(Geometry geo1 , Geometry geo2){
        return  geo1.intersects(geo2);
    }

    /**
     * 判断geo1是否覆盖geo2
     * @param geo1
     * @param geo2
     * @return
     */
    public static boolean Covers(Geometry geo1 , Geometry geo2){
        return geo1.covers(geo2);
    }

    /**
     * 判断geo1是否包含geo2
     * @param geo1
     * @param geo2
     * @return
     */
    public static boolean Contains(Geometry geo1 , Geometry geo2){
        return geo1.contains(geo2);
    }

    /**
     * 平移
     * @param line      被平移的线
     * @param distance  平移的x及y距离
     * @return
     * @author songhuixing
     */
    public static boolean move(LineString line, Coordinate distance){
        double[] mx = new double[]{1, 0 , distance.x, 0, 1, distance.y};

        AffineTransformation trans = new AffineTransformation(mx);

        line.apply(trans);

        return true;
    }

    /**
     * 计算从进入线的起点至终点转到退出线的起点至终点所需要的方向
     * @param inline    进入线vertex从起点到终点
     * @param outline   退出线vertex从起点到终点
     * @return  4调 2左 1直 3右
     * @author songhuixing
     */
    public static short calculateDirectInfo(LineString inline, LineString outline){
        short info = 1;

        if (inline == null || outline == null)
            return info;

        //弧度
        double angle = calculateCCTurnAngle(inline, outline);

        //转成角度
        double pi = 3.1415926;

        angle = (angle * 180.0 / Math.PI) % 360;

        //直
        if (angle > 135 && angle <= 225)
            info = 1;

        //左
        if (angle > 225 && angle <= 315)
            info = 2;

        //调
        if (angle > 315 && angle <= 360)
            info = 4;
        if (angle >= 0 && angle <= 45)
            info = 4;

        //右
        if (angle > 45 && angle <= 135)
            info = 3;

        return info;
    }

    /**
     * 计算从进入线的起点至终点转到退出线的起点至终点的弧度
     * @param inline    进入线vertex从起点到终点
     * @param outline   退出线vertex从起点到终点
     * @return  逆时针弧度
     * @author songhuixing
     */
    public static double calculateCCTurnAngle(LineString inline, LineString outline){
        Angle angle = new Angle();

        Coordinate s1 = inline.getCoordinate();
        Coordinate s2 = outline.getCoordinate();

        if(!s1.equals2D(s2)){   //两线不连接
            inline = (LineString)inline.clone();
            outline = (LineString)outline.clone();
            move(inline, new Coordinate(-s1.x, -s1.y, s1.z));
            move(outline, new Coordinate(-s2.x, -s2.y, s2.z));
        }

        Coordinate tip1 = inline.getCoordinateN(inline.getNumPoints()-1);
        Coordinate tail = outline.getCoordinate();
        Coordinate tip2 = outline.getCoordinateN(outline.getNumPoints()-1);

        double a = Angle.angleBetweenOriented(tip1, tail, tip2);

        return a >=0 ? a : a+2*Math.PI;
    }

    public static double getAngleWithNorth(Coordinate s, Coordinate e){
        if(s.x == e.x){
            if(s.y > e.y)
                return Math.PI;
            else
                return 0;
        }

        double y = e.y - s.y;
        double x = e.x - s.x;

        double a = Math.atan2(y, x);

        double halfPi = Math.PI / 2;

        if(y < 0 && x < 0){
            a = -3 * halfPi - a;
        } else {
            a = halfPi - a;
        }

        return a;
    }

    /**
     * 获取最小凸包
     * @param coords
     * @return
     * @author songhuixing
     */
    public static Coordinate[] getConvexHull(Coordinate[] coords){
        if(coords.length < 4) {
            return coords;
        }

        ConvexHull converxHull = new ConvexHull(coords, new GeometryFactory());

        Geometry hull = converxHull.getConvexHull();

        return hull.getCoordinates();
    }

    /**
     * 获取中心
     * @param coords
     * @return
     * @author songhuixing
     */
    public static Coordinate getCentroid(Coordinate[] coords){
        CentroidPoint centroidPoint = new CentroidPoint();

        for (Coordinate coord : coords){
            centroidPoint.add(coord);
        }

        return centroidPoint.getCentroid();
    }

    /**
     * 旋转卡壳法获取最大对踵点
     * @param converxhull 封闭的凸包
     * @return 对踵的两个点
     * @author songhuixing
     */
    public static Coordinate[] getMaxAntipode(Coordinate[] converxhull){
        double maxDis = 0;

        int ptCount = converxhull.length - 1;

        Coordinate resultP1 = null;
        Coordinate resultP2 = null;

        int q = 1;
        for(int p=0;p<ptCount;p++){
            Coordinate pt_p = converxhull[p];
            Coordinate pt_p1 = converxhull[p+1];

            while (cross(pt_p1.x, pt_p1.y,
                         converxhull[q+1].x, converxhull[q+1].y,
                         pt_p.x, pt_p.y) >
                   cross(pt_p1.x, pt_p1.y,
                         converxhull[q].x, converxhull[q].y,
                         pt_p.x, pt_p.y)) {
                q = (q + 1) % ptCount;
            }

            Coordinate pt_q = converxhull[q];
            Coordinate pt_q1 = converxhull[q+1];

            double dis0 = getDistance(pt_p.x, pt_p.y, pt_q.x, pt_q.y);
            double dis1 = getDistance(pt_p1.x, pt_p1.y, pt_q1.x, pt_q1.y);

            if(Math.max(dis0, dis1) > maxDis){
                maxDis = Math.max(dis0, dis1);
                if(dis0 > dis1){
                    resultP1 = pt_p;
                    resultP2 = pt_q;
                } else {
                    resultP1 = pt_p1;
                    resultP2 = pt_q1;
                }
            }
        }

        return new Coordinate[]{resultP1, resultP2};
    }

    /**
     * 获取凸包上所有点距离指定直线的最大垂直距离
     * @param converxhull
     * @param line
     * @return
     */
    public static double getMaxPerpendicularDistance(Coordinate[] converxhull, Coordinate[] line){
        Coordinate ptA = line[0];
        Coordinate ptB = line[1];

        double dis = 0;

        for (Coordinate pt : converxhull){
            double tmp = CGAlgorithms.distancePointLinePerpendicular(pt, ptA, ptB);

            if(tmp > dis)
                dis = tmp;
        }

        return dis;
    }

    /**
     * 旋转和平移
     * @param x         原始x
     * @param y         原始y
     * @param angle     逆时针弧度
     * @param offsetx   x方向的平移
     * @param offsety   y方向的平移
     * @return
     * @author songhuixing
     */
    public static int[] transform(int x, int y, double angle, int offsetx, int offsety){
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        DoubleMatrix transMx = DoubleMatrix.zeros(3, 3);
        transMx.put(0, 0, cosA);
        transMx.put(0, 1, -sinA);
        transMx.put(0, 2, offsetx);

        transMx.put(1, 0, sinA);
        transMx.put(1, 1, cosA);
        transMx.put(1, 2, offsety);

        transMx.put(2, 2, 1);

        return transform(x, y, transMx);
    }

    public static int[] transform(int x, int y, DoubleMatrix transMx){
        DoubleMatrix coords = new DoubleMatrix(new double[]{x, y, 1});

        DoubleMatrix result = transMx.mmul(coords);

        return new int[]{(int)Math.round(result.get(0,0)), (int)Math.round(result.get(1,0))};
    }

    public static List<int[]> transform(List<int[]> coords, double angle, int offsetx, int offsety){
        ArrayList<int[]> result = new ArrayList<>();

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        DoubleMatrix transMx = DoubleMatrix.zeros(3, 3);
        transMx.put(0, 0, cosA);
        transMx.put(0, 1, -sinA);
        transMx.put(0, 2, offsetx);

        transMx.put(1, 0, sinA);
        transMx.put(1, 1, cosA);
        transMx.put(1, 2, offsety);

        transMx.put(2, 2, 1);

        for(int[] pt : coords){
            result.add(transform(pt[0], pt[1], transMx));
        }

        return result;
    }

    public static double[] transform(double x, double y, DoubleMatrix transMx){
        DoubleMatrix coords = new DoubleMatrix(new double[]{x, y, 1});

        DoubleMatrix result = transMx.mmul(coords);

        return new double[]{result.get(0,0),result.get(1,0)};
    }

    public static List<double[]> transformD(List<double[]> coords, double angle, double offsetx, double offsety){
        ArrayList<double[]> result = new ArrayList<>();

        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        DoubleMatrix transMx = DoubleMatrix.zeros(3, 3);
        transMx.put(0, 0, cosA);
        transMx.put(0, 1, -sinA);
        transMx.put(0, 2, offsetx);

        transMx.put(1, 0, sinA);
        transMx.put(1, 1, cosA);
        transMx.put(1, 2, offsety);

        transMx.put(2, 2, 1);

        for(double[] pt : coords){
            result.add(transform(pt[0], pt[1], transMx));
        }

        return result;
    }

    /**
     * 以原点为基点进行缩放
     * @param x
     * @param y
     * @param xtime
     * @param ytime
     * @return
     * @author songhuixing
     */
    public static double[] zoom(double x, double y, double xtime, double ytime){
        DoubleMatrix transMx = DoubleMatrix.zeros(3, 3);
        transMx.put(0, 0, xtime);
        transMx.put(1, 1, ytime);
        transMx.put(2, 2, 1);

        return zoom(x, y, transMx);
    }

    public static double[] zoom(double x, double y, DoubleMatrix transMx){
        DoubleMatrix coords = new DoubleMatrix(new double[]{x, y, 1});

        DoubleMatrix result = transMx.mmul(coords);

        return new double[]{result.get(0,0),result.get(1,0)};
    }

    public static List<double[]> zoom(List<double[]> coords, double xtime, double ytime){
        ArrayList<double[]> result = new ArrayList<>();

        DoubleMatrix transMx = DoubleMatrix.zeros(3, 3);
        transMx.put(0, 0, xtime);
        transMx.put(1, 1, ytime);
        transMx.put(2, 2, 1);

        for(double[] pt : coords){
            result.add(zoom(pt[0], pt[1], transMx));
        }

        return result;
    }

    public static double[] zoomEnvelopeInCenter(double[] src, double xtime, double ytime){
        double centerx = (src[0] + src[2]) / 2;
        double centery = (src[1] + src[3]) / 2;

        ArrayList<double[]> pts = new ArrayList<>();
        pts.add(new double[]{src[0], src[1]});
        pts.add(new double[]{src[2], src[3]});

        List<double[]> trans = transformD(pts, 0, -centerx, -centery);

        List<double[]> zoom = SpatialUtil.zoom(trans, xtime, ytime);

        zoom = SpatialUtil.transformD(zoom, 0, centerx, centery);

        return new double[]{zoom.get(0)[0], zoom.get(0)[1], zoom.get(1)[0], zoom.get(1)[1]};
    }
}
