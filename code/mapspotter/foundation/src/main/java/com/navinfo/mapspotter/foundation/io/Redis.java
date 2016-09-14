package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis操作类
 *     1.调用connect连接
 *     2.查询或修改数据
 *     3.结束后调用close
 *     tips: 也可以使用try-with-resources
 * Created by gaojian on 2016/1/3.
 */
public class Redis extends DataSource implements Cursor {
    private static final Logger logger = Logger.getLogger(Redis.class);

    private static final int PIPE_BATCH_COUNT = 2000;

    private Jedis _conn = null; //Redis sqlConnection
    private Pipeline _pipe = null; //For transaction

    private ScanResult<String> _scan = null;
    private int _index = -1;

    private int _pipeCounter = 0;

    public Redis() {
        super();
    }

    /**
     * 连接redis
     * @param params redis参数，地址，ip，密码等
     * @return 0：成功；-1：失败
     */
    @Override
    public int open(DataSourceParams params) {
        try {
            String host = params.getHost();
            int port = params.getPort();
            // 创建连接
            _conn = new Jedis(host, port, 100000);

            // 验证密码
            String password = params.getPassword();
            if (!StringUtil.isEmpty(password)) {
                String reply = _conn.auth(password);
                if (!reply.equals("OK")) {
                    close();
                    return -1;
                }
            }

            // 选择db
            String db = params.getDb();
            if (!StringUtil.isEmpty(db)) {
                int index = Integer.parseInt(db);
                String reply = _conn.select(index);
                if (!reply.equals("OK")) {
                    close();
                    return -1;
                }
            }
        } catch (Exception e) {
            logger.error(e);
            close();
            return -1;
        }

        return 0;
    }

    /**
     * 实现AutoCloseable接口，使用完毕后调用close关掉连接
     * @throws Exception
     */
    @Override
    public void close() {
        try {
            // 游标置空
            _scan = null;

            // 关闭pipeline
            if (_pipe != null) {
                _pipe.close();
            }
            _pipe = null;

            // 关闭连接
            if (_conn != null) {
                _conn.close();
            }
            _conn = null;
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * 重新scan
     */
    @Override
    public void reset() {
        ScanParams params = new ScanParams();
        params.count(10000);
        _scan = _conn.scan(ScanParams.SCAN_POINTER_START, params);

        _index = -1;
    }

    /**
     * 游标向下
     * @return
     */
    @Override
    public boolean next() {
        // 如果未初始化则reset
        if (_scan == null)
            reset();

        // 游标+1
        _index++;

        // 判断是否到底
        if (_index >= _scan.getResult().size()) {
            if (_scan.getStringCursor() == "0") {
                // 游标已到当前list的结尾且无下一组scan，则结束
                _index = _scan.getResult().size();
                return false;
            }

            // scan下一组结果
            try {
                ScanParams params = new ScanParams();
                params.count(10000);
                _scan = _conn.scan(_scan.getStringCursor(), params);
                _index = 0;
            } catch (Exception e) {
                logger.error(e);
                return false;
            }
        }

        return true;
    }

    /**
     * 用于下面的get方法
     */
    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";

    public void selectDb(int index){
        _conn.select(index);
    }

    /**
     * 获取当前key
     * @return String 当前游标key
     */
    @Override
    public Object get(String field) throws Exception {
        if (_scan == null || _index == -1 || _index >= _scan.getResult().size()) {
            throw new IllegalStateException();
        }

        String key = _scan.getResult().get(_index);

        if (field.equals(FIELD_KEY)) {
            return key;
        }

        if (field.equals(FIELD_VALUE)) {
            return query(key);
        }

        throw new IllegalArgumentException();
    }

    /**
     * 通用查询
     * @param key
     * @return 类型不同返回不同值，null表示失败
     */
    public Object query(String key) {
        try {
            // 首先判断key是否存在
            if (!exists(key)) return null;

            Object value;
            // 获取数据类型，根据类型调用对应方法获取值
            String valueType = _conn.type(key);
            if (valueType.equals("string")) {
                // String类型，返回String
                value = _conn.get(key);
            } else if (valueType.equals("hash")) {
                // Hash类型，使用hgetAll返回Map
                value = _conn.hgetAll(key);
            } else if (valueType.equals("list")) {
                // List类型，使用lrange返回List
                value = _conn.lrange(key, 0, -1);
            } else if (valueType.equals("set")) {
                // Set类型，使用smembers返回List
                value = _conn.smembers(key);
            } else if (valueType.equals("zset")) {
                // Sorted Set类型，使用zrange返回List
                value = _conn.zrange(key, 0, -1);
            } else {
                // 暂不支持的类型
                value = null;
            }

            return value;

        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public boolean exists(String key) {
        return _conn.exists(key);
    }

    public String queryString(String key) {
        try {
            return _conn.get(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public Map<String, String> queryMap(String key) {
        try {
            return _conn.hgetAll(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public String queryMap(String key, String field) {
        try {
            return _conn.hget(key, field);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public List<String> queryList(String key) {
        try {
            return _conn.lrange(key, 0, -1);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public String queryList(String key, long index) {
        try {
            return _conn.lindex(key, index);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public Set<String> querySet(String key) {
        try {
            return _conn.smembers(key);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public Set<String> queryZSet(String key) {
        try {
            return _conn.zrange(key, 0, -1);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下为更新相关方法

    private void pipeCount() {
        if (++_pipeCounter % PIPE_BATCH_COUNT == 0) {
            _pipe.sync();
        }
    }

    public void updateString(String key, String value) {
        try {
            if (_pipe != null) {
                _pipe.set(key, value);
                pipeCount();
            } else {
                _conn.set(key, value);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void updateMap(String key, String field, String value) {
        try {
            if (_pipe != null) {
                _pipe.hset(key, field, value);
                pipeCount();
            } else {
                _conn.hset(key, field, value);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void updateMap(String key, Map<String, String> map) {
        try {
            if (_pipe != null) {
                _pipe.hmset(key, map);
                pipeCount();
            } else {
                _conn.hmset(key, map);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void updateList(String key, long index, String value) {
        try {
            if (_pipe != null) {
                _pipe.lset(key, index, value);
                pipeCount();
            } else {
                _conn.lset(key, index, value);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void updateList(String key, String value) {
        try {
            if (_pipe != null) {
                _pipe.rpush(key, value);
                pipeCount();
            } else {
                _conn.rpush(key, value);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void incr(String key) {
        try {
            if (_pipe != null) {
                _pipe.incr(key);
                pipeCount();
            } else {
                _conn.incr(key);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void incr(String key, long increment) {
        try {
            if (_pipe != null) {
                _pipe.incrBy(key, increment);
                pipeCount();
            } else {
                _conn.incrBy(key, increment);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void incr(String key, double increment) {
        try {
            if (_pipe != null) {
                _pipe.incrByFloat(key, increment);
                pipeCount();
            } else {
                _conn.incrByFloat(key, increment);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void incr(String key, String field, long increment) {
        try {
            if (_pipe != null) {
                _pipe.hincrBy(key, field, increment);
                pipeCount();
            } else {
                _conn.hincrBy(key, field, increment);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void incr(String key, String field, double increment) {
        try {
            if (_pipe != null) {
                _pipe.hincrByFloat(key, field, increment);
                pipeCount();
            } else {
                _conn.hincrByFloat(key, field, increment);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void decr(String key) {
        try {
            if (_pipe != null) {
                _pipe.decr(key);
                pipeCount();
            } else {
                _conn.decr(key);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void decr(String key, long decrement) {
        try {
            if (_pipe != null) {
                _pipe.decrBy(key, decrement);
                pipeCount();
            } else {
                _conn.decrBy(key, decrement);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 以下为删除相关方法

    public void del(String key) {
        try {
            if (_pipe != null) {
                _pipe.del(key);
                pipeCount();
            } else {
                _conn.del(key);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void delMap(String key, String field) {
        try {
            if (_pipe != null) {
                _pipe.hdel(key, field);
                pipeCount();
            } else {
                _conn.hdel(key, field);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public String delList(String key) {
        try {
            if (_pipe != null) {
                _pipe.rpop(key);
                pipeCount();
                return null;
            } else {
                return _conn.rpop(key);
            }
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * 使用Pipeline来实现类似事务，目的是提高处理效率，无法回滚
     * @return 0：成功；-1：失败
     */
    public int transaction() {
        try {
            _pipe = _conn.pipelined();
            _pipeCounter = 0;
        } catch (Exception e) {
            logger.error(e);
            _pipe = null;
            return -1;
        }
        return 0;
    }

    /**
     * 使用pipe存储二进制
     * @param key
     * @param value
     * @return
     */
    public int pipeSet(String key, byte[] value){
        try {
            if(null == _pipe)
                return -1;

            _pipe.set(key.getBytes(), value);

            pipeCount();
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }

        return 0;
    }

    public byte[] pipeGet(String key){
        try {
            if(null == _pipe)
                return new byte[0];

            Response<byte[]> response = _pipe.get(key.getBytes());

            return response.get();
        } catch (Exception e) {
            logger.error(e);
        }

        return new byte[0];
    }

    /**
     * Pipeline同步
     * @return 0：成功；-1：失败
     */
    public int commit() {
        try {
            _pipe.sync();
            _pipe.close();
            _pipeCounter = 0;
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }
        _pipe = null;
        return 0;
    }

    /**
     * 删除当前DB中的所有key
     * @return 0：成功；-1：失败
     */
    public int clear() {
        try {
            _conn.flushDB();
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }

        return 0;
    }

}
