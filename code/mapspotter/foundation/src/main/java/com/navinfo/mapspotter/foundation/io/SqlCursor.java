package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.util.Logger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL数据库访问游标
 * Created by SongHuiXing on 2015/12/30.
 */
public class SqlCursor implements Cursor {
    private static final Logger logger = Logger.getLogger(SqlCursor.class);

    protected final WKTReader wktReader = new WKTReader();
    protected final WKBReader wkbReader = new WKBReader();

    protected Statement sqlStmt = null;
    protected ResultSet sqlResultSet = null;

    protected SqlCursor(Statement stmt, ResultSet rs) {
        sqlStmt = stmt;
        sqlResultSet = rs;
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
            hasNext = sqlResultSet.next();
        } catch (SQLException e) {
            logger.error(e);
        }

        return hasNext;
    }

    /**
     * 重置游标至初始位置
     */
    @Override
    public void reset() {
        try {
            sqlResultSet.beforeFirst();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    /**
     * 返回当前游标的字段值
     * @param field
     * @return
     * @throws SQLException
     */
    @Override
    public Object get(String field) throws SQLException {
        return sqlResultSet.getObject(field);
    }

    /**
     * 返回字段的字符串值
     *
     * @param fieldname 字段名称
     * @return
     */
    public String getString(String fieldname) throws SQLException {
        return sqlResultSet.getString(fieldname);
    }

    public String getString(int filedIndex) throws SQLException {
        return sqlResultSet.getString(filedIndex);
    }

    /**
     * 返回字段的整数值
     *
     * @param fieldname 字段名称
     * @return
     */
    public int getInteger(String fieldname) throws SQLException {
        return sqlResultSet.getInt(fieldname);
    }

    public int getInteger(int fieldIndex) throws SQLException {
        return sqlResultSet.getInt(fieldIndex);
    }

    /**
     * 返回字段的浮点数值
     *
     * @param fieldname 字段名称
     * @return
     */
    public double getDouble(String fieldname) throws SQLException {
        return sqlResultSet.getDouble(fieldname);
    }

    public double getDouble(int fieldIndex) throws SQLException {
        return sqlResultSet.getDouble(fieldIndex);
    }

    /**
     * 获取Bolb字段值
     * @param fieldname
     * @return
     * @throws SQLException
     */
    public InputStream getBlob(String fieldname) throws SQLException {
        return sqlResultSet.getBlob(fieldname).getBinaryStream();
    }

    public InputStream getBlob(int filedIndex) throws SQLException {
        return sqlResultSet.getBlob(filedIndex).getBinaryStream();
    }

    /**
     * 返回字段的二进制值，例如WKB
     * @param fieldname
     * @return
     * @throws Exception
     */
    public byte[] getBytes(String fieldname) throws SQLException {
        return sqlResultSet.getBytes(fieldname);
    }

    public byte[] getBytes(int fieldIndex) throws SQLException {
        return sqlResultSet.getBytes(fieldIndex);
    }

    /**
     * 返回字段的Geometry，不同的数据库需重载各自实现该方法
     * @param geoFieldname
     * @return
     * @throws Exception
     */
    public Geometry getWKTGeometry(String geoFieldname) throws Exception {
        String wkt = sqlResultSet.getString(geoFieldname);

        return wktReader.read(wkt);
    }

    public Geometry getWKTGeometry(int fieldIndex) throws Exception {
        String wkt = sqlResultSet.getString(fieldIndex);

        return wktReader.read(wkt);
    }

    public Geometry getWKBGeometry(String geoFieldname) throws Exception {
        byte[] wkb = sqlResultSet.getBytes(geoFieldname);

        return wkbReader.read(wkb);
    }

    public Geometry getWKBGeometry(int fieldIndex) throws Exception {
        byte[] wkb = sqlResultSet.getBytes(fieldIndex);

        return wkbReader.read(wkb);
    }

    /**
     * 关闭游标
     *
     * @return
     */
    @Override
    public void close() throws SQLException {
        if(null != sqlResultSet){
            sqlResultSet.close();
        }

        if(null != sqlStmt){
            sqlStmt.close();
            sqlStmt = null;
        }
    }
}
