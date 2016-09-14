package com.navinfo.mapspotter.foundation.util;


import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
/**
 * Created by ZhangJin1207 on 2016/1/5.
 */
public class SpatialUtil {

    private static GeometryFactory gfact = new GeometryFactory();
    private static WKTReader wktReader = new WKTReader(gfact);

    /**
     * 判断两个矩形是否相交
     * @param env1
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
     * @param p1
     * @param p2
     * @param q1
     * @param q2
     * @return
     */
    public static boolean isStraddle(double[] p, double[] q){
        double[] p1_q1 = new double[]{p[0]-q[0], p[1]-q[1]};

        double[] p2_q1 = new double[]{p[2]-q[0], p[3]-q[1]};

        double[] q2_q1 = new double[]{q[2]-q[0], q[3]-q[1]};

        double p1_q1Xq2_q1 = p1_q1[0]*q2_q1[1] - q2_q1[0]*p1_q1[1];

        double q2_q1Xp2_q1 = q2_q1[0]*p2_q1[1] - p2_q1[0]*q2_q1[1];

        return p1_q1Xq2_q1 * q2_q1Xp2_q1 >=0;
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
     * @param p
     * @param q
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
     * 通过WKT串生成Geometry对象
     * @param strWKT
     * @return
     */
    public static Geometry CreateGeometryByWKT(String strWKT){
        Geometry geom = null;
        try {
            geom = wktReader.read(strWKT);
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        return geom;
    }
}
