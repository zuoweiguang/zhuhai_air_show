package com.navinfo.mapspotter.process.convert.vectortile;

import com.mercator.TileUtils;
import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.process.convert.dig2mongo.ExtractDig2Mongo;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.bson.Document;
import org.geojson.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从Postgre数据库转换Protobuf
 * Created by SongHuiXing on 6/6 0006.
 */
public class MgConverter extends InformationConverter {

    private final Logger logger = Logger.getLogger(MgConverter.class);
    private MongoDB database = null;

    public MgConverter(MongoDB db) {
        database = db;
    }

    public boolean setup() {
        if(null != database)
            return true;

        database = (MongoDB) DataSource.getDataSource(params);

        return database != null;
    }

    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType) {
        return getProtobuf(z, x, y, WarehouseDataType.getLayers(srcType));
    }

    public byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList) {

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

        for (WarehouseDataType.LayerType type : typeList) {
            switch (type) {
                case Poi:
                    try {
                        if(z >= 14 && z <= 17)
                            writePoi(vtm, z, x, y);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case Missingroad:
                    writeMissRoad(vtm, z, x, y);
                    break;
                case Construction:
                    writeConstruction(vtm, z, x, y);
                    break;
                case RestricDetail:
                    writeRestriction(vtm, z, x, y);
                    break;
            }
        }
        return vtm.encode();
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType, String condition) {
        return getProtobuf(z, x, y, WarehouseDataType.getLayers(srcType), condition);
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList, String condition) {

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

        for (WarehouseDataType.LayerType type : typeList) {
            switch (type) {
                case InfoPoi:
                    int infoPoiCount = writeInfoPoi(vtm, z, x, y, condition);
                    System.out.println("vtm infoPoi total:" + infoPoiCount);
                    break;
                case InfoRoad:
                    int infoRoadCount = writeInfoRoad(vtm, z, x, y, condition);
                    System.out.println("vtm infoRoad total:" + infoRoadCount);
                    break;
            }
        }
        return vtm.encode();
    }

    private int writePoi(VectorTileEncoder encoder,
                         int z, int x, int y) throws Exception {
        int count = 0;
        MongoOperator condition = new MongoOperator();

        String key = "tile_" + z;
        String value = x + "_" + y;
        condition.and(MongoOperator.FilterType.EQ, key, value);
        MongoDBCursor cursor = (MongoDBCursor)database.query("poi",condition);

        while (cursor.next()){
            double x_coord = (Double)cursor.get("x");
            double y_coord = (Double)cursor.get("y");

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("pid", cursor.get("pid"));
            attributes.put("mainkind", cursor.get("mainkind"));
            attributes.put("kindcode", cursor.get("kindcode"));
            attributes.put("form_name", cursor.get("form_name"));
            attributes.put("level", cursor.get("level"));
            attributes.put("contact", cursor.get("contact"));

            Coordinate coordinate = new Coordinate(x_coord, y_coord);

            Point point = geometryFactory.createPoint(coordinate);
            TileUtils.convert2Piexl(x, y, z, point);

            encoder.addFeature(WarehouseDataType.LayerType.Poi.toString(),
                    attributes, point);
            count++;
        }
        return count;
    }

    private int writeMissRoad(VectorTileEncoder encoder,
                              int z, int x, int y){
        int count = 0;
        MongoOperator condition = new MongoOperator();

        String key = "tile_" + z;
        String value = x + "_" + y;
        condition.and(MongoOperator.FilterType.EQ, key, value);
        MongoDBCursor cursor = (MongoDBCursor)database.query("qingbao_road_20160531",condition);

        while (cursor.next()){
            try {
                HashMap<String, Object> attrs = new HashMap<>();
                attrs.put("time", cursor.get("timestamp"));

                Map<String, Object> props = (Map<String, Object>) cursor.get("properties");
                attrs.put("confidence", props.get("confidence"));

                String linegeo = ((Document)cursor.get("geometry")).toJson();
                LineString line =
                        ExtractDig2Mongo.convertLineString(JsonUtil.getInstance().readValue(linegeo,
                                org.geojson.LineString.class));

                TileUtils.convert2Piexl(x, y, z, line);

                encoder.addFeature(WarehouseDataType.LayerType.Missingroad.toString(),
                        attrs, line);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            count++;
        }

        return count;
    }

    private int writeConstruction(VectorTileEncoder encoder,
                                  int z, int x, int y){
        int count = 0;
        MongoOperator condition = new MongoOperator();

        String key = "tile_" + z;
        String value = x + "_" + y;
        condition.and(MongoOperator.FilterType.EQ, key, value);
        MongoDBCursor cursor = (MongoDBCursor)database.query("sogou_construction_201603",condition);

        while (cursor.next()){
            try {
                HashMap<String, Object> attrs = new HashMap<>();
                attrs.put("time", cursor.get("timestamp"));

                Map<String, Object> props = (Map<String, Object>)cursor.get("properties");
                attrs.put("confidence", props.get("confidence"));

                String linegeo = ((Document)cursor.get("geometry")).toJson();
                LineString line =
                        ExtractDig2Mongo.convertLineString(JsonUtil.getInstance().readValue(linegeo,
                                org.geojson.LineString.class));

                TileUtils.convert2Piexl(x, y, z, line);

                encoder.addFeature(WarehouseDataType.LayerType.Construction.toString(),
                        attrs, line);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


            count++;
        }

        return count;
    }

    private int writeRestriction(VectorTileEncoder encoder,
                                 int z, int x, int y) {
        int count = 0;
        MongoOperator condition = new MongoOperator();

        String key = "tile_" + z;
        String value = x + "_" + y;
        condition.and(MongoOperator.FilterType.EQ, key, value);
        MongoDBCursor cursor = (MongoDBCursor)database.query("sogou_restric_detail_20160331",condition);

        while (cursor.next()){
            try {
                HashMap<String, Object> attrs = new HashMap<>();

                Map<String, Object> props = (Map<String, Object>) cursor.get("properties");

                attrs.put("type", props.get("type"));
                attrs.put("confidence", props.get("confidence"));
                attrs.put("res", props.get("res"));
                attrs.put("linkin", props.get("linkin"));
                attrs.put("linkout", props.get("linkout"));
                attrs.put("provinceid", props.get("provinceid"));

                String linegeo = ((Document)cursor.get("geometry")).toJson();
                org.geojson.Point ptJson = JsonUtil.getInstance().readValue(linegeo,
                        org.geojson.Point.class);
                org.geojson.LngLatAlt c = ptJson.getCoordinates();

                Point pt = geometryFactory.createPoint(new Coordinate(c.getLongitude(), c.getLatitude()));

                TileUtils.convert2Piexl(x, y, z, pt);

                encoder.addFeature(WarehouseDataType.LayerType.RestricDetail.toString(),
                        attrs, pt);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            count++;
        }

        return count;
    }

    private int writeInfoPoi(VectorTileEncoder vte, int z, int x, int y, String condition) {

        int count = 0;

        try {

            MongoOperator opt = new MongoOperator();
            String key = "tile_" + z;
            String value = x + "_" + y;
            opt.and(MongoOperator.FilterType.EQ, key, value);
            MongoDBCursor cursor = (MongoDBCursor)database.query("information_main", opt);

            while (cursor.next()) {
                try {

                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("id", cursor.get("globalId"));
                    attributes.put("type", "information");
                    attributes.put("b_featureKind", cursor.get("b_featureKind"));
                    attributes.put("b_sourceCode", cursor.get("b_sourceCode"));
                    attributes.put("t_status", cursor.get("t_status"));
                    attributes.put("i_proposal", cursor.get("i_proposal"));
                    attributes.put("i_infoName", cursor.get("i_infoName"));
                    attributes.put("g_location", cursor.get("g_location"));

                    Document g_location = (Document) cursor.get("g_location");
                    String type = (String) g_location.get("type");
                    if (type.equals("Point")) {
                        String pointJsonStr = g_location.toJson();
                        org.geojson.Point pointJson = JsonUtil.getInstance().readValue(pointJsonStr, org.geojson.Point.class);
                        org.geojson.LngLatAlt c = pointJson.getCoordinates();

                        Point pt = geometryFactory.createPoint(new Coordinate(c.getLongitude(), c.getLatitude()));

                        TileUtils.convert2Piexl(x, y, z, pt);
                        vte.addFeature(WarehouseDataType.LayerType.InfoPoi.toString(), attributes, pt);
                    }

                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return count;
        }
    }


    private int writeInfoRoad(VectorTileEncoder vte, int z, int x, int y, String condition) {

        int count = 0;

        try {

            MongoOperator opt = new MongoOperator();
            String key = "tile_" + z;
            String value = x + "_" + y;
            opt.and(MongoOperator.FilterType.EQ, key, value);
            MongoDBCursor cursor = (MongoDBCursor)database.query("information_main", opt);

            while (cursor.next()) {
                try {

                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("id", cursor.get("globalId"));
                    attributes.put("type", "information");
                    attributes.put("b_featureKind", cursor.get("b_featureKind"));
                    attributes.put("b_sourceCode", cursor.get("b_sourceCode"));
                    attributes.put("t_status", cursor.get("t_status"));
                    attributes.put("i_proposal", cursor.get("i_proposal"));
                    attributes.put("i_infoName", cursor.get("i_infoName"));
                    attributes.put("g_location", cursor.get("g_location"));

                    Document g_location = (Document) cursor.get("g_location");
                    String type = (String) g_location.get("type");
                    if (type.equals("LineString")) {
                        String geomJsonStr = g_location.toJson();
                        org.geojson.LineString slGeoJson = JsonUtil.getInstance().readValue(geomJsonStr, org.geojson.LineString.class);
                        List<LngLatAlt> coordinates = slGeoJson.getCoordinates();

                        Coordinate[] lineStringList = new Coordinate[coordinates.size()];
                        for (int i = 0; i < coordinates.size(); i++) {
                            LngLatAlt point = (LngLatAlt) coordinates.get(i);
                            double lon = point.getLongitude();
                            double lat = point.getLatitude();
                            Coordinate coordinate = new Coordinate(lon, lat);
                            lineStringList[i] = coordinate;
                        }
                        LineString lineString = geometryFactory.createLineString(lineStringList);
                        TileUtils.convert2Piexl(x, y, z, lineString);
                        vte.addFeature(WarehouseDataType.LayerType.InfoRoad.toString(), attributes, lineString);
                    }

                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return count;
        }
    }

    public String getGeojson(int z, int x, int y, WarehouseDataType.LayerType type){
        Envelope bounds = MercatorUtil.mercatorBound(z, x, y);

        return getGeojson(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY(),
                          type);
    }

    public String getGeojson(double minx, double miny, double maxx, double maxy,
                             WarehouseDataType.LayerType type){

        switch (type){
            case Road:
                break;
            case RailWay:
                break;
            case Admin:
                break;
            case AdminBoundary:
                break;
            case AdminFlag:
                break;
            case LU:
                break;
            case LC:
                break;
            case CityModel:
                break;
            case Poi:
                break;
            case PoiDayEditHeatmap:
                return getPoiEditHeatDatas(minx, miny, maxx, maxy, "day_edit").toString();
            case PoiMonthEditHeatmap:
                return getPoiEditHeatDatas(minx, miny, maxx, maxy, "month_edit").toString();
        }

        return "";
    }

    private FeatureCollection getPoiEditHeatDatas(double minx, double miny, double maxx, double maxy,
                                                  String keyfield){
        FeatureCollection features = new FeatureCollection();

        MongoOperator condition = new MongoOperator();

        condition.and(MongoOperator.FilterType.EQ, keyfield, 0);
        condition.ands(MongoOperator.FilterType.GeoWithinBox, "loc", minx, miny, maxx, maxy);

        long t1 = System.currentTimeMillis();

        MongoDBCursor cursor = (MongoDBCursor)database.query("poi", condition);

        long t2 = System.currentTimeMillis();

        System.out.println("query:" + (t2-t1));

        while (cursor.next()){

            try {
                Feature ft = new Feature();

                double x_coord = (Double)cursor.get("x");
                double y_coord = (Double)cursor.get("y");
                ft.setGeometry(new org.geojson.Point(x_coord, y_coord));

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("pid", cursor.get("pid"));
                attributes.put("kindcode", cursor.get("kindcode"));
                attributes.put("form_name", cursor.get("form_name"));

                ft.setProperties(attributes);

                features.add(ft);
            } catch (Exception e) {

            }
        }

        return features;
    }
}
