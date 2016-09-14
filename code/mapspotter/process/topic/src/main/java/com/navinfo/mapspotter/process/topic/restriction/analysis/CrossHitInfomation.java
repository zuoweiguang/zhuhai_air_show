package com.navinfo.mapspotter.process.topic.restriction.analysis;

import com.navinfo.mapspotter.foundation.util.IntCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述路口的打点情况
 * Created by SongHuiXing on 2016/2/17.
 */
public class CrossHitInfomation {
    public static class Point2d{
        public double longitude;
        public double latitude;
    }

    public long pid;

    public String mesh;

    public String province;

    public Map<String,Point2d> envelope = new HashMap<>();

    public Map<String,Point2d> links = new HashMap<>();

    public Map<Integer, Integer> link_inx_pid = new HashMap<>();

    public ArrayList<Map.Entry<String, IntCoordinate>> tileInfo = null;

    public int[][] restrictions;

    public int[][] cross_pixels = null;

    public List<TrackHitInfo> traj = new ArrayList<>();
}
