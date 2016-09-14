package com.navinfo.mapspotter.foundation.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import org.junit.Test;
/**
 * Created by ZhangJin1207 on 2016/1/5.
 */
public class SpatialUtilTest {

    @Test
    public void test(){

        Geometry Geo1 = GeoUtil.wkt2Geometry("LINESTRING(0 40, 60 40, 60 0, 40 0)");
        Geometry Geo2 = GeoUtil.wkt2Geometry("LINESTRING(0 0, 80 80)");
        Geometry Geo3 = GeoUtil.wkt2Geometry("POINT(10 10)");
        Geometry Geo4 = GeoUtil.wkt2Geometry("POLYGON((0 0,60 0,60 60 ,0 60,0 0))");
        Geometry IGeo = SpatialUtil.Intersection(Geo1 , Geo2);
        boolean bI = SpatialUtil.Intersects(Geo1 , Geo2);

        System.out.println(IGeo);
        System.out.println(bI);

        IntersectionMatrix im = Geo4.relate(Geo2);

        boolean bintersect =  im.isIntersects();
        boolean bcontains = SpatialUtil.Contains(Geo4 , Geo3);
        boolean bcoveredby = im.isCoveredBy();
        boolean bcovers = SpatialUtil.Covers(Geo4 , Geo2);
        boolean boverlaps = im.isOverlaps(2 , 2);

        System.out.println(bintersect);
        System.out.println(bcontains);
        System.out.println(bcoveredby);
        System.out.println(bcovers);
        System.out.println(boverlaps);

        String str = im.toString();

        System.out.println(str);
        System.out.println(im);
    }

    @Test
    public void TestIntesect(){
        double[] Line1 = new double[]{0,0,60,60};
        double[] Line2 = new double[]{0,40,40,0};
        double[] Env1 = new double[]{10,10,60,60};
        double[] Env2 = new double[]{0,0,20,20};

        boolean bLiL = SpatialUtil.isLineIntersect(Line1 , Line2);
        boolean bLiE = SpatialUtil.isLineIntersectWithEnvelope(Line1 , Env1);
        boolean bEiE = SpatialUtil.isEnvelopeIntesect(Env1 , Env2);

        System.out.println(bLiL);
        System.out.println(bLiE);
        System.out.println(bEiE);
    }
}
