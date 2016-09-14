package com.navinfo.mapspotter.warehouse.zhuhai.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mercator.TileUtils;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.warehouse.zhuhai.dao.ViewDao;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zuoweiguang on 2016/9/7.
 */
public class ViewService {

    private ViewDao viewDao = ViewDao.getInstance();

    public static GeometryFactory geometryFactory = new GeometryFactory();
    private static ViewService instance;
    public WKTReader reader = new WKTReader();

    private ViewService() {
    }

    public static synchronized ViewService getInstance() {
        if (instance == null) {
            instance = new ViewService();
        }
        return instance;
    }

    public byte[] getTrafficEvent(int z, int x, int y) {
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        byte[] result = null;
        try {
            JSONObject prop = PropertiesUtil.getProperties();
            DBCollection col = viewDao.getCollection(prop.getString("eventColName"));
            List<DBObject> eventList = viewDao.getTrafficEvent(col, z, x, y);
            for (DBObject event: eventList) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("RoadName", event.get("RoadName"));
                attributes.put("EventDescription", event.get("EventDescription"));
                List<Double> LinkCoordinate = (List<Double>)event.get("LinkCoordinate");

                Coordinate coordinate = new Coordinate(LinkCoordinate.get(0), LinkCoordinate.get(1));
                Point point = geometryFactory.createPoint(coordinate);
                TileUtils.convert2Piexl(x, y, z, point);
                vtm.addFeature(WarehouseDataType.LayerType.TrafficEvents.toString(), attributes, point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }

    public byte[] getForecasts(int z, int x, int y) {
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
        byte[] result = null;
        try {
            JSONObject prop = PropertiesUtil.getProperties();
            DBCollection col = viewDao.getCollection(prop.getString("forecastColName"));
            List<DBObject> forecastList = viewDao.getForecasts(col, z, x, y);
            for (DBObject forecast: forecastList) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("link_id", forecast.get("link_id"));
                attributes.put("direct", forecast.get("direct"));
                attributes.put("road_class", forecast.get("road_class"));
                attributes.put("status", forecast.get("status"));
                attributes.put("travel_time", forecast.get("travel_time"));
                String geometryStr = (String)forecast.get("geometry");
                LineString line = (LineString) reader.read(geometryStr);
                TileUtils.convert2Piexl(x, y, z, line);

                vtm.addFeature(WarehouseDataType.LayerType.Forecast.toString(),
                        attributes, line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return vtm.encode();
        }
    }


}
