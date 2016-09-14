package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.algorithm.rtree.SimplePointRTree;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.SqlDatabase;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.process.topic.restriction.CrossRaster;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 结构化数据库内路口访问处理
 * Created by SongHuiXing on 2016/2/3.
 */
public class SqlCrossVistor {
    private static GeometryFactory factory = new GeometryFactory();

    private static WKTWriter wktWriter = new WKTWriter();

    private static WKBWriter wkbWriter = new WKBWriter();
    private static WKBReader wkbReader = new WKBReader();

    private static SerializeUtil<int[][]> intMxUtil = new SerializeUtil<>();

    private static String insertIndexSql = "INSERT INTO CrossPosition " +
                                            "VALUES (%d, PointFromText('%s'), PolygonFromText('%s'))";

    private static String insertInfoSql = "INSERT INTO CrossInfo " +
                                          "VALUES (%d, PolygonFromText('%s'), PolygonFromText('%s'), %d, '%s', ?, ?, ?, ?, ?)";

    private static String searchIndexSql = "SELECT CrossId, AsText(PageEnvelope) FROM CrossPosition " +
                                            "WHERE MBRWithin(CrossCenter, PolygonFromText('%s'))";

    private static String searchIndexSqlWKB = "SELECT CrossId, AsBinary(PageEnvelope) FROM CrossPosition " +
                                                "WHERE MBRWithin(CrossCenter, PolygonFromWKB(?))";

    private static String getRasterSql = "SELECT RasterColCount, AsText(PageEnvelope), Raster FROM CrossInfo " +
                                            "WHERE CrossId=%d LIMIT 1";

    private static String getRestrictionSql = "SELECT Restriction FROM CrossInfo WHERE CrossId=%d LIMIT 1";

    private static String spatialIndexSql = "ALTER TABLE CrossPosition ADD SPATIAL INDEX(CrossCenter)";

    private static String getAllCrossInx = "SELECT CrossId, AsText(CrossCenter), AsText(PageEnvelope) FROM CrossPosition";

    private SqlDatabase database = null;
    private PreparedStatement preparedStatement = null;

    private DataSourceParams dataSourceParams = null;

    private JsonUtil jsonUtil = JsonUtil.getInstance();

    public SqlCrossVistor(String host, String db, int port, String user, String pwd){
        dataSourceParams = IOUtil.makeMysqlParams(host, port, db, user, pwd);
    }

    public boolean prepare() {
        if(null != database)
            return true;

        database = (SqlDatabase) DataSource.getDataSource(dataSourceParams);

        try {
            preparedStatement = database.prepare(searchIndexSqlWKB);
        } catch (SQLException e){
            preparedStatement = null;
        }

        return null != database;
    }

    public void shutdown(){
        try {
            if (null != preparedStatement && !preparedStatement.isClosed()) {
                preparedStatement.close();
            }
        } catch (SQLException e){

        }

        if(null != database){
            database.close();
            database = null;
        }
    }

    /**
     * 插入路口索引信息
     * @param pid       路口pid
     * @param pageenv   路口栅格范围
     * @return
     */
    public boolean insertCrossIndex(long pid, double[] pageenv){
        double centerx = (pageenv[0] + pageenv[2]) / 2;
        double centery = (pageenv[1] + pageenv[3]) / 2;

        Point centerPt = factory.createPoint(new Coordinate(centerx, centery));

        Coordinate p1 = new Coordinate(pageenv[0], pageenv[1]);
        Coordinate p2 = new Coordinate(pageenv[0], pageenv[3]);
        Coordinate p3 = new Coordinate(pageenv[2], pageenv[3]);
        Coordinate p4 = new Coordinate(pageenv[2], pageenv[1]);

        Polygon mbrPolygon = factory.createPolygon(new Coordinate[]{p1, p2, p3, p4, p1});

        String sql = String.format(insertIndexSql, pid, wktWriter.write(centerPt), wktWriter.write(mbrPolygon));

        return database.execute(sql) == 0;
    }

    /**
     * 插入路口综合信息
     * @param cross     路口基本信息
     * @param raster    路口栅格信息
     * @return
     */
    public boolean insertCrossInfo(BaseCrossJsonModel cross, CrossRaster raster){

        double[] pageenv = raster.getPageEnvelope();
        Coordinate p1 = new Coordinate(pageenv[0], pageenv[1]);
        Coordinate p2 = new Coordinate(pageenv[0], pageenv[3]);
        Coordinate p3 = new Coordinate(pageenv[2], pageenv[3]);
        Coordinate p4 = new Coordinate(pageenv[2], pageenv[1]);

        Polygon mbrPolygon = factory.createPolygon(new Coordinate[]{p1, p2, p3, p4, p1});

        double[] crossenv = raster.getCrossEnvelope();
        Coordinate p11 = new Coordinate(crossenv[0], crossenv[1]);
        Coordinate p12 = new Coordinate(crossenv[0], crossenv[3]);
        Coordinate p13 = new Coordinate(crossenv[2], crossenv[3]);
        Coordinate p14 = new Coordinate(crossenv[2], crossenv[1]);

        Polygon crossPolygon = factory.createPolygon(new Coordinate[]{p11, p12, p13, p14, p11});

        String sql = String.format(insertInfoSql,
                                    cross.getPID(),
                                    wktWriter.write(mbrPolygon),
                                    wktWriter.write(crossPolygon),
                                    raster.getRasterColCount(),
                                    cross.getLinkenvelope());


        ArrayList<Object> paramValues = new ArrayList<>();

        InputStream ldIs = new ByteArrayInputStream(Bytes.toBytes(cross.getLinkDirection()));
        paramValues.add(ldIs);

        InputStream rsIs = new ByteArrayInputStream(Bytes.toBytes(cross.getRestriction()));
        paramValues.add(rsIs);

        InputStream rasterIs = new ByteArrayInputStream(intMxUtil.serialize(raster.getSparseRaster()));
        paramValues.add(rasterIs);

        InputStream nodeIs = new ByteArrayInputStream(Bytes.toBytes(cross.getNodes()));
        paramValues.add(nodeIs);

        InputStream linkIs = new ByteArrayInputStream(Bytes.toBytes(cross.getLinks()));
        paramValues.add(linkIs);

        return database.execute(sql, paramValues) == 0;
    }

    /**
     * 为索引表的中点列创建空间索引
     * @return
     */
    public boolean createSpatialIndex4Info(){
        if(null == database)
            return false;

        return database.execute(spatialIndexSql) == 0 ? true : false;
    }

    /**
     * 选取中心点落在<code>envelope</code>范围内的路口
     * @param envelope  选择框
     * @return  路口pid + 路口页面范围
     */
    public Map<Long, double[]> search(double[] envelope){
        HashMap<Long, double[]> allCrosses = new HashMap<>();

        Coordinate p1 = new Coordinate(envelope[0], envelope[1]);
        Coordinate p2 = new Coordinate(envelope[0], envelope[3]);
        Coordinate p3 = new Coordinate(envelope[2], envelope[3]);
        Coordinate p4 = new Coordinate(envelope[2], envelope[1]);

        Polygon mbrPolygon = factory.createPolygon(new Coordinate[]{p1, p2, p3, p4, p1});

        String sql = String.format(searchIndexSql, wktWriter.write(mbrPolygon));

        try(SqlCursor cursor = database.query(sql)){
            while (cursor.next()){
                Long id = new Long(cursor.getInteger(1));
                Envelope pageEnv = cursor.getWKTGeometry(2).getEnvelopeInternal();

                allCrosses.put(id, new double[]{pageEnv.getMinX(),
                                                pageEnv.getMinY(),
                                                pageEnv.getMaxX(),
                                                pageEnv.getMaxY()});
            }
        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return allCrosses;
    }

    public Map<Long, double[]> searchCrosses(double[] envelope){
        HashMap<Long, double[]> allCrosses = new HashMap<>();

        Coordinate p1 = new Coordinate(envelope[0], envelope[1]);
        Coordinate p2 = new Coordinate(envelope[0], envelope[3]);
        Coordinate p3 = new Coordinate(envelope[2], envelope[3]);
        Coordinate p4 = new Coordinate(envelope[2], envelope[1]);

        Polygon mbrPolygon = factory.createPolygon(new Coordinate[]{p1, p2, p3, p4, p1});

        ArrayList<Object> paramValues = new ArrayList<>();
        paramValues.add(wkbWriter.write(mbrPolygon));

        try(SqlCursor cursor = database.query(preparedStatement,paramValues)){
            while (cursor.next()){
                Long id = new Long(cursor.getInteger(1));

                byte[] envBytes = cursor.getBytes(2);

                Envelope pageEnv = wkbReader.read(envBytes).getEnvelopeInternal();

                allCrosses.put(id, new double[]{pageEnv.getMinX(),
                        pageEnv.getMinY(),
                        pageEnv.getMaxX(),
                        pageEnv.getMaxY()});
            }
        } catch (Exception e){
            System.out.print(e.getStackTrace());
        }

        return allCrosses;
    }

    /**
     * 根据pid查询路口栅格信息
     * @param pid   路口pid
     * @return 路口栅格信息(pid, 路口页面范围, 路口栅格)
     */
    public CrossRaster getCrossRaster(long pid){
        CrossRaster raster = null;

        String sql = String.format(getRasterSql, pid);
        try(SqlCursor cursor = database.query(sql)){
            if (cursor.next()){
                raster = new CrossRaster();
                raster.setPid(pid);

                int colCount = cursor.getInteger(1);
                raster.setRasterColCount(colCount);

                Envelope pageEnv = cursor.getWKTGeometry(2).getEnvelopeInternal();
                raster.setPageEnvelope(new double[]{pageEnv.getMinX(),
                                                    pageEnv.getMinY(),
                                                    pageEnv.getMaxX(),
                                                    pageEnv.getMaxY()});

                InputStream blobIn = cursor.getBlob(3);

                int[][] sparse = intMxUtil.deserialize(blobIn);

                raster.setSparseRaster(sparse);
            }
        } catch (Exception e){
            System.out.print(e.getStackTrace());
            return null;
        }

        return raster;
    }

    /**
     * 根据路口pid查询路口的原始交限信息
     * @param crosspid  路口pid
     * @return  路口的原始交限矩阵
     * @throws SQLException
     */
    public int[][] getOriginalRestrictionMatrix(long crosspid) throws SQLException{
        int[][] resMatrix = new int[1][1];

        String sql = String.format(getRestrictionSql, crosspid);

        try (SqlCursor cursor = database.query(sql)){

            if(cursor.next()){
                InputStream stream = cursor.getBlob(1);

                byte[] bytesValue = IOUtil.readBlob2Bytes(stream);
                if(null == bytesValue)
                    return resMatrix;

                List<int[]> matrix = jsonUtil.readIntMatrix(bytesValue);

                if(matrix.size() > 0){
                    int rowCount = matrix.size();
                    int colCount = matrix.get(0).length;

                    resMatrix = new int[rowCount][colCount];
                    for(int i=0;i<rowCount;i++){
                        int[] row = matrix.get(i);
                        System.arraycopy(row, 0, resMatrix[i], 0, colCount);
                    }
                }
            }

        } catch (SQLException e){
            throw e;
        }

        return resMatrix;
    }

    public SimplePointRTree buildMemIndex(){
        double[] indexEnv = new double[]{70, 10, 140, 60};
        short indexLevel = 10;

        SimplePointRTree tree = new SimplePointRTree(indexEnv, indexLevel);

        for (int i = 0; i < 42334; i++) {
            CrossPosition crossInx = new CrossPosition(new double[]{118.5f, 41.5f, 119.5f, 42.5f}, 12354);

            tree.insert(crossInx);
        }

//        try(SqlCursor cursor = database.query(getAllCrossInx)){
//            while (cursor.next()){
//                Long id = new Long(cursor.getInteger(1));
//                Point center = (Point) cursor.getWKTGeometry(2);
//                Envelope pageEnv = cursor.getWKTGeometry(3).getEnvelopeInternal();
//
//                CrossPosition crossInx = new CrossPosition(center.getX(), center.getY(), id);
//
//                crossInx.Envelope = new double[]{pageEnv.getMinX(),
//                        pageEnv.getMinY(),
//                        pageEnv.getMaxX(),
//                        pageEnv.getMaxY()};
//
//                tree.insert(crossInx);
//            }
//        } catch (Exception e){
//            System.out.print(e.getStackTrace());
//        }

        return tree;
    }
}
