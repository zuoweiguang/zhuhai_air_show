package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 空间关系过滤器接口
 * Created by ZhangJin1207 on 2016/4/6.
 */
public interface RelateFilter<T> {
    boolean accept(T object , Geometry geometry);
}
