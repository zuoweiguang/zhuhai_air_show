package com.navinfo.mapspotter.foundation.algorithm.rtree;

/**
 * 简单的索引点的R树
 * 采用墨卡托分级划分网格
 * Created by SongHuiXing on 2016/3/4.
 */
public class SimplePointRTree extends NonLeafNode {

    private long total_obj_count = 0;

    /**
     * ctor
     * @param env       索引范围 minx, miny, maxx, maxy
     * @param leafLevel 索引叶子节点采用的墨卡托等级
     */
    public SimplePointRTree(double[] env, short leafLevel){
        super(env, (short)(leafLevel - 1));
    }

    @Override
    public void insert(EnvlopeIndentifiedObject obj){
        super.insert(obj);
        total_obj_count++;
    }

    public long getTotalObjCount(){
        return total_obj_count;
    }

}
