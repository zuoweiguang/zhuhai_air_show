package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.util.XmlUtil;

/**
 * Created by cuiliang on 2016/1/7.
 */
public class Constants {

    public final static String ROAD_DETECT_TABLE = "road_detect_12";

    public final static String ROAD_DETECT_ROAD_FAMILY = "road";

    public final static String ROAD_DETECT_SOURCE_FAMILY = "source";

    public final static String ROAD_DETECT_DETECT_FAMILY = "detect";


    public final static String ROAD_DETECT_ROAD_QUALIFIER = "data";
    public final static String ROAD_DETECT_TUNNEL_QUALIFIER = "tunnel";

    public final static String SOURCE_DIDI = "didi";

    public final static String SOURCE_SOGOU = "sogou";

    public final static String SOURCE_FLOAT = "float";

    public final static String SOURCE_BAIDU = "baidu";

    public static float HIGH_PERCENT_THRESHOLD = 0.99f;

    public static float LOW_PERCENT_THRESHOLD = 0.90f;

}
