package com.navinfo.mapspotter.process.convert.road.transfer;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created by huanghai on 2016/3/17.
 */
public class ConvertBaiDuData {
    public static void main(String[] args) throws IOException {
        String zookeeperHost = args[0];
        String tableNameSource = args[1];
        String tableNameTarget = args[2];
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zookeeperHost);
        Connection connectionSource = ConnectionFactory.createConnection(conf);
        Connection connectionTarget = ConnectionFactory.createConnection(conf);
        Table tableSource = connectionSource
                .getTable(TableName.valueOf(tableNameSource));
        Table tableTarget = connectionTarget
                .getTable(TableName.valueOf(tableNameTarget));
        Scan scan = new Scan();
        scan.addFamily("baidu".getBytes());
        scan.addColumn("baidu".getBytes(), "data".getBytes());

        ResultScanner results = tableSource.getScanner(scan);

        int sum = 0;
        int sumTotal = 0;
        for (Result result : results) {
            byte[] baiduByte = null;
            for (Cell cell : result.rawCells()) {
                if (Bytes.toString(cell.getFamilyArray(),
                        cell.getFamilyOffset(), cell.getFamilyLength()).equals(
                        "baidu")
                        && Bytes.toString(cell.getQualifierArray(),
                        cell.getQualifierOffset(),
                        cell.getQualifierLength()).equals(
                        "data")) {
                    baiduByte = Bytes.copy(cell.getValueArray(),
                            cell.getValueOffset(), cell.getValueLength());
                }
            }
            sumTotal++;
            if (baiduByte != null) {
                SerializeUtil<Integer[][]> serializeUtil = new SerializeUtil();
                Integer[][] deserialize = serializeUtil.deserialize(baiduByte);
                Integer[][] deserializeTmp = new Integer[1024][1024];
                for (int i = 0; i < deserialize.length; i++) {
                    for (int j = 0; j < deserialize[i].length; j++) {
                        deserializeTmp[j][i] = deserialize[i][j];
                    }
                }
                // 存入新表
                SerializeUtil<int[][]> serialize = new SerializeUtil<>();
                DoubleMatrix dMatrix = new DoubleMatrix(deserializeTmp);
                DoubleMatrix.SparseMatrix sparseMatrix = dMatrix.toSparse();
                int[][] intMx = new int[][]{sparseMatrix.data, sparseMatrix.indices, sparseMatrix.indptr};
                Put put = new Put(result.getRow());
                put.addColumn("source".getBytes(), "baidu".getBytes(),
                        serialize.serialize(intMx));
                tableTarget.put(put);
                sum++;
            }
        }
        tableSource.close();
        tableTarget.close();
        connectionSource.close();
        connectionTarget.close();
        System.out.println("sumTotal : " + sumTotal + " sum : " + sum);
    }
}
