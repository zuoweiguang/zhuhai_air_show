package com.navinfo.mapspotter.foundation.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by SongHuiXing on 2015/12/30.
 */
public class BasicDBCursor extends DBCursor {
    protected Statement sqlStmt = null;
    protected ResultSet sqlResultset = null;

    protected BasicDBCursor(Statement stmt, ResultSet rs){
        sqlStmt = stmt;
        sqlResultset = rs;
    }

    /**
     * 是否具有下一个返回值
     *
     * @return
     */
    @Override
    public boolean next() {
        boolean hasNext = false;
        try {
            hasNext = sqlResultset.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hasNext;
    }

    /**
     * 重置游标至初始位置
     */
    @Override
    public void reset() {
        try {
            sqlResultset.beforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回字段的字符串值
     *
     * @param fieldname 字段名称
     * @return
     */
    @Override
    public String getString(String fieldname) throws SQLException {
        return sqlResultset.getString(fieldname);
    }

    @Override
    public String getString(int filedIndex) throws SQLException {
        return sqlResultset.getString(filedIndex);
    }

    /**
     * 返回字段的整数值
     *
     * @param fieldname 字段名称
     * @return
     */
    @Override
    public int getInteger(String fieldname) throws SQLException {
        return sqlResultset.getInt(fieldname);
    }

    @Override
    public int getInteger(int fieldIndex) throws SQLException {
        return sqlResultset.getInt(fieldIndex);
    }

    /**
     * 返回字段的浮点数值
     *
     * @param fieldname 字段名称
     * @return
     */
    @Override
    public double getDouble(String fieldname) throws SQLException {
        return sqlResultset.getDouble(fieldname);
    }

    @Override
    public double getDouble(int fieldIndex) throws SQLException {
        return sqlResultset.getDouble(fieldIndex);
    }

    /**
     * 关闭游标
     *
     * @return
     */
    @Override
    public void close() throws SQLException {
        if(null != sqlResultset){
            sqlResultset.close();
        }

        if(null != sqlStmt){
            sqlStmt.close();
        }
    }
}
