package com.navinfo.mapspotter.foundation.io.oracle;

import com.navinfo.mapspotter.foundation.io.BasicDBCursor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKB;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Oracle 数据库访问游标
 * Created by SongHuiXing on 2015/12/29.
 */
public class OracleCursor extends BasicDBCursor {

    private ResultSet oraResultset = null;

    OracleCursor(Statement stmt, ResultSet rs){
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
        byte[] st = oraResultset.getBytes(geoFieldname);

        return fromSpatialToJTS(st);
    }

    @Override
    public Geometry getGeometry(int fieldIndex) throws Exception {
        byte[] st = oraResultset.getBytes(fieldIndex);

        return fromSpatialToJTS(st);
    }

    private static Geometry fromSpatialToJTS(byte[] spatialBytes) throws Exception {
        JGeometry sdoGeo = JGeometry.load(spatialBytes);
        WKB oraWKB = new WKB();

        WKBReader reader = new WKBReader();

        return reader.read(oraWKB.fromJGeometry(sdoGeo));
    }
}
