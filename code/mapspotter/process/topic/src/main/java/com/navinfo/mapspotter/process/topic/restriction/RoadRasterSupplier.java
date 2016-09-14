package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 道路栅格底图提供者
 * Created by SongHuiXing on 2016/3/7.
 */
public class RoadRasterSupplier {
    public final String roadRasterTable;
    private final byte[] roadRasterColFamily;

    public RoadRasterSupplier(String tablename, String colFamily){
        roadRasterTable = tablename;
        roadRasterColFamily = Bytes.toBytes(colFamily);
    }


    private static byte[] s_roadRasterCol = null;

    private static SerializeUtil<int[][]> intMxUtil = new SerializeUtil<>();
    private static SerializeUtil<double[]> doubleArrayUtil = new SerializeUtil<>();

    static{
        s_roadRasterCol = Bytes.toBytes("road");
    }

    private Connection m_hbaseConn = null;

    private org.jblas.DoubleMatrix mainRoadRaster = null;
    private String mainRoadTile = "";

    public boolean prepare(){

        if(null == m_hbaseConn || m_hbaseConn.isClosed()){
            try {
                m_hbaseConn = ConnectionFactory.createConnection(CrossInformationVistor.getHBaseConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null == m_hbaseConn;
    }

    public void shutdown(){
        if(null != m_hbaseConn){
            try {
                if(!m_hbaseConn.isClosed())
                    m_hbaseConn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMainRoadRaster(String tileCode){
        if(mainRoadTile.equals(tileCode))
            return;

        mainRoadTile = tileCode;
        mainRoadRaster = null;
    }

    /**
     * 读取道路底图栅格
     * @param tileCode 底图的tile号码
     * @return
     */
    public org.jblas.DoubleMatrix getRoadRaster(String tileCode){

        if(tileCode.equals(mainRoadTile) && null != mainRoadRaster){
            return mainRoadRaster;
        }

        return readRoadFromHbase(tileCode);
    }

    private org.jblas.DoubleMatrix readRoadFromHbase(String tileCode){
        org.jblas.DoubleMatrix roadRaster;

        StringBuilder sb = new StringBuilder(tileCode);

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(roadRasterTable))){

            Get gt = new Get(Bytes.toBytes(sb.reverse().toString()));
            gt.addColumn(roadRasterColFamily, s_roadRasterCol);

            Result re = targetTable.get(gt);

            if(!re.isEmpty()) {
                byte[] roadmx = re.getValue(roadRasterColFamily, s_roadRasterCol);

                int[][] sparse = intMxUtil.deserialize(roadmx);

                roadRaster = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
            } else {
                roadRaster = DoubleMatrix.zeros(1024, 1024);
            }

        } catch (IOException e) {
            e.printStackTrace();
            roadRaster = DoubleMatrix.zeros(1024, 1024);
        }

        if(null == mainRoadRaster && tileCode.equals(mainRoadTile)){
            mainRoadRaster = roadRaster;
        }

        return roadRaster;
    }
}
