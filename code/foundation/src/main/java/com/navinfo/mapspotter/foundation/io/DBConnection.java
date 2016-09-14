package com.navinfo.mapspotter.foundation.io;

import java.util.List;

/**
 * 数据库访问接口
 */
public abstract class DBConnection implements AutoCloseable {
    /**
     * 执行SQL语句
     * @param sql 需要执行的SQL语句
     * @return <code>0</code>打开连接成功;
     *         <code>-1</code>打开连接失败.
     */
    public abstract int execute(String sql);

    /**
     * 通过查询语句查询数据库
     * @param sql 查询语句
     * @return 结果集游标 @see DBCursor
     */
    public abstract DBCursor query(String sql);

    /**
     * 执行多次
     * @param paramSql 参数化SQL
     * @param datarows 绑定数据
     * @return
     */
    public abstract int executeMany(String paramSql, List<List<Object>> datarows);

    /**
     * 关闭数据库连接
     * @return
     */
    public abstract void close() throws Exception;
}
