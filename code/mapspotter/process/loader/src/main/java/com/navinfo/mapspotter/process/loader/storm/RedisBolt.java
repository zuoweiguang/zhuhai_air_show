package com.navinfo.mapspotter.process.loader.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;

import com.navinfo.mapspotter.foundation.io.Redis;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.DateTimeUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiliang on 2016/3/29.
 */
public class RedisBolt extends BaseRichBolt {

    private OutputCollector collector;
    private Map<String, Integer> counts = null;

    private String host;
    private int post;
    private String auth;

    private RedisUtil redis;

    public RedisBolt(String host, int post, String auth){
        this.host = host;
        this.post = post;
        this.auth = auth;

        redis = new RedisUtil(host,post,auth);
        this.counts = new HashMap();
    }


    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {


        String key = tuple.getStringByField("key");

        Integer count = this.counts.get(key);
        if(count == null){
            count = 0;
        }
        count++;
        this.counts.put(key, count);

        if(counts.size() > 50000){
            redis.updateRedis(counts);
            counts.clear();
        }
    }

    class RedisUtil implements Serializable{

        private Redis redis;
        public RedisUtil(String host, int post ,String auth){
            DataSourceParams params = IOUtil.makeRedisParam(host, post, auth);
            redis = (Redis)DataSource.getDataSource(params);
        }

        public void updateRedis(Map<String, Integer> map){
            redis.transaction();

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();
                String date = DateTimeUtil.formatDate("MMdd");
                redis.incr("amount", date, Integer.valueOf(value));
                String hour = DateTimeUtil.formatDate("yyyyMMddHH");
                redis.incr("amountByHour", hour, Integer.valueOf(value));
                redis.incr(key, date, Integer.valueOf(value));
            }
            redis.commit();
        }
    }



    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
