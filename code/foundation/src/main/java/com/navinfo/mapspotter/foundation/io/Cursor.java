package com.navinfo.mapspotter.foundation.io;

/**
 * Created by gaojian on 2016/1/6.
 */
public interface Cursor extends AutoCloseable {
    /**
     * 游标跳转下一个
     * @return true：可以继续向下；false：已到最后
     */
    boolean next();

    /**
     * 重置游标到开始
     */
    void reset();

    /**
     * 得到当前游标
     * @return
     * @throws Exception
     */
    Object fetch();

    /**
     * 关闭游标
     * @throws Exception
     */
    void close() throws Exception;
}
