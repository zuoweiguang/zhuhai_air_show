package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;

import java.sql.DriverManager;

/**
 * Hive数据源连接类
 * Created by gaojian on 2016/1/16.
 */
public class Hive extends SqlDatabase {
    private static final Logger logger = Logger.getLogger(Hive.class);

    // 加载Hive的JDBC Driver
    static {
        try {
            Class.forName(PropertiesUtil.getValue("HIVE_JDBC_DRIVER"));
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static String urlHeader = PropertiesUtil.getValue("HIVE_URL_HEADER");

    protected Hive() {
        super();
    }

    /**
     * 打开连接
     * @param params 数据源参数
     * @return 0：成功；-1：失败
     */
    @Override
    protected int open(DataSourceParams params) {
        try {
            sqlConnection = DriverManager.getConnection(
                    makeURL(params),
                    params.getUser(),
                    params.getPassword());
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }
        return 0;
    }

    @Override
    public void close() {
        super.close();
    }

    /**
     * 设置每个reducer处理文件大小，用于计算启动多少reducer，默认1GB
     * @param bytes 文件大小，单位Byte
     * @return 0：成功；其他：失败
     */
    public int setBytesPerReducer(int bytes) {
        String hql = "set hive.exec.reducers.bytes.per.reducer=" + String.valueOf(bytes);
        return execute(hql);
    }

    /**
     * 设置最多的reducer个数，默认999
     * @param maxNum reducer最大个数
     * @return 0：成功；其他：失败
     */
    public int setMaxReducers(int maxNum) {
        String hql = "set hive.exec.reducers.max=" + String.valueOf(maxNum);
        return execute(hql);
    }

    /**
     * 固定reducer个数，不自动计算，默认-1
     * @param num 固定reducer个数
     * @return 0：成功；其他：失败
     */
    public int setReducerNum(int num) {
        String hql = "set mapred.reduce.tasks=" + String.valueOf(num);
        return execute(hql);
    }

    /**
     * 生成hive连接URL
     * @param params 连接参数
     * @return URL
     */
    private static String makeURL(DataSourceParams params) {
        return String.format("%1$s//%2$s:%3$d/%4$s",
                urlHeader,
                params.getHost(),
                params.getPort(),
                params.getDb());
    }
}
