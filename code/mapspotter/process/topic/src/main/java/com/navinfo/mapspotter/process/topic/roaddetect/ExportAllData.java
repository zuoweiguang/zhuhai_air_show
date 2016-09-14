package com.navinfo.mapspotter.process.topic.roaddetect;
import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * Created by cuiliang on 2016/3/18.
 */
public class ExportAllData {
    Configuration configuration = null;
    Hbase hbase = null;

    public ExportAllData() {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.rootdir", "hdfs://Master.Hadoop:9000/hbase");
        configuration.set("hbase.zookeeper.quorum", "Master.Hadoop:2181");
        configuration.set("hbase.master", "Slave3.Hadoop");
        hbase = Hbase.createWithConfiguration(configuration);
    }

    public Map<String, byte[]> GetRecordByRowKey(String rowkey)
            throws IOException {
        String source = "baidu";
        Connection connection = ConnectionFactory.createConnection(configuration);

        Table table = connection.getTable(TableName
                .valueOf("road_detect_12_gt"));

        Get get = new Get(rowkey.getBytes());
        Result r = table.get(get);
        Map<String, byte[]> returnMap = new HashMap<>();

        for (Cell cell : r.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY) && qualifier.equals(source)) {
                byte[] qualifierByte = r.getValue(Constants.ROAD_DETECT_SOURCE_FAMILY.getBytes(), source.getBytes());
                returnMap.put(source, qualifierByte);
            }
        }
        table.close();
        connection.close();
        return returnMap;
    }

    public void scanTable(String path, String table_name) throws IOException {
        Table table = hbase.getTable(table_name);
        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(true);
        ResultScanner scanner = table.getScanner(scan);
        Map<String, byte[]> returnMap = new HashMap<>();
        ImageAlgorithm arrayUtil = new ImageAlgorithm();
        SerializeUtil<int[][]> icu = new SerializeUtil<>();
        for (Result result : scanner) {
            for (Cell cell : result.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String rowKey = Bytes.toString(result.getRow());
                String qualifier = new String(CellUtil.cloneQualifier(cell));
                String key = family + ":" + qualifier;

                if (family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY) && qualifier.equals("baidu")){
                    byte[] byteData = result.getValue(family.getBytes(), qualifier.getBytes());
                    if (byteData != null) {
                        Integer[][] sourceMatrix = null;

                        int[][] sparse = icu.deserialize(byteData);
                        DoubleMatrix dMatrix = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
                        sourceMatrix = dMatrix.toIntegerArray2();
                        File headPath = new File(path + File.separator + "baidu/");
                        if(!headPath.exists()){
                            headPath.mkdirs();
                        }
                        try {
                            arrayUtil.arrayToFile(sourceMatrix, path + File.separator + "baidu/" + rowKey + ".txt");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        table.close();
    }

    public static void main(String[] args){
        String path = "E:\\fusion\\road\\emergency\\";
        String table = "road_detect_12_gt";
        RowCount rc = new RowCount();
        try {
            rc.scanTable(path,table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
