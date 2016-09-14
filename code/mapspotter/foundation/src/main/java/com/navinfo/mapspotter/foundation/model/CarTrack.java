package com.navinfo.mapspotter.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 行车轨迹
 * Created by SongHuiXing on 2016/1/12.
 */
public class CarTrack {

    private static final double meterPerPixel = 2.5;

    protected static double  s_maxDistanceBetweenPoints = (500 / meterPerPixel);

    public enum TrackType{
        Didi,
        Sogou,
    }

    public CarTrack(){
        this("");
    }

    public CarTrack(String id){
        m_CarID = id;
        m_Envelope = new double[]{0,0,0,0};
    }

    private String m_CarID;
    @JsonProperty("carid")
    public String getCarID() {
        return m_CarID;
    }
    public void setCarID(String id){
        m_CarID = id;
    }

    protected List<CarTrackPoint> m_CarTrack = null;
    @JsonProperty("trackpoints")
    public List<CarTrackPoint> getCarTrack(){
        return m_CarTrack;
    }
    public void setCarTrack(List<CarTrackPoint> track){
        m_CarTrack = track;
    }

    @JsonProperty("timespan")
    public long[] getTimeSpan(){
        long s = 0, e=0;
        List<CarTrackPoint> carTrack = getCarTrack();
        if(carTrack.size() > 0){
            s = carTrack.get(0).getTimestamp();
            e = carTrack.get(carTrack.size()-1).getTimestamp();
        }
        return new long[]{s, e};
    }
    public void setTimeSpan(long[] span){
    }

    protected double[] m_Envelope;
    @JsonProperty("envelope")
    public double[] getEnvelope(){
        return m_Envelope;
    }
    public void setEnvelope(double[] env){
        m_Envelope = env;
    }

    @JsonIgnore
    public int getPointCount(){
        return m_CarTrack.size();
    }

    @JsonIgnore
    public int[] getTrackCoordinates(){
        List<CarTrackPoint> carTrack = m_CarTrack;
        int[] coords = new int[2 * carTrack.size()];

        int i=0;
        for(CarTrackPoint pt : carTrack){
            coords[i * 2] = pt.getLongitude();
            coords[i * 2 + 1] = pt.getLatitude();
            i++;
        }

        return coords;
    }

    /**
     * 加入一个轨迹点
     * @param fields 滴滴数据
     * @return 0 成功 -1 无效数据 1 不应作为该轨迹的点
     */
    public int pushPoint(String[] fields){
        return  -1;
    }
}
