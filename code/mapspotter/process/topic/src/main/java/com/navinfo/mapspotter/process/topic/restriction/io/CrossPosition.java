package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.algorithm.rtree.EnvlopeIndentifiedObject;

/**
 * 路口位置对象
 * Created by SongHuiXing on 2016/3/4.
 */
public class CrossPosition implements EnvlopeIndentifiedObject {

    public long CrossPid;

    private double[] Envelope;

    public CrossPosition(double[] envelope, long pid){
        this.Envelope = envelope;
        this.CrossPid = pid;
    }

    @Override
    public long getID() {
        return CrossPid;
    }

    @Override
    public double getX() {
        return (Envelope[0] + Envelope[2]) / 2;
    }

    @Override
    public double getY() {
        return (Envelope[1] + Envelope[3]) / 2;
    }

    @Override
    public double[] getEnvelope() {
        return Envelope;
    }
}
