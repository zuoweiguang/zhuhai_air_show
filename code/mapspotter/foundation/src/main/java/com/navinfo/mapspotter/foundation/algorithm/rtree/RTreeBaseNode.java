package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.util.List;

/**
 * RTree 节点抽象类
 * Created by ZhangJin1207 on 2016/4/1.
 */
public abstract class RTreeBaseNode<T> extends Envelope {
    public RTreeBaseNode() {
    }

    public abstract void UpdateEnvelop();

    public abstract List<T> Relate(Envelope envelope, Geometry geometry, RelateFilter<T> filter);

    public abstract List<T> Relate(Coordinate coordinate, Geometry geometry, RelateFilter<T> filter);
}