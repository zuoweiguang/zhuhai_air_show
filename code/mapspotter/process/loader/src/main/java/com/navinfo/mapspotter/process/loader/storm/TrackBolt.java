package com.navinfo.mapspotter.process.loader.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.model.DidiCarTrack;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cuiliang on 2016/3/29.
 */
public class TrackBolt extends BaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(TrackBolt.class);
    private OutputCollector collector;
    private Map<String, CarTrack> trackMap = null;
    private SortedMap<Long, Vector<String>> timeMap = null;
    private int interval;

    public TrackBolt(int interval) {
        this.interval = interval;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.trackMap = new ConcurrentHashMap();
        this.timeMap = new TreeMap();
    }

    @Override
    public void execute(Tuple tuple) {
        String userId = tuple.getStringByField("userId");
        double lng = tuple.getDoubleByField("lng");
        double lat = tuple.getDoubleByField("lat");
        double speed = tuple.getDoubleByField("speed");
        double direction = tuple.getDoubleByField("direction");
        long timestamp = tuple.getLongByField("timestamp");

        dealMap(trackMap, timeMap, timestamp);

        CarTrack carTrack = trackMap.get(userId);
        if (carTrack == null) {
            carTrack = new DidiCarTrack(userId);
            Vector v = timeMap.get(timestamp);
            if (v == null) {
                v = new Vector();
            }
            v.add(userId);
            timeMap.put(timestamp, v);
        }
        String[] array = {String.valueOf(lng), String.valueOf(lat), String.valueOf(speed), String.valueOf(direction), String.valueOf(timestamp)};
        int code = carTrack.pushPoint(array);

        if (-1 == code) {
            //    logger.info("");
        } else if (1 == code) {
            String json = null;
            try {
                json = JsonUtil.getInstance().write2String(carTrack);
                System.out.println(json);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            carTrack = new DidiCarTrack(userId);
            carTrack.pushPoint(array);
        }
        //System.out.println(trackMap.size());
        trackMap.put(userId, carTrack);


    }

    public void dealMap(Map<String, CarTrack> trackMap, SortedMap<Long, Vector<String>> timeMap, long timestamp) {

        Iterator it = timeMap.headMap(timestamp - interval).entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Long, Vector<String>> entry = (Map.Entry)it.next();
            for (String userId : entry.getValue()) {
                CarTrack track = trackMap.get(userId);
                String json = null;
                try {
                    json = JsonUtil.getInstance().write2String(track);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                trackMap.remove(userId);

                if(json != null){
                    this.collector.emit(new Values(json));
                }
            }
            it.remove();
        }

//        for (Map.Entry<Long, Vector<String>> entry : timeMap.headMap(timestamp - interval).entrySet()) {
//            for (String userId : entry.getValue()) {
//                CarTrack track = trackMap.get(userId);
//                String json = null;
//                try {
//                    json = JsonUtil.getInstance().write2String(track);
//
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//                trackMap.remove(userId);
//                this.collector.emit(new Values(json));
//            }
//            timeMap.remove(entry.getKey());
//        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("json"));
    }
}
