package com.navinfo.mapspotter.foundation.io;

import com.vividsolutions.jts.geom.Geometry;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKB;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by SongHuiXing on 5/24 0024.
 */
public class OracleSharedCursor extends SqlSharedCursor {
    protected OracleSharedCursor(Statement stmt, ResultSet rs) {
        super(stmt, rs);
    }

    public byte[] getWellKnownBytes(String geoFieldname) throws Exception{
        byte[] st = sqlResultSet.getBytes(geoFieldname);

        return fromSpatialToWellKnownBytes(st);
    }

    public byte[] getWellKnownBytes(int fieldIndex) throws Exception{
        byte[] st = sqlResultSet.getBytes(fieldIndex);

        return fromSpatialToWellKnownBytes(st);
    }

    /**
     * 获取几何
     *
     * @param geoFieldname 几何字段名
     * @return JTS几何信息 @see Geometry
     */
    @Override
    public Geometry getWKBGeometry(String geoFieldname) throws Exception {
        byte[] st = getWellKnownBytes(geoFieldname);

        return wkbReader.read(st);
    }

    @Override
    public Geometry getWKBGeometry(int fieldIndex) throws Exception {
        byte[] st = getWellKnownBytes(fieldIndex);

        return wkbReader.read(st);
    }

    private static byte[] fromSpatialToWellKnownBytes(byte[] spatialBytes) throws Exception {
        JGeometry sdoGeo = JGeometry.load(spatialBytes);

        WKB oraWKB = new WKB();

        return oraWKB.fromJGeometry(sdoGeo);
    }
}
