package com.navinfo.mapspotter.foundation.io;

/**
 * Created by gaojian on 2016/1/6.
 */
public interface Connection extends AutoCloseable {
    /**
     * 连接数据库或文件
     * @param params
     * @return 0：成功；-1：失败
     */
    int connect(ConnectParams params) throws Exception;

    /**
     * 关闭连接
     * @throws Exception
     */
    @Override
    void close() throws Exception;
}
