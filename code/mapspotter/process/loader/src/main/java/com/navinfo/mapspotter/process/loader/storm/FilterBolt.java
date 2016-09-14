package com.navinfo.mapspotter.process.loader.storm;

import Didi2Siwei.DidiSiweiProtocol;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.navinfo.mapspotter.foundation.util.CoordinateUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Vector;

/**
 * Created by cuiliang on 2016/3/23.
 * 过滤异常数据
 */
public class FilterBolt extends BaseRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(FilterBolt.class);
    private OutputCollector collector;

    private Vector v;
    private String fileName;

    public FilterBolt(String fileName){
        this.fileName = fileName;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        v = new Vector();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)));
            String line;
            while ((line = br.readLine()) != null) {
                v.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String userId = tuple.getStringByField("userId");
        double lng = tuple.getDoubleByField("lng");
        double lat = tuple.getDoubleByField("lat");
        double speed = tuple.getDoubleByField("speed") * 3.6;
        double direction = tuple.getDoubleByField("direction");
        long timestamp = tuple.getLongByField("timestamp");
        int isGps = tuple.getIntegerByField("isGps");
        int sourceType = tuple.getIntegerByField("sourceType");
        String coordType = tuple.getStringByField("coordType");

        if(isGps != Constants.IS_GPS){
            return;
        }
        if(sourceType != DidiSiweiProtocol.DataSourceVendor.DD_TAXI_VALUE) {
            return;
        }
        if(speed < 0 || speed >= 250){
            return;
        }
        if(direction < 0 || direction > 360){
            return;
        }
        if(userId.length() != 11){
            return;
        }
        if(timestamp <= 0){
            return;
        }
        if (coordType.equals(Constants.WGS_84)) {
            return;
        }
        else if (coordType.equals(Constants.BD_09)){
            double[] lnglat = CoordinateUtil.bd_decrypt(lng, lat);
            lng = lnglat[0];
            lat = lnglat[1];
        }

        MercatorUtil mkt = new MercatorUtil(1024, 12);
        String mercator = mkt.lonLat2MCode(new Coordinate(lng, lat));
        if(!v.contains(mercator)){
            return;
        }

        this.collector.emit(new Values(userId, lng, lat, speed, direction, timestamp));
        this.collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("userId", "lng", "lat", "speed", "direction", "timestamp"));
    }
}
