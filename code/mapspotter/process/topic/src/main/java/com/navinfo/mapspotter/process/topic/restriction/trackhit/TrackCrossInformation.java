package com.navinfo.mapspotter.process.topic.restriction.trackhit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轨迹及其关联路口信息
 * Created by SongHuiXing on 2016/1/12.
 */
public class TrackCrossInformation {
    public String TrackId;

    private double[] m_coordinates;
    @JsonProperty("Coordinates")
    public double[] getCoords(){
        return m_coordinates;
    }
    public void setCoords(double[] coords){
        m_coordinates = coords;
    }

    private List<String> m_relatecross = new ArrayList<>();
    @JsonProperty("Crosses")
    public List<String> getCrosses(){
        return m_relatecross;
    }
    public void setCrosses(List<String> crs){
        m_relatecross = crs;
    }
    public void insertCross(String cs){
        m_relatecross.add(cs);
    }

    private Map<Long, int[]> m_coordsInCross = new HashMap<>();
    public Map<Long, int[]> getCoordsInCross(){
        return m_coordsInCross;
    }
    public void setCoordsInCross(Map<Long, int[]> coordsInCross){
        m_coordsInCross = coordsInCross;
    }
}
