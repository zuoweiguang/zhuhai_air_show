package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.algorithm.rtree.RTreeUtil;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.OracleDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block 相关分析类
 * Created by ZhangJin1207 on 2016/4/7.
 */
public class BlocksAnalysis {
    public static final Logger logger = Logger.getLogger(BlocksAnalysis.class);
    private OracleDatabase orclDB;
    private RTreeUtil<BlockInfo> Rtree = new RTreeUtil<BlockInfo>(10,20);
    private Map<String , BlockInfo> blockInfoMap = null;
    private String strSql;
    public int Initialize(String host , String db , String user , String password , String port){
        DataSourceParams dataSourceParams = new DataSourceParams();
        dataSourceParams.setHost(host);
        dataSourceParams.setDb(db);
        dataSourceParams.setUser(user);
        dataSourceParams.setPassword(password);
        dataSourceParams.setPort(Integer.parseInt(port));
        dataSourceParams.setType(DataSourceParams.SourceType.Oracle);

        orclDB = (OracleDatabase) DataSource.getDataSource(dataSourceParams);
        return orclDB == null ? 0 : 1;
    }

    /**
     * 通过oracle装载block RTree
     * @param strSql
     */
    public void PrepareRtree(String strSql){
        try {
            SqlCursor cursor = orclDB.query(strSql);
            while(cursor.next()){
                BlockInfo info = new BlockInfo();
                info.setBlockid(cursor.getString(1));
                info.setProvince(cursor.getString(2));
                info.setCity(cursor.getString(3));
                info.setCounty(cursor.getString(4));
                info.setArea(cursor.getString(5));

                String wkt = cursor.getString(6);
                Geometry geom = GeoUtil.wkt2Geometry(wkt);
                info.setGeom(geom);
                Rtree.Add(info.getGeom().getEnvelopeInternal() , info);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Close();
    }
    /**
     * 通过oracle装载block Map
     * @param strSql
     */
    public void PrepareBlockInfoMap(String strSql){
        try {
            blockInfoMap = new HashMap<>();
            SqlCursor cursor = orclDB.query(strSql);
            while (cursor.next()) {
                BlockInfo info = new BlockInfo();
                info.setBlockid(cursor.getString(1));
                info.setProvince(cursor.getString(2));
                info.setCity(cursor.getString(3));
                info.setCounty(cursor.getString(4));
                info.setArea(cursor.getString(5));
                blockInfoMap.put(info.getBlockid(), info);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public BlockInfo GetBlockInfo(String blockid){
        return blockInfoMap.get(blockid);
    }

    public List<BlockInfo> Intersects(Coordinate coordinate , Geometry geometry){
        return Rtree.Relate(coordinate ,geometry , new BlockIntersectsFilter());
    }

    public List<BlockInfo> Intersects(Envelope envelope , Geometry geometry){
        return Rtree.Relate(envelope ,geometry , new BlockIntersectsFilter());
    }

    public List<BlockInfo> Contains(Coordinate coordinate , Geometry geometry){
        return Rtree.Relate(coordinate ,geometry , new BlockContainsFilter());
    }

    public List<BlockInfo> Contains(Envelope envelope , Geometry geometry){
        return Rtree.Relate(envelope ,geometry , new BlockContainsFilter());
    }

    public void Close(){
        orclDB.close();
    }

    /**
     * 装载block RTree
     * @param fileName  Json文件
     * @param systemType 文件存储系统类型  0 本地  其他 hadoop hdfs
     * @throws IOException
     */
    public void prepareRTree_Json(String fileName , int systemType) throws IOException{
        if (fileName.isEmpty()){
            return;
        }

        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        if (systemType == 0){
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()){
            return;
            }
            inputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(inputStream);
        }else{
            Configuration conf = new Configuration();
            inputStream = Hdfs.readFile(conf , fileName);
            inputStreamReader = new InputStreamReader(inputStream);
        }

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strLine = null;
        while ((strLine = bufferedReader.readLine()) != null){
            strLine = strLine.trim();
            BlockInfo blockInfo = BlockInfo.parse(strLine);

            Geometry geometry = GeoUtil.wkt2Geometry(blockInfo.getWkt());

            blockInfo.setGeom(geometry);
            blockInfo.setWkt(null);
            Rtree.Add(blockInfo.getGeom().getEnvelopeInternal() , blockInfo);
        }
        inputStream.close();
    }

    /**
     * 装载block Map
     * @param fileName  Json文件
     * @param systemType 文件存储系统类型  0 本地  其他 hadoop hdfs
     * @throws IOException
     */
    public void prepareMap_Json(String fileName , int systemType) throws IOException{

        if (fileName.isEmpty()){
            return;
        }
        blockInfoMap = new HashMap<>();

        InputStreamReader inputStreamReader = null;
        if (systemType == 0){
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()){
                return;}
            inputStreamReader = new InputStreamReader(new FileInputStream(file));
        }else{
            Configuration conf = new Configuration();
            InputStream inputStream = Hdfs.readFile(conf , fileName);
            inputStreamReader = new InputStreamReader(inputStream);
        }
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strLine = null;
        while ((strLine = bufferedReader.readLine()) != null){
            strLine = strLine.trim();
            BlockInfo blockInfo = BlockInfo.parse(strLine);
            blockInfo.setWkt(null);
            blockInfoMap.put(blockInfo.getBlockid() , blockInfo);
        }
        bufferedReader.close();
    }
}
