package com.navinfo.mapspotter.process.topic.missingroad;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.awt.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by cuiliang on 2016/5/12.
 */
public class DetectionResultExportSpark implements Serializable {


    public String master;
    public String table;
    public String family;
    public String source;
    public String confidence;

    public DetectionResultExportSpark(String master, String table, String family, String source) {
        this.master = master;
        this.table = table;
        this.family = family;
        this.source = source;
        this.confidence = source + "_confidence";
    }


    public void start() {
        System.out.println("=================table:" + table + "=================");
        System.out.println("=================family:" + family + "=================");
        System.out.println("=================confidence:" + confidence + "=================");

        SparkConf sparkConf = new SparkConf().setAppName(source + "_trace_export");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        Configuration conf = HBaseConfiguration.create();
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes(family));
        //scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(source));
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(confidence));
        String ScanToString = "";
        try {
            ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
            ScanToString = Base64.encodeBytes(proto.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        conf.set(TableInputFormat.INPUT_TABLE, table);
        conf.set(TableInputFormat.SCAN, ScanToString);

        JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = jsc.newAPIHadoopRDD(conf,
                TableInputFormat.class, ImmutableBytesWritable.class,
                Result.class);
        JavaPairRDD<String, String> road_detect = hBaseRDD.mapToPair(
                new PairFunction<Tuple2<ImmutableBytesWritable, Result>, String, String>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<ImmutableBytesWritable, Result> results) {
                        //Map<String, byte[]> map = new HashMap();
                        //byte[] sourceByte = results._2().getValue(Bytes.toBytes(family), Bytes.toBytes(source));
                        byte[] confidenceByte = results._2().getValue(Bytes.toBytes(family), Bytes.toBytes(confidence));
                        //map.put(source, sourceByte);
                        //map.put(confidence, confidenceByte);
                        String confidenceValue = new String(confidenceByte);

                        System.out.println(new String(confidenceByte));
                        return new Tuple2(Bytes.toString(results._1().get()), confidenceValue);
                    }
                }
        );


        System.out.println("=================count:" + hBaseRDD.count() + "=================");
        JavaRDD value = road_detect.values();
        value.saveAsTextFile("/data/spark_output");
    }
    private int matrix_size = 1024;
    private int scan_size = 3;
    private int dis = (scan_size - 1) / 2;

    public void deal() {

        SparkConf sparkConf = new SparkConf().setAppName("appName");
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        Configuration conf = HBaseConfiguration.create();
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes(family));
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(source));
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(confidence));
        String ScanToString = "";
        try {
            ClientProtos.Scan proto = ProtobufUtil.toScan(scan);
            ScanToString = Base64.encodeBytes(proto.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        conf.set(TableInputFormat.INPUT_TABLE, table);
        conf.set(TableInputFormat.SCAN, ScanToString);

        JavaPairRDD<ImmutableBytesWritable, Result> hBaseRDD = jsc.newAPIHadoopRDD(conf,
                TableInputFormat.class, ImmutableBytesWritable.class,
                Result.class);

        JavaPairRDD<String, String> road_detect = hBaseRDD.repartition(40).mapToPair(
                new PairFunction<Tuple2<ImmutableBytesWritable, Result>, String, String>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<ImmutableBytesWritable, Result> results) {
                        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
                        ConnectedAlgorithm ca = new ConnectedAlgorithm();
                        MercatorUtil mkt = new MercatorUtil(1024, 12);
                        DecimalFormat df = new DecimalFormat("#.#####");
                        String mCode = StringUtil.reverse(Bytes.toString(results._1().get()));

                        byte[] detectByte = results._2().getValue(Bytes.toBytes(family), Bytes.toBytes(source));
                        byte[] confidenceByte = results._2().getValue(Bytes.toBytes(family), Bytes.toBytes(confidence));

                        JSONArray jsonReturnArray = new JSONArray();

                        if (detectByte != null && detectByte.length > 0) {
                            /**
                             * 获取置信度信息
                            */

                            String confidence = new String(confidenceByte);
                            JSONArray jsonArray = JSON.parseArray(confidence);
                            Map<Integer, String> tunnelMap = new HashMap();
                            Map<Integer, Double> confidenceMap = new HashMap();

                            for (Object object : jsonArray) {
                                JSONObject json = (JSONObject) object;
                                if (json.getInteger("tunnel") == 1)
                                    tunnelMap.put(json.getInteger("key"), "");
                                confidenceMap.put(json.getIntValue("key"), json.getDoubleValue("confidence"));
                            }

                            Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(detectByte, true);
                            //过滤隧道数据
                            detectMatrix = imageAlgorithm.filterTunnelMatrix(detectMatrix, tunnelMap);


                            /**
                             * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素中心点像素无值而其上下左右的点有值则给该点打上值
                             */

                            Integer[][] scanMatrixPatch = new Integer[scan_size][scan_size];
                            for (int i = 0; i < detectMatrix.length ; i++) {
                                for (int j = 0; j < detectMatrix.length; j++) {
                                    for (int m = 0; m < scan_size; m++) {
                                        for (int n = 0; n < scan_size; n++) {
                                            scanMatrixPatch[m][n] = imageAlgorithm.getMatrixValue(detectMatrix, i + m - dis, j + n - dis);
                                        }
                                    }
                                    if (scanMatrixPatch[1][1] == 0) {
                                        if(scanMatrixPatch[0][1] > 0 && scanMatrixPatch[1][0] > 0 && scanMatrixPatch[2][1] > 0 && scanMatrixPatch[1][2] > 0)
                                            detectMatrix[i][j] = scanMatrixPatch[0][1];
                                    }
                                }
                            }



                            //=============================抽骨骼=============================
                            ByteProcessor bp = new ByteProcessor(matrix_size, matrix_size);
                            BinaryProcessor ibp = new BinaryProcessor(bp);
                            ibp.setColor(Color.white);
                            ibp.fill();
                            for (int x = 0; x < matrix_size; x++) {
                                for (int y = 0; y < matrix_size; y++) {
                                    if (detectMatrix[x][y] != null && detectMatrix[x][y] != 0)
                                        ibp.putPixel(x, y, 0);
                                }
                            }
                            ibp.skeletonize();
                            Integer[][] skeletonArray = new Integer[matrix_size][matrix_size];
                            for (int x = 0; x < matrix_size; x++) {
                                for (int y = 0; y < matrix_size; y++) {
                                    if (ibp.get(x, y) == 0) {
                                        skeletonArray[x][y] = detectMatrix[x][y];
                                    }
                                }
                            }
                            //=============================END=============================

                            Integer[][] matrix = imageAlgorithm.binarizationMatrix(skeletonArray);
                            Integer[][] matrix2 = imageAlgorithm.copyMatrix(matrix);


                            Integer[][] scanMatrix = new Integer[scan_size][scan_size];
                            java.util.List<int[]> list = new ArrayList();

                            /**
                             * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素大于3则将其记为0，并且将其坐标记录在list中
                             */

                            for (int i = 0; i < matrix.length; i++) {
                                for (int j = 0; j < matrix.length; j++) {
                                    for (int m = 0; m < scan_size; m++) {
                                        for (int n = 0; n < scan_size; n++) {
                                            scanMatrix[m][n] = imageAlgorithm.getMatrixValue(matrix, i + m - dis, j + n - dis);
                                        }
                                    }
                                    int sum = 0;
                                    for (int x = 0; x < scanMatrix.length; x++) {
                                        for (int y = 0; y < scanMatrix.length; y++) {
                                            sum = sum + scanMatrix[x][y];
                                        }
                                    }
                                    if (sum > 3 && scanMatrix[1][1] >= 1) {
                                        matrix2[i][j] = 0;
                                        int[] xcoord = {i, j};
                                        list.add(xcoord);
                                    }
                                }
                            }

                            /**
                             * 去掉交叉区域后找连通区域
                             */

                            Integer[][] init_Matrix = imageAlgorithm.initFloodFill2(matrix);
                            Integer[][] ff_Matrix = ca.doLabel(init_Matrix);

                            String[][] resultArray = new String[matrix_size][matrix_size];

                            for (int x = 0; x < ff_Matrix.length; x++) {
                                for (int y = 0; y < ff_Matrix[x].length; y++) {
                                    if (ff_Matrix[x][y] == null)
                                        resultArray[x][y] = "0";
                                    else
                                        resultArray[x][y] = String.valueOf(ff_Matrix[x][y]);
                                }
                            }
                            for (int[] xcoord : list) {
                                resultArray[xcoord[0]][xcoord[1]] = "x";
                            }
                            /**
                             * 通过周边值侵蚀x区域
                             */
                            //String[][] finalArray;

                            while (true) {
                                resultArray = imageAlgorithm.batchDeal(resultArray);
                                int x_num = 0;
                                for (String[] array : resultArray) {
                                    for (String str : array) {
                                        if (str.equals("x")) {
                                            x_num++;
                                        }
                                    }
                                }
                                if (x_num == 0)
                                    break;
                            }
                            Map<String, java.util.List<int[]>> region = imageAlgorithm.matrixToRegion(resultArray);

                            Iterator iterator = region.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry entry = (Map.Entry) iterator.next();
                                String new_group_code = (String) entry.getKey(); //重新找连通性后的编码
                                java.util.List<int[]> xyList = (java.util.List<int[]>) entry.getValue();
                                if (xyList != null && xyList.size() > 0) {
                                    int[] start = xyList.get(0);
                                    int group_code = skeletonArray[start[0]][start[1]];
                                    double confidenceValue = confidenceMap.get(group_code);
                                    String key = mCode + "|" + group_code + "|" + new_group_code + "|" + confidenceValue;
                                    //context.write(new Text(key), new Text(serializeUtil.serialize(xyList)));
                                    xyList = imageAlgorithm.sortNodes(xyList);

                                    if(xyList.size() > 0){
                                        JSONObject json = new JSONObject();
                                        json.put("mercator", mCode);
                                        json.put("key", group_code);
                                        json.put("code", new_group_code);
                                        json.put("confidence", confidenceValue);
                                        JSONObject geojson = new JSONObject();
                                        geojson.put("type", "LineString");

                                        JSONArray coordinates = new JSONArray();
                                        for (int[] xy : xyList) {
                                            int x = xy[1];
                                            int y = xy[0];
                                            IntCoordinate pixel = mkt.inTile2Pixels(x, y, mCode);
                                            Coordinate coord = mkt.pixels2LonLat(pixel);
                                            JSONArray coordinate = new JSONArray();
                                            coordinate.add(Double.parseDouble(df.format(coord.x)));
                                            coordinate.add(Double.parseDouble(df.format(coord.y)));
                                            coordinates.add(coordinate);
                                        }

                                        geojson.put("coordinates", coordinates);
                                        json.put("geometry", geojson);
                                        jsonReturnArray.add(json);
                                        //context.write(new Text(json.toString()), new Text(""));
                                    }

                                }

                            }
                        }
                        return new Tuple2(Bytes.toString(results._1().get()), jsonReturnArray.toJSONString());
                    }
                }
        );


        System.out.println("=================count:" + hBaseRDD.count() + "=================");
        JavaRDD value = road_detect.values();
        String now = DateTimeUtil.formatDate("yyyyMMddHHmmss");
        value.saveAsTextFile("/data/spark_output_" + source + "_" + now);
    }

    public static void main(String[] args) {
        String master = args[0];
        String table = args[1];
        String family = args[2];
        String source = args[3];
        DetectionResultExportSpark spark = new DetectionResultExportSpark(master, table, family, source);
        spark.deal();
    }
}
