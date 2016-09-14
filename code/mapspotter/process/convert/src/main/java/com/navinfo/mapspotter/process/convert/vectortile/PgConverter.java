package com.navinfo.mapspotter.process.convert.vectortile;

import com.mercator.TileUtils;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.PostGISDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.*;
import org.apache.commons.lang.NotImplementedException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 从Postgre数据库转换Protobuf
 * Created by SongHuiXing on 6/6 0006.
 */
public class PgConverter extends InformationConverter {


    private static double coordPerMeter = 1.0 / 111000;

    private final Logger logger = Logger.getLogger(PgConverter.class);

    private PostGISDatabase pgDatabase = null;

    public PgConverter(PostGISDatabase pgdb) {
        pgDatabase = pgdb;
    }

    public boolean setup() {
        if(null != pgDatabase)
            return true;

        pgDatabase = (PostGISDatabase) DataSource.getDataSource(params);

        return pgDatabase != null;
    }

    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType) {
        return getProtobuf(z, x, y, WarehouseDataType.getLayers(srcType));
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, WarehouseDataType.SourceType srcType, String condition) {
        return new byte[0];
    }

    public byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList) {
        Envelope bound = MercatorUtil.mercatorBound(z, x, y);

        Geometry boxGeo = GeoUtil.convert(bound);

        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

        for (WarehouseDataType.LayerType type : typeList) {
            switch (type) {
                case Road:
                    writeRoad(boxGeo, vtm, z, x, y);
                    break;
                case RailWay:
                    writeRailway(boxGeo, vtm, z, x, y);
                    break;
                case Admin:
                    writeAdmin(boxGeo, vtm, z, x, y);
                    break;
                case AdminBoundary:
                    writeAdminBoundary(boxGeo, vtm, z, x, y);
                    break;
                case AdminFlag:
                    writeAdminFlagPoint(boxGeo, vtm, z, x, y);
                    break;
                case LU:
                    writeLanduse(boxGeo, vtm, z, x, y);
                    break;
                case LC:
                    writeLandcover(boxGeo, vtm, z, x, y);
                    break;
                case CityModel:
                    writeCityModel(boxGeo, vtm, z, x, y);
                    break;
                case BlockHistory:
                    writeBlockHistory(boxGeo, vtm, z, x, y);
                    break;
                case TrafficStatus:
                    writeTrafficStatus(boxGeo, vtm, z, x, y);
                    break;
                case BaotouTrafficStatus:
                    writeBaotouTrafficStatus(boxGeo, vtm, z, x, y);
                    break;
            }
        }

        return vtm.encode();
    }

    @Override
    public byte[] getProtobuf(int z, int x, int y, List<WarehouseDataType.LayerType> typeList, String condition) {
        return new byte[0];
    }

    private int writeRoad(Geometry bound, VectorTileEncoder encoder,
                          int z, int x, int y) {

        int count = 0;

        Douglas sparser = new Douglas();

        String sql = getLayerDataSql(WarehouseDataType.LayerType.Road, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("pid", cursor.getInteger(1));
                attributes.put("kind", cursor.getInteger(2));
                attributes.put("form", cursor.getInteger(3));
                attributes.put("direct", cursor.getInteger(4));
                attributes.put("functionclass", cursor.getInteger(5));
                attributes.put("name", cursor.getString(6));

                byte[] wkb = cursor.getBytes(7);
                LineString linkGeo = (LineString) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, linkGeo);

                encoder.addFeature(WarehouseDataType.LayerType.Road.toString(),
                        attributes, linkGeo);

                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeRailway(Geometry bound, VectorTileEncoder encoder,
                             int z, int x, int y) {
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.RailWay, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("lineid", cursor.getInteger(1));
                attributes.put("kind", cursor.getInteger(2));
                attributes.put("name", cursor.getString(3));

                byte[] wkb = cursor.getBytes(4);
                LineString linkGeo = (LineString) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, linkGeo);

                encoder.addFeature(WarehouseDataType.LayerType.RailWay.toString(),
                        attributes, linkGeo);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeAdmin(Geometry bound, VectorTileEncoder encoder,
                           int z, int x, int y) {
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.Admin, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("regionid", cursor.getInteger(1));

                double adminType = cursor.getDouble(2);
                attributes.put("admintype", adminType);

                byte[] wkb = cursor.getBytes(3);
                Polygon faceGeo = (Polygon) wkbReader.read(wkb);

                attributes.put("name", cursor.getString(4));

                TileUtils.convert2Piexl(x, y, z, faceGeo);

                encoder.addFeature(WarehouseDataType.LayerType.Admin.toString(),
                        attributes,
                        faceGeo);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeAdminBoundary(Geometry bound, VectorTileEncoder encoder,
                                   int z, int x, int y){
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.AdminBoundary, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("kind", cursor.getInteger(1));

                byte[] wkb = cursor.getBytes(2);
                LineString boundary = (LineString) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, boundary);

                encoder.addFeature(WarehouseDataType.LayerType.AdminBoundary.toString(),
                        attributes,
                        boundary);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeAdminFlagPoint(Geometry bound, VectorTileEncoder encoder,
                                    int z, int x, int y){
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.AdminFlag, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("adminid", cursor.getInteger(1));
                attributes.put("admintype", cursor.getDouble(2));

                byte[] wkb = cursor.getBytes(3);
                Point flagPoint = (Point) wkbReader.read(wkb);

                attributes.put("name", cursor.getString(4));

                TileUtils.convert2Piexl(x, y, z, flagPoint);

                encoder.addFeature(WarehouseDataType.LayerType.AdminFlag.toString(),
                                    attributes,
                                    flagPoint);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeLandcover(Geometry bound, VectorTileEncoder encoder,
                               int z, int x, int y) {
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.LC, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("lcid", cursor.getInteger(1));

                attributes.put("kind", cursor.getInteger(2));

                attributes.put("name", cursor.getString(3));

                byte[] wkb = cursor.getBytes(4);
                Polygon faceGeo = (Polygon) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, faceGeo);

                encoder.addFeature(WarehouseDataType.LayerType.LC.toString(),
                        attributes,
                        faceGeo);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }


    private int writeLanduse(Geometry bound, VectorTileEncoder encoder,
                             int z, int x, int y) {
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.LU, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("luid", cursor.getInteger(1));
                attributes.put("kind", cursor.getInteger(2));
                attributes.put("name", cursor.getString(3));
                byte[] wkb = cursor.getBytes(4);
                Polygon faceGeo = (Polygon) wkbReader.read(wkb);
                TileUtils.convert2Piexl(x, y, z, faceGeo);
                encoder.addFeature(WarehouseDataType.LayerType.LU.toString(),
                        attributes,
                        faceGeo);
                count++;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeCityModel(Geometry bound, VectorTileEncoder encoder,
                             int z, int x, int y) {
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.CityModel, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("cmid", cursor.getInteger(1));
                attributes.put("levels", cursor.getDouble(2));
                attributes.put("kind", cursor.getInteger(3));
                attributes.put("name", cursor.getString(4));
                byte[] wkb = cursor.getBytes(5);
                Polygon faceGeo = (Polygon) wkbReader.read(wkb);
                TileUtils.convert2Piexl(x, y, z, faceGeo);
                encoder.addFeature(WarehouseDataType.LayerType.CityModel.toString(),
                        attributes,
                        faceGeo);
                count++;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeBlockHistory(Geometry bound, VectorTileEncoder encoder,
                                  int z, int x, int y){
        int count = 0;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.BlockHistory, z);

        try (SqlCursor cursor = pgDatabase.query(sql, wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                String name = cursor.getString(1);
                String province = cursor.getString(2);
                String city = cursor.getString(3);

                attributes.put("name", province+city+name);

                String version = cursor.getString(4);

                byte[] wkb = cursor.getBytes(5);
                Polygon faceGeo = (Polygon) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, faceGeo);

                encoder.addFeature(version,
                                attributes,
                                faceGeo);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }

    private int writeTrafficStatus(Geometry bound, VectorTileEncoder encoder,
                                   int z, int x, int y){
        int count = 0;

        if(z < 8)
            return count;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.TrafficStatus, z);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        int second = date.getSeconds();

        calendar.set(2016, 6, 25, 16, second, 0);

        long secondtimestamp = calendar.getTimeInMillis() / 1000;

        calendar.clear();

        try (SqlCursor cursor = pgDatabase.query(sql,
                                                secondtimestamp - 60,
                                                secondtimestamp,
                                                wkbWriter.write(bound))) {

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("dir", cursor.getInteger(1));
                attributes.put("status", cursor.getInteger(2));

                byte[] wkb = cursor.getBytes(3);
                LineString line = (LineString) wkbReader.read(wkb);

                TileUtils.convert2Piexl(x, y, z, line);

                encoder.addFeature(WarehouseDataType.LayerType.TrafficStatus.toString(),
                                    attributes,
                                    line);

                count++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return count;
    }


    private int writeBaotouTrafficStatus(Geometry bound, VectorTileEncoder encoder,
                                   int z, int x, int y){
        int count = 0;

        if(z < 11)
            return count;

        String sql = getLayerDataSql(WarehouseDataType.LayerType.TrafficStatus, z);

        Calendar calendar = Calendar.getInstance();
        int DAY_OF_MONTH = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        calendar.set(2016, Calendar.AUGUST, 2, 7, 0, 0);
//        calendar.clear();

        long secondtimestamp = calendar.getTimeInMillis() / 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(secondtimestamp);
        String time1 = sdf.format(new Date(secondtimestamp * 1000));
//        System.out.println(time1);

        //提前2个小时
        long s2 = (secondtimestamp * 1000) - (1000 * 60 * 60 * 1);
//        System.out.println(s2 / 1000);
        String time2 = sdf.format(new Date(s2));
//        System.out.println(time2);

        try {
            //, wkbWriter.write(bound)
            SqlCursor cursor = pgDatabase.query(sql,
                    ((secondtimestamp * 1000) - (1000 * 60 * 60 * 1)) / 1000, secondtimestamp, wkbWriter.write(bound));

            while (cursor.next()) {
                Map<String, Object> attributes = new HashMap<>();

                attributes.put("dir", cursor.getInteger(1));
                attributes.put("status", cursor.getInteger(2));

                byte[] wkb = cursor.getBytes(3);
                LineString line = (LineString) wkbReader.read(wkb);

//                if (cursor.getInteger(2) == 2 || cursor.getInteger(2) == 3) {
//                    System.out.println("status::" + cursor.getInteger(2));
//                    System.out.println("linestring::" + line.toText());
//                }

                TileUtils.convert2Piexl(x, y, z, line);

                encoder.addFeature(WarehouseDataType.LayerType.TrafficStatus.toString(),
                        attributes,
                        line);

                count++;
            }
//            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        return count;
    }

    private String getLayerDataSql(WarehouseDataType.LayerType type, int level) {
        switch (type) {
            case Road: {
                String filter = FilterReader.getFilter(type, level);
                return String.format(SrcdataSQL.road_main, filter);
            }
            case RailWay:
                return SrcdataSQL.railyway_main;
            case Admin:
                return SrcdataSQL.admin_main;
            case AdminBoundary:
                return SrcdataSQL.admin_boundary;
            case AdminFlag:
                return SrcdataSQL.admin_flag;
            case LU: {
                String filter = FilterReader.getFilter(type, level);
                return String.format(SrcdataSQL.lu_main, filter);
            }
            case LC: {
                String filter = FilterReader.getFilter(type, level);
                return String.format(SrcdataSQL.lc_main, filter);
            }
            case CityModel: {
                String filter = FilterReader.getFilter(type, level);
                return String.format(SrcdataSQL.city_model, filter);
            }
            case BlockHistory:{
                return SrcdataSQL.block_history;
            }
            case TrafficStatus:{
                String filter = FilterReader.getFilter(type, level);
                return String.format(SrcdataSQL.traffic, filter);
            }
        }

        return "";
    }

    public String getGeojson(int z, int x, int y, WarehouseDataType.LayerType type){
        throw new NotImplementedException();
    }

    public String getGeojson(double minx, double miny, double maxx, double maxy,
                                      WarehouseDataType.LayerType type){
        throw new NotImplementedException();
    }

    private static double getCoordFromDis(int level){
        double firstPrecision = 80000.0 / 16 * coordPerMeter;

        return firstPrecision / Math.pow(2, level);
    }

    public static void main(String[] args) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.PostGIS);
        params.setHost("192.168.4.104");
        params.setPort(5440);
        params.setUser("postgres");
        params.setPassword("navinfo1!pg");
        params.setDb("baotou_demo");

        // 12/3296/1539  12/3298/1539  14/13191/6161
        //http://192.168.4.128:8050/baotou_demo/view/traffic/13/6597/3081
//        http://192.168.4.128:8050/baotou_demo/view/traffic/14/13271/6152
        int z = 14;
        int x = 13271;
        int y = 6152;
        PgConverter pg = new PgConverter((PostGISDatabase) DataSource.getDataSource(params));
        Envelope bound = MercatorUtil.mercatorBound(z, x, y);
        Geometry boxGeo = GeoUtil.convert(bound);
        VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);
//        pg.writeTrafficStatus(boxGeo, vtm, z, x, y);
        int count = pg.writeRoad(boxGeo, vtm, z, x, y);
        System.out.println("count:" + count);

        //测试道路等级
//        String sql = pg.getLayerDataSql(WarehouseDataType.LayerType.TrafficStatus, 8);
//        System.out.println(sql);

//        Calendar calendar = Calendar.getInstance();
//        int DAY_OF_MONTH = calendar.get(Calendar.DAY_OF_MONTH);
//        int hour = calendar.get(Calendar.HOUR);
//        int minute = calendar.get(Calendar.MINUTE);
//        int second = calendar.get(Calendar.SECOND);
//        calendar.set(2016, Calendar.AUGUST, 2, 7, 0, 0);
//        long secondtimestamp = calendar.getTimeInMillis() / 1000;
//        System.out.println(secondtimestamp);
//
//        //提前2个小时
//        long s2 = (secondtimestamp * 1000) - (1000 * 60 * 60 * 1);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dStr = sdf.format(new Date(s2));
//        System.out.println(dStr);
    }

}
