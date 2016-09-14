package com.navinfo.mapspotter.process.topic.restriction.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/2/17.
 */
public class TrackHitInfo {
    public String trackId;

    public Map<String,int[]> index = new HashMap<>();

    public Map<String,double[]> location = new HashMap<>();

    public List<int[]> coords = new ArrayList<>();
    public List<Integer> values = new ArrayList<>();
}
