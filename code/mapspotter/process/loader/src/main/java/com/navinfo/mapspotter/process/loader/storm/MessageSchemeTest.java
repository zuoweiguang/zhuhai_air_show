package com.navinfo.mapspotter.process.loader.storm;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by cuiliang on 2016/4/5.
 */
public class MessageSchemeTest implements Scheme {
    @Override
    public List<Object> deserialize(byte[] bytes) {

        String msg = null;
        try {
            msg = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //System.out.println(msg);
        String userId = msg.split(",")[0];
        double lng = Double.parseDouble(msg.split(",")[1]);
        double lat = Double.parseDouble(msg.split(",")[2]);

        long timestamp = Long.parseLong(msg.split(",")[3]);
        double speed = Double.parseDouble(msg.split(",")[4]);
        double direction = Double.parseDouble(msg.split(",")[5]);

        int isGps = Constants.NOT_GPS;
        if (msg.split(",")[7].equals("LOC_GPS")) {
            isGps = Constants.IS_GPS;
        }
        String coordType = msg.split(",")[8];


        int sourceType = -1;
        if (msg.split(",")[9].equals("DD_TAXI")) {
            sourceType = 0;
        }

        return new Values(userId, lng, lat, speed, direction, timestamp, isGps, sourceType, coordType);
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("userId", "lng", "lat", "speed", "direction", "timestamp", "isGps", "sourceType", "coordType");
    }
}
