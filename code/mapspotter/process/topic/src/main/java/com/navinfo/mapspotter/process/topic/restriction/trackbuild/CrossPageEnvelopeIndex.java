package com.navinfo.mapspotter.process.topic.restriction.trackbuild;

import com.navinfo.mapspotter.process.topic.restriction.RestricConfig;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;

import java.util.HashMap;
import java.util.Map;

/**
 * 路口对应的页面范围及索引
 * Created by SongHuiXing on 2016/2/2.
 */
public class CrossPageEnvelopeIndex {
    private HashMap<String, Map<Long, double[]>> index = new HashMap<>();

    public void insert(String rowkey, double[] env){
        String geohash = rowkey.substring(0, RestricConfig.SearchGeohashLen);
        String pidStr = rowkey.substring(rowkey.lastIndexOf('#')+1, rowkey.length());

        Map<Long, double[]> crosses = null;
        if(index.containsKey(geohash)){
            crosses = index.get(geohash);
        } else {
            crosses = new HashMap<>();
        }


        crosses.put(CrossInformationVistor.getPid(pidStr), env);
        index.put(geohash, crosses);
    }

    public Map<Long, double[]> get(String geohash){
        if(!index.containsKey(geohash))
            return null;

        return index.get(geohash);
    }

    public int getCount(){
        return index.size();
    }
}
