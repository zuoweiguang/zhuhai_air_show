package com.navinfo.mapspotter.foundation.algorithm.rtree;

/**
 * 以点标示的对象
 * Created by SongHuiXing on 2016/3/4.
 */
public interface EnvlopeIndentifiedObject {

    long getID();

    double getX();

    double getY();

    double[] getEnvelope();
}
