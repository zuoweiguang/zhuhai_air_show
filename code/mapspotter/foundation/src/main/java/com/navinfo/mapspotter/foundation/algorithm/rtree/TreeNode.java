package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.navinfo.mapspotter.foundation.util.SpatialUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 叶子节点
 * Created by SongHuiXing on 2016/3/4.
 */
public class TreeNode {
    protected double[] nodeEnv = null;

    public TreeNode(double[] env){
        this.nodeEnv = env;
    }

    private HashMap<Long, EnvlopeIndentifiedObject> objs = new HashMap<>();

    public double[] getNodeEnv(){
        return nodeEnv;
    }

    public boolean isIntersect(double[] targetEnv){
        return SpatialUtil.isEnvelopeIntesect(targetEnv, nodeEnv);
    }

    public void insert(EnvlopeIndentifiedObject obj){

        if(objs.containsKey(obj.getID()))
            return;

        objs.put(obj.getID(), obj);
    }

    public Map<Long, EnvlopeIndentifiedObject> find(double[] targetEnv){
        HashMap<Long,EnvlopeIndentifiedObject> result = new HashMap<>();

        for(EnvlopeIndentifiedObject obj : objs.values()){
            if(SpatialUtil.isEnvelopeIntesect(obj.getEnvelope(), targetEnv)){
                result.put(obj.getID(), obj);
            }
        }

        return result;
    }
}
