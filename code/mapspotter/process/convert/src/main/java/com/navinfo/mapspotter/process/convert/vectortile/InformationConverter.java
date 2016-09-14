package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.util.List;

/**
 * Created by cuiliang on 2016/6/16.
 */
public abstract class InformationConverter{
    public static GeometryFactory geometryFactory = new GeometryFactory();
    public DataSourceParams params = null;
    public WKBWriter wkbWriter = new WKBWriter();
    public WKBReader wkbReader = new WKBReader();

    public abstract byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType);
    public abstract byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType, String condition);

    public abstract byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList);
    public abstract byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList, String condition);


    public abstract String getGeojson(int z, int x, int y,
                                      WarehouseDataType.LayerType type);

    public abstract String getGeojson(double minx, double miny, double maxx, double maxy,
                                      WarehouseDataType.LayerType type);
}
