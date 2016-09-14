package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.navinfo.mapspotter.process.topic.restriction.Link;
import org.geojson.LineString;
import org.geojson.LngLatAlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 输出给广伟的显示
 * Created by SongHuiXing on 2016/1/18.
 */
public class RestricAnalysisDisplay extends org.geojson.Feature {

    public static class RestricInfo{
        public RestricInfo(int id, long in, int inDir,
                           long out, int outDir){
            this.id = id;

            this.lin.put("id", String.format("%d", in));
            this.lin.put("direct", inDir);

            this.lout.put("id", String.format("%d", out));
            this.lout.put("direct", outDir);
        }

        public int id;
        public HashMap<String, Object> lin = new HashMap<>();
        public HashMap<String, Object> lout = new HashMap<>();
        public double[] geo;
        public int res;
    }

    public void insertLink(Link l){
        String propertyName = "links";

        HashMap<String, List<LngLatAlt>> links = null;
        if(!getProperties().containsKey(propertyName)){
            links = new HashMap<>();
        } else {
            links = getProperty(propertyName);
        }

        LineString line = l.getLine();
        links.put(String.format("%d", l.getPid()), line.getCoordinates());

        setProperty(propertyName, links);
    }

    public void insertBaseRestric(RestricInfo info){
        ArrayList<RestricInfo> restrics = null;
        String propName = "base";

        if(!getProperties().containsKey(propName)){
            restrics = new ArrayList<>();
        } else {
            restrics = getProperty(propName);
        }

        restrics.add(info);
        setProperty(propName, restrics);
    }

    public void insertAnalysisRestric(RestricInfo info){
        ArrayList<RestricInfo> restrics = null;
        String propName = "dig";

        if(!getProperties().containsKey(propName)){
            restrics = new ArrayList<>();
        } else {
            restrics = getProperty(propName);
        }

        restrics.add(info);
        setProperty(propName, restrics);
    }

    public void insertDiffRestricIds(String key, List<Integer> ids){
        String propName = "diff";
        HashMap<String, List<Integer>> diffIds = null;

        if(!getProperties().containsKey(propName)){
            diffIds = new HashMap<>();
        } else {
            diffIds = getProperty(propName);
        }

        diffIds.put(key, ids);

        setProperty(propName, diffIds);
    }
}
