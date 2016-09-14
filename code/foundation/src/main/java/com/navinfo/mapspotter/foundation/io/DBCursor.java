package com.navinfo.mapspotter.foundation.io;

import com.vividsolutions.jts.geom.Geometry;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.SQLException;

/**
 * Created by SongHuiXing on 2015/12/29.
 * 访问的返回游标
 */
public abstract class DBCursor implements Cursor {

    /**
     * 是否具有下一个返回值
     * @return
     */
    @Override
    public abstract boolean next();

    /**
     * 重置游标至初始位置
     */
    @Override
    public abstract void reset();

    /**
     *
     * @return
     */
    @Override
    public Object fetch() {
        throw new NotImplementedException();
    }

    /**
     * 返回字段的字符串值
     * @param fieldname 字段名称
     * @return
     */
    public String getString(String fieldname) throws Exception {
        throw new NotImplementedException();
    }

    public String getString(int filedIndex) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * 返回字段的整数值
     * @param fieldname 字段名称
     * @return
     */
    public int getInteger(String fieldname) throws Exception {
        throw new NotImplementedException();
    }

    public int getInteger(int fieldIndex) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * 返回字段的浮点数值
     * @param fieldname 字段名称
     * @return
     */
    public double getDouble(String fieldname) throws Exception {
        throw new NotImplementedException();
    }

    public double getDouble(int fieldIndex) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * 获取几何
     * @param geoFieldname 几何字段名
     * @return JTS几何信息 @see Geometry
     */
    public Geometry getGeometry(String geoFieldname) throws Exception{
        throw new NotImplementedException();
    }

    public Geometry getGeometry(int fieldIndex) throws Exception{
        throw new NotImplementedException();
    }

    /**
     * 关闭游标
     * @return
     */
    @Override
    public void close() throws Exception {
    }
}
