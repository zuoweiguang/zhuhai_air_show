package com.navinfo.mapspotter.process.convert;

import java.awt.image.BufferedImage;
import java.io.*;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import javax.imageio.ImageIO;

/**
 * Created by huanghai on 2016/1/26.
 */
public class DrawImage {
    public static void drawImage_Sparse() throws IOException {
        BufferedImage bimage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        // 读取hbase里面的矩阵
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "datanode01:2181,datanode02:2181,datanode03:2181");
        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection
                .getTable(TableName.valueOf("road_fusion_test"));
        MercatorUtil mutil = new MercatorUtil(256, 14);
        String tileNumber = mutil.lonLat2MCode(new Coordinate(116.41817,
                39.90489));
        String rowKey = new StringBuffer(tileNumber).reverse().toString();
        rowKey = "8126_48431";
        Get g = new Get(Bytes.toBytes(rowKey));
        g.addColumn("road".getBytes(), "data".getBytes());
        Result result = table.get(g);
        for (Cell cell : result.rawCells()) {
            byte[] roadByte = Bytes.copy(cell.getValueArray(),
                    cell.getValueOffset(), cell.getValueLength());
            SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
            int[][] sparse = serializeUtil.deserialize(roadByte);
            DoubleMatrix dMatrix = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
            int[][] byteToArray = dMatrix.toIntArray2();
            for (int i = 0; i < byteToArray.length; i++) {
                int[] arr = byteToArray[i];
                for (int j = 0; j < arr.length; j++) {
                    int integer = byteToArray[i][j];
                    if (integer > 0) {
                        bimage.setRGB(j, i, 200);
                    } else {
                        bimage.setRGB(j, i, 0);
                    }
                }
            }
            ImageIO.write(bimage, "jpg", new File("E:\\" + rowKey + ".jpg"));
        }
        table.close();
    }
	
	

    public static void drawImage() throws IOException {
        BufferedImage bimage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        // 读取hbase里面的矩阵
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "datanode01:2181,datanode02:2181,datanode03:2181");
        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection
                .getTable(TableName.valueOf("road_fusion_test"));
        MercatorUtil mutil = new MercatorUtil(256, 14);
        String tileNumber = mutil.lonLat2MCode(new Coordinate(116.39507,
                39.981975));
        String rowKey = new StringBuffer(tileNumber).reverse().toString();
        Get g = new Get(Bytes.toBytes(rowKey));
        g.addColumn("road".getBytes(), "data".getBytes());
        Result result = table.get(g);
        for (Cell cell : result.rawCells()) {
            byte[] roadByte = Bytes.copy(cell.getValueArray(),
                    cell.getValueOffset(), cell.getValueLength());
            SerializeUtil<Integer[][]> serializeUtil = new SerializeUtil<>();
            Integer[][] byteToArray = serializeUtil.deserialize(roadByte);
            for (int i = 0; i < byteToArray.length; i++) {
                Integer[] arr = byteToArray[i];
                for (int j = 0; j < arr.length; j++) {
                    Integer integer = byteToArray[j][i];
                    if (integer != null && integer > 0) {
                        bimage.setRGB(i, j, 200);
                    } else {
                        bimage.setRGB(i, j, 0);
                    }
                }
            }
            ImageIO.write(bimage, "jpg", new File("E:\\" + rowKey + ".jpg"));
        }
        table.close();
    }

    public static void writeDataSingleTile_Sparse() throws IOException {
        // 读取hbase里面的矩阵
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "datanode01:2181,datanode02:2181,datanode03:2181");
        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection
                .getTable(TableName.valueOf("road_fusion_test"));
        MercatorUtil mutil = new MercatorUtil(256, 14);
        String tileNumber = mutil.lonLat2MCode(new Coordinate(116.41817,
                39.90489));
//        String rowKey = new StringBuffer(tileNumber).reverse().toString();
        String rowKey = "9026_68431";
        File outFile = new File("E:\\data_" + rowKey + ".txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        Get g = new Get(Bytes.toBytes(rowKey));
        g.addColumn("road".getBytes(), "data".getBytes());
        Result result = table.get(g);
        for (Cell cell : result.rawCells()) {
            byte[] roadByte = Bytes.copy(cell.getValueArray(),
                    cell.getValueOffset(), cell.getValueLength());
            SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
            int[][] sparse = serializeUtil.deserialize(roadByte);
            DoubleMatrix dMatrix = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
            int[][] byteToArray = dMatrix.toIntArray2();
            for (int i = 0; i < byteToArray.length; i++) {
                StringBuilder line = new StringBuilder();
                int[] arr = byteToArray[i];
                for (int j = 0; j < arr.length; j++) {
                    int integer = byteToArray[j][i];
                    if (integer > 0) {
                        line.append(integer + "\t");
                    } else {
                        line.append(0 + "\t");
                    }
                }
                bw.write((line + "\r\n").toString());
            }
        }
        bw.flush();
        bw.close();
        table.close();
    }

    public static void writeDataSingleTile() throws IOException {
        // 读取hbase里面的矩阵
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "datanode01:2181,datanode02:2181,datanode03:2181");
        Connection connection = ConnectionFactory.createConnection(conf);
        Table table = connection
                .getTable(TableName.valueOf("road_fusion_test"));
        MercatorUtil mutil = new MercatorUtil(256, 14);
        String tileNumber = mutil.lonLat2MCode(new Coordinate(116.41817,
                39.90489));
        String rowKey = new StringBuffer(tileNumber).reverse().toString();

        File outFile = new File("E:\\data_" + rowKey + ".txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        Get g = new Get(Bytes.toBytes(rowKey));
        g.addColumn("road".getBytes(), "data".getBytes());
        Result result = table.get(g);
        for (Cell cell : result.rawCells()) {
            byte[] roadByte = Bytes.copy(cell.getValueArray(),
                    cell.getValueOffset(), cell.getValueLength());
            SerializeUtil<Integer[][]> serializeUtil = new SerializeUtil<>();
            Integer[][] byteToArray = serializeUtil.deserialize(roadByte);
            for (int i = 0; i < byteToArray.length; i++) {
                StringBuilder line = new StringBuilder();
                Integer[] arr = byteToArray[i];
                for (int j = 0; j < arr.length; j++) {
                    Integer integer = byteToArray[j][i];
                    if (integer != null && integer > 0) {
                        line.append(integer + "\t");
                    } else {
                        line.append(0 + "\t");
                    }
                }
                bw.write((line + "\r\n").toString());
            }
        }
        bw.flush();
        bw.close();
        table.close();
    }


    public static void testParse() throws IOException {

        File outFileOld = new File("E:\\data_ test_parse_old.txt");
        BufferedWriter bwOld = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFileOld)));

        File outFileNew = new File("E:\\data_ test_parse_new.txt");
        BufferedWriter bwNew = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFileNew)));

        int[][] arrs = new int[1024][1024];
        for (int i = 0; i < 1024; i++) {
            for (int j = 0; j < 1024; j++) {
                arrs[i][j] = i;
            }
        }

        for (int i = 0; i < arrs.length; i++) {
            StringBuilder line = new StringBuilder();
            int[] arr = arrs[i];
            for (int j = 0; j < arr.length; j++) {
                int integer = arrs[j][i];
                if (integer > 0) {
                    line.append(integer + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bwOld.write((line + "\r\n").toString());
        }
        bwOld.flush();
        bwOld.close();


        SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
        DoubleMatrix dMatrix = new DoubleMatrix(arrs);
        DoubleMatrix.SparseMatrix sparseMatrix = dMatrix.toSparse();
        int[][] intMx = new int[][]{sparseMatrix.data, sparseMatrix.indices, sparseMatrix.indptr};
        byte[] bytes = serializeUtil.serialize(intMx);

        int[][] sparse = serializeUtil.deserialize(bytes);

        DoubleMatrix dMatrix1 = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
        int[][] byteToArray = dMatrix1.toIntArray2();
        for (int i = 0; i < byteToArray.length; i++) {
            StringBuilder line = new StringBuilder();
            int[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                int integer = byteToArray[j][i];
                if (integer > 0) {
                    line.append(integer + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bwNew.write((line + "\r\n").toString());
        }
        bwNew.flush();
        bwNew.close();
    }

    public static void main(String[] args) throws IOException {
        drawImage_Sparse();
//        writeDataSingleTile_Sparse();
//        drawImage();
//        writeDataSingleTile();
//        testParse();
    }
}
