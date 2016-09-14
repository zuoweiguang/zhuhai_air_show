package com.navinfo.mapspotter.foundation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 行车的轨迹点
 * Created by SongHuiXing on 2016/1/12.
 */
public class CarTrackPoint implements Comparable<CarTrackPoint> {
    private static short timePrecision = 3;

    private static double coordinatePrecision = 0.1;

    private static MercatorUtil mercator = new MercatorUtil(1024, 14);

    /**
     * for jackson decode
     */
    public CarTrackPoint(){
    }

    /**
     * One point record for car track
     * @param timeStamp POSIX time stamp
     * @param lon
     * @param lat
     * @param speed
     * @param angle
     * @param informations
     */
    public CarTrackPoint(long timeStamp,
                         double lon, double lat,
                         double speed, double angle,
                         String[] informations){

        IntCoordinate coord = mercator.lonLat2Pixels(new Coordinate(lon, lat));
        m_pixelX= coord.x;
        m_pixelY = coord.y;

        m_timeStamp = timeStamp;
        m_Speed = speed;
        m_Angle = angle;
        m_Information = informations;
    }

    private long m_timeStamp = 0;
    @JsonIgnore
    public long getTimestamp() {
        return m_timeStamp;
    }
    public void setTimestamp(long stamp){
        m_timeStamp = stamp;
    }

    private int m_pixelX = 0;
    @JsonProperty("lon")
    public int getLongitude(){
        return m_pixelX;
    }
    public void setLongitude(int lon){
        m_pixelX = lon;
    }

    private int m_pixelY = 0;
    @JsonProperty("lat")
    public int getLatitude(){
        return m_pixelY;
    }
    public void setLatitude(int lat){
        m_pixelY = lat;
    }

    private double m_Speed = 0;
    @JsonIgnore
    public double getSpeed(){
        return m_Speed;
    }
    public void setSpeed(double speed){
        m_Speed = speed;
    }

    private double m_Angle = 0;
    @JsonIgnore
    public double getAngle(){
        return m_Angle;
    }
    public void setAngle(double angle){
        m_Angle = angle;
    }

    private String[] m_Information;
    @JsonIgnore
    public String[] getInformation(){
        return m_Information;
    }
    public void setInformation(String[] infos){
        m_Information = infos;
    }

    @Override
    public int compareTo(CarTrackPoint o) {
        int interval = (int)(m_timeStamp - o.getTimestamp());

        if(0 == interval)
            return 0;

        if(Math.abs(interval) <= timePrecision){
            if(Math.abs(m_pixelX - o.getLongitude()) < coordinatePrecision &&
                    Math.abs(m_pixelY - o.getLatitude()) < coordinatePrecision)
                return 0;
        }

        return interval;
    }
}
