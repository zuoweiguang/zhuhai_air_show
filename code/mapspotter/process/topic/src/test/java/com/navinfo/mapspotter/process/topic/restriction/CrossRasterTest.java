package com.navinfo.mapspotter.process.topic.restriction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import com.vividsolutions.jts.geom.Coordinate;
import org.geojson.Point;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SongHuiXing on 2016/1/16.
 */
public class CrossRasterTest {
    @Test
    public void testCreateRaster(){
        String test = "-1";

        String[] nums = test.split("\\-");

        System.out.println(nums.length);

        Assert.assertTrue(nums.length >= 1);
    }

    @Test
    public void testOther(){

        HashMap<String, String> meshinfo = new HashMap<>();

        try {
            InputStreamReader input = new InputStreamReader(
                    new FileInputStream("E:\\WorkSpace\\testdata\\BaseRoad\\四省图幅列表_part.csv"));

            BufferedReader reader = new BufferedReader(input);

            String lineTxt = null;
            while (null != (lineTxt = reader.readLine())){
                String[] infos = lineTxt.split(",");

                if(4 != infos.length)
                    continue;

                String meshid = infos[0];
                meshid = meshid.substring(meshid.indexOf('\"')+1, meshid.length()-1);

                String province = infos[2];
                province = province.substring(province.indexOf('\"')+1, province.length()-1);

                meshinfo.put(meshid, province);
            }

            reader.close();
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final int i=0;
    @Test
    public void sbJava(){
        ObjectMapper mapper = new ObjectMapper();

        TypeFactory factory = mapper.getTypeFactory();

        ArrayType longArrayType = factory.constructArrayType(long.class);
        CollectionType childTurnType = factory.constructCollectionType(ArrayList.class,
                BaseCrossJsonModel.TurnFromChild.class);

        try {
            ArrayList<BaseCrossJsonModel.TurnFromChild> children = mapper.readValue("[0,0]", childTurnType);

            assert(null != children);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAngle(){
        Coordinate coord1 = new Coordinate(119.23, 40.112);
        Coordinate coord = new Coordinate(119.02, 40.2011);

        double theta = SpatialUtil.getAngleWithNorth(coord1, coord);

        System.out.println(theta);

        System.out.println(2*Math.cos(theta));

        System.out.println(-2*Math.sin(theta));
    }
}


