package com.navinfo.mapspotter.foundation.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.geojson.LngLatAlt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Geometry相关公共函数
 *
 * Created by gaojian on 2016/1/25.
 */
public class GeoUtil {
    private static GeometryFactory factory = new GeometryFactory();
    private static WKTReader reader = new WKTReader(factory);
    private static WKTWriter writer = new WKTWriter();
    private static WKBWriter wkbWriter = new WKBWriter();

    public static String coordinate2wkt(double x, double y) {
        return createPoint(x, y).toText();
    }

    public static Geometry createPoint(double x, double y) {
        return factory.createPoint(new Coordinate(x, y));
    }

    public static Geometry createLine(Coordinate[] coordinates){
        return factory.createLineString(coordinates);
    }
    public static Geometry createPolygon(Coordinate[] coordinates) {
        return factory.createPolygon(coordinates);
    }

    public static Geometry createPolygon(Envelope envelope){
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(envelope.getMinX() , envelope.getMinY());
        coordinates[1] = new Coordinate(envelope.getMaxX() , envelope.getMinY());
        coordinates[2] = new Coordinate(envelope.getMaxX() , envelope.getMaxY());
        coordinates[3] = new Coordinate(envelope.getMinX() , envelope.getMaxY());
        coordinates[4] = new Coordinate(envelope.getMinX() , envelope.getMinY());

        return createPolygon(coordinates);
    }

    public static Geometry convert(Envelope envelope){
        Coordinate[] coords = new Coordinate[5];

        coords[0] = new Coordinate(envelope.getMinX(), envelope.getMinY());
        coords[1] = new Coordinate(envelope.getMinX(), envelope.getMaxY());
        coords[2] = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
        coords[3] = new Coordinate(envelope.getMaxX(), envelope.getMinY());
        coords[4] = coords[0];

        return factory.createPolygon(coords);
    }

    public static Geometry wkt2Geometry(String wkt) {
        try {
            return reader.read(wkt);
        } catch (Exception e) {
            return null;
        }
    }

    public static String geometry2WKT(Geometry geometry){
        return writer.write(geometry);
    }

    public static byte[] geometry2WKB(Geometry geometry){
        return wkbWriter.write(geometry);
    }

    public static Geometry readGeojson(String json) throws IOException {

        JsonUtil util = JsonUtil.getInstance();

        Map<String, Object> dic = util.readMap(json);

        String type = dic.get("type").toString();
        if(null == type){
            throw new IllegalArgumentException("input json: " + json + "is not illegal.");
        }

        if(type.equals("Point")){
            org.geojson.Point pt = util.readValue(json, org.geojson.Point.class);
            LngLatAlt c = pt.getCoordinates();

            return factory.createPoint(new Coordinate(c.getLongitude(), c.getLatitude()));
        } else if(type.equals("LineString")){
            org.geojson.LineString line = util.readValue(json, org.geojson.LineString.class);

            List<LngLatAlt> cs = line.getCoordinates();

            Coordinate[] coordinates = new Coordinate[cs.size()];
            for (int i=0;i<cs.size();i++){
                LngLatAlt c = cs.get(i);
                coordinates[i] = new Coordinate(c.getLongitude(), c.getLatitude());
            }

            return factory.createLineString(coordinates);
        } else if(type.equals("Polygon")){
            org.geojson.Polygon polygon = util.readValue(json, org.geojson.Polygon.class);

            List<LngLatAlt> exring = polygon.getExteriorRing();
            Coordinate[] exc = new Coordinate[exring.size()];
            for (int i=0;i<exring.size();i++){
                LngLatAlt c = exring.get(i);
                exc[i] = new Coordinate(c.getLongitude(), c.getLatitude());
            }

            List<List<LngLatAlt>> inners = polygon.getInteriorRings();

            if(inners.size() == 0) {
                return factory.createPolygon(exc);
            }

            LinearRing exRing = factory.createLinearRing(exc);

            LinearRing[] innerRings = new LinearRing[inners.size()];
            for (int j=0; j<inners.size(); j++){
                List<LngLatAlt> innerCoors = inners.get(j);

                Coordinate[] innerCoord = new Coordinate[innerCoors.size()];
                for (int k=0;k<innerCoors.size();k++){
                    LngLatAlt c = innerCoors.get(k);
                    innerCoord[k] = new Coordinate(c.getLongitude(), c.getLatitude());
                }

                innerRings[j] = factory.createLinearRing(innerCoord);
            }

            return factory.createPolygon(exRing, innerRings);
        }

        throw new IllegalArgumentException("Not support " + type + " yet.");
    }

    public static String writeGeojson(Geometry geometry) throws JsonProcessingException {
        String type = geometry.getGeometryType();

        JsonUtil jsonUtil = JsonUtil.getInstance();

        if(type.equals("Point")){
            Coordinate coord = geometry.getCoordinate();

            org.geojson.Point pt = new org.geojson.Point(coord.x, coord.y);

            return jsonUtil.write2String(pt);
        } else if(type.equals("LineString")){
            Coordinate[] coordinates = geometry.getCoordinates();

            org.geojson.LineString line = new org.geojson.LineString();

            for (Coordinate c : coordinates){
                line.add(new LngLatAlt(c.x, c.y));
            }

            return jsonUtil.write2String(line);

        } else if(type.equals("Polygon")){
            org.geojson.Polygon polygon = new org.geojson.Polygon();

            Polygon poly = (Polygon)geometry;

            Coordinate[] ex = poly.getExteriorRing().getCoordinates();
            List<LngLatAlt> exCoords = new ArrayList<>(ex.length);
            for (Coordinate c : ex){
                exCoords.add(new LngLatAlt(c.x, c.y));
            }
            polygon.setExteriorRing(exCoords);

            int innerCount = poly.getNumInteriorRing();
            for (int i = 0; i < innerCount; i++) {
                Coordinate[] inner = poly.getInteriorRingN(i).getCoordinates();

                List<LngLatAlt> innerCoords = new ArrayList<>(inner.length);
                for (Coordinate c : inner){
                    innerCoords.add(new LngLatAlt(c.x, c.y));
                }
                polygon.addInteriorRing(innerCoords);
            }

            return jsonUtil.write2String(polygon);
        }

        throw new IllegalArgumentException("Not support " + type + "yet.");
    }
}
