package com.navinfo.mapspotter.process.loader.storm;

import java.util.Map;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class MercatorBolt extends BaseRichBolt {
//    private static final Logger LOG = LoggerFactory.getLogger(MercatorBolt.class);
    private OutputCollector collector;


    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple tuple) {
        // TODO Auto-generated method stub
        double lng = tuple.getDoubleByField("lng");
        double lat = tuple.getDoubleByField("lat");

        MercatorUtil mkt = new MercatorUtil(1024, 12);
        String mercator = mkt.lonLat2MCode(new Coordinate(lng, lat));

        IntCoordinate _pixels = mkt.lonLat2Pixels(new Coordinate(lng, lat));
        IntCoordinate _m = mkt.pixelsInTile(_pixels);

        int x = _m.x;
        int y = _m.y;

        String key = mercator + "," + x + "," + y;

        this.collector.emit(new Values(key));
        this.collector.ack(tuple);

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // TODO Auto-generated method stub
        declarer.declare(new Fields("key"));
    }

}
