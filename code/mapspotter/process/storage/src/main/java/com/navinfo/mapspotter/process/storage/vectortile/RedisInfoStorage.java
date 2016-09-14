package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import redis.clients.jedis.*;

/**
 * Created by SongHuiXing on 6/16 0016.
 */
public class RedisInfoStorage implements IPbfStorage, AutoCloseable {
    private final String redisHost;
    private final int redisPort;
    private final String redisPwd;

    public RedisInfoStorage(String host, int port, String password){
        redisHost = host;
        redisPort = port;
        redisPwd = password;
    }

    private JedisPool pool = null;

    private JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(100);

            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
            config.setMaxIdle(5);

            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
            config.setMaxWaitMillis(1000 * 100);

            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            config.setTestOnBorrow(true);

            pool = new JedisPool(config, redisHost, redisPort, 100000, redisPwd);
        }

        return pool;
    }

    @Override
    public boolean open() {
        try {
            getPool();
        } catch (Exception e){
            return false;
        }

        return null != pool;
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType target) {
        String key = target.toString().toLowerCase() + "_" + z + "_" + x + "_" + y;

        Jedis redis = null;

        try {
            redis = getPool().getResource();
            if(null == redis)
                return new byte[0];

            if(target == WarehouseDataType.SourceType.PoiHeatMap)
                redis.select(3);
            else if (target == WarehouseDataType.SourceType.BaoTouTraffic) {
                redis.select(2);
            }
            else
                redis.select(1);

            byte[] value = redis.get(key.getBytes());

            if (null != value) {
                return value;
            }
        } finally {
            if(null != redis)
                redis.close();
        }

        return new byte[0];
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.LayerType target) {
        String key = target.toString().toLowerCase() + "_" + z + "_" + x + "_" + y;

        Jedis redis = null;

        try {
            redis = getPool().getResource();
            if(null == redis)
                return new byte[0];

            redis.select(2);

            byte[] value = redis.get(key.getBytes());

            if (null != value) {
                return value;
            }
        } finally {
            if(null != redis)
                redis.close();
        }

        return new byte[0];
    }

    @Override
    public void close() {
        if(null != pool){
            pool.close();
            pool = null;
        }
    }
}
