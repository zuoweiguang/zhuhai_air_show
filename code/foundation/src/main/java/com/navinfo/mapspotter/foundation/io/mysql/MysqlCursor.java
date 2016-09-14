package com.navinfo.mapspotter.foundation.io.mysql;

import com.navinfo.mapspotter.foundation.io.BasicDBCursor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by SongHuiXing on 2015/12/30.
 */
public class MysqlCursor extends BasicDBCursor {

    protected MysqlCursor(Statement stmt, ResultSet rs) {
        super(stmt, rs);
    }

    /**
     * 获取几何
     *
     * @param geoFieldname 几何字段名
     * @return JTS几何信息 @see Geometry
     */
    @Override
    public Geometry getGeometry(String geoFieldname) throws Exception {
        String wkt = sqlResultset.getString(geoFieldname);

        WKTReader reader = new WKTReader();

        return reader.read(wkt);
    }

    @Override
    public Geometry getGeometry(int fieldIndex) throws Exception {
        return sqlResultset.getObject(fieldIndex, Geometry.class);
    }
}
