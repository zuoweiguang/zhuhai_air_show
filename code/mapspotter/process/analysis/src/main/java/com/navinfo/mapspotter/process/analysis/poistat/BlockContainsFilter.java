package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.algorithm.rtree.RelateFilter;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Created by ZhangJin1207 on 2016/4/7.
 */
public class BlockContainsFilter implements RelateFilter<BlockInfo> {
    public boolean accept(BlockInfo object , Geometry geometry){
        return object.getGeom().contains(geometry);
    }
}
