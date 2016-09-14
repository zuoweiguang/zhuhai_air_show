package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.*;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.dom4j.Element;
import org.dom4j.Node;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/12.
 */
public class RoadDetectionMR {
    public static class RoadDetectionMapper extends
            TableMapper<ImmutableBytesWritable, Put> {
        public static final Logger logger = Logger.getLogger(RoadDetectionMR.class);
        private String family = "";
        private String source = "";
        private int initFilter = 10;
        private int firstPixelFilter = 100;
        private double eccentricityFilter = 0.95;
        private int secondPixelFilter = 200;

        private static int expansionLength = 10;

        @Override
        public void setup(Context context) {
            family = context.getConfiguration().get("family");
            source = context.getConfiguration().get("source");
            initFilter = Integer.parseInt(context.getConfiguration().get("initFilter"));
            firstPixelFilter = Integer.parseInt(context.getConfiguration().get("firstPixelFilter"));
            secondPixelFilter = Integer.parseInt(context.getConfiguration().get("secondPixelFilter"));
            eccentricityFilter = Double.parseDouble(context.getConfiguration().get("eccentricityFilter"));
        }

        @Override
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {

            boolean existRoad = false;
            boolean existSource = false;
            String rowKey = Bytes.toString(result.getRow());
            ConnectedAlgorithm ca = new ConnectedAlgorithm();

            ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
            for (Cell cell : result.rawCells()) {
                String cell_family = new String(CellUtil.cloneFamily(cell));
                String cell_qualifier = new String(CellUtil.cloneQualifier(cell));
                if (cell_family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && cell_qualifier.equals(Constants.ROAD_DETECT_ROAD_QUALIFIER)) {
                    existRoad = true;
                }
                if (cell_family.equals(family) && cell_qualifier.equals(source)) {
                    existSource = true;
                }
            }
            if (existSource && existRoad /*&& !rowKey.equals(StringUtil.reverse("3422_1663"))
                    && !rowKey.equals(StringUtil.reverse("3314_1626"))
                    && !rowKey.equals(StringUtil.reverse("3437_1687"))
                    && !rowKey.equals(StringUtil.reverse("3401_1695"))*/) {
                byte[] roadByte = result.getValue(
                        Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                        Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());

                byte[] sourceByte = result.getValue(
                        family.getBytes(),
                        source.getBytes());

                Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte ,true);
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte ,true);
                Integer[][] filterMatrix;

                if (!source.equals(Constants.SOURCE_BAIDU)) {
                    //step 1: 小于10个点的像素滤掉
                    filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, initFilter);
                } else {
                    filterMatrix = imageAlgorithm.filterStepOne(sourceMatrix, initFilter);
                }


                //step 2: 路网轨迹二值化并擦除
                Integer[][] minusMatrix = imageAlgorithm.matrixMinus(filterMatrix, roadMatrix);

                //step 3: 中值滤波
                Integer[][] medianMatrix = imageAlgorithm.medianFilter(minusMatrix, 3);
                //step 4: 寻找连通区域
                Integer[][] init_ff_Matrix1 = imageAlgorithm.initFloodFill(medianMatrix);
                //Integer[][] ff_Matrix1 = imageAlgorithm.floodFill(init_ff_Matrix1);
                Integer[][] ff_Matrix1 = ca.doLabel(init_ff_Matrix1);

                //step 5: 计算区域斜率、离心率
                Map<String, List<Integer[]>> region1 = imageAlgorithm.matrixToRegion(ff_Matrix1, 0);
                Map<String, Map<String, Object>> calculateResult1 = imageAlgorithm.CalEccentricityAndOrientation(region1, false, 0);
                //step 6: 顺斜率方向膨胀，二值化
                Map<String, Object> oriMap1 = calculateResult1.get("orientation");
                Map<String, List<int[]>> lineMap1 = imageAlgorithm.CalculationLineStrel(expansionLength, oriMap1);
                Integer[][] exp_Matrix1 = imageAlgorithm.expansion(region1, lineMap1);
                //step 7: 寻找连通区域，保留连通区域大于100像素，且离心率大约0.95的区域
                Integer[][] init_ff_Matrix2 = imageAlgorithm.initFloodFill(exp_Matrix1);
                //Integer[][] ff_Matrix2 = imageAlgorithm.floodFill(init_ff_Matrix2);
                Integer[][] ff_Matrix2 = ca.doLabel(init_ff_Matrix2);

                Map<String, List<Integer[]>> region2 = imageAlgorithm.matrixToRegion(ff_Matrix2, firstPixelFilter);
                Map<String, Map<String, Object>> calculateResult2 = imageAlgorithm.CalEccentricityAndOrientation(region2, true, eccentricityFilter);
                Map<String, Object> oriMap2 = calculateResult2.get("xyList");
                Integer[][] exp_Matrix2 = imageAlgorithm.regionToMatrix(oriMap2);

                //step 8: 与step1的结果求交集 并再次用路网擦除
                Integer[][] intersectionMatrix = imageAlgorithm.matrixIntersection(filterMatrix, exp_Matrix2);
                Integer[][] minusMatrix2 = imageAlgorithm.matrixMinus(intersectionMatrix, roadMatrix);

                //step 9: 寻找连通区域，计算区域斜率、离心率顺方向膨胀保留连通区域>=200的区域
                Integer[][] init_ff_Matrix3 = imageAlgorithm.initFloodFill(minusMatrix2);
                //Integer[][] ff_Matrix3 = imageAlgorithm.floodFill(init_ff_Matrix3);
                Integer[][] ff_Matrix3 = ca.doLabel(init_ff_Matrix3);

                Map<String, List<Integer[]>> region3 = imageAlgorithm.matrixToRegion(ff_Matrix3, 0);

                Map<String, Map<String, Object>> calculateResult3 = imageAlgorithm.CalEccentricityAndOrientation(region3, false, 0);
                Map<String, Object> oriMap3 = calculateResult3.get("orientation");

//                if (source.equals(Constants.SOURCE_BAIDU)) {
//                    expansionLength = 5;
//                }

                Map<String, List<int[]>> lineMap2 = imageAlgorithm.CalculationLineStrel(expansionLength, oriMap3);
                Integer[][] exp_Matrix3 = imageAlgorithm.expansion(region3, lineMap2);
                Integer[][] init_ff_Matrix4 = imageAlgorithm.initFloodFill(exp_Matrix3);
                //Integer[][] ff_Matrix4 = imageAlgorithm.floodFill(init_ff_Matrix4);
                Integer[][] ff_Matrix4 = ca.doLabel(init_ff_Matrix4);

                Map<String, List<Integer[]>> region4 = imageAlgorithm.matrixToRegion(ff_Matrix4, secondPixelFilter);

                Integer[][] final_Matrix = imageAlgorithm.mapToMatrix(region4);

                //step 10:抽骨骼
                int width = 1024;
                int height = 1024;

                ByteProcessor bp = new ByteProcessor(width, height);
                BinaryProcessor ibp = new BinaryProcessor(bp);

                ibp.setColor(Color.white);
                ibp.fill();
                for (int x = 0; x < 1024; x++) {
                    for (int y = 0; y < 1024; y++) {
                        if (final_Matrix[x][y] != null)
                            ibp.putPixel(x, y, 0);
                    }
                }

                ibp.skeletonize();

                Integer[][] skeletonArray = new Integer[1024][1024];
                for (int x = 0; x < 1024; x++) {
                    for (int y = 0; y < 1024; y++) {
                        if (ibp.get(x, y) == 0) {
                            skeletonArray[x][y] = 1;
                        }
                    }
                }

                boolean isEmpty = true;

                for (int i = 0; i < 1024; i++) {
                    for (int j = 0; j < 1024; j++) {
                        if (skeletonArray[i][j] != null
                                && skeletonArray[i][j] != 0) {
                            isEmpty = false;
                        }
                    }
                }
                if (!isEmpty) {
                    byte[] finalByte = MatrixUtil.serializeSparseMatrix(skeletonArray, true);
                    Put put = new Put(rowKey.getBytes());
                    put.addColumn(family.getBytes(),
                            source.getBytes(), finalByte);
                    context.write(rowkey, put);
                }
            }
        }
    }

    public static class RoadDetectionReducer extends
            TableReducer<ImmutableBytesWritable, Put, NullWritable> {
        @Override
        public void reduce(ImmutableBytesWritable key, Iterable<Put> values,
                           Context context) throws IOException, InterruptedException {
            context.write(NullWritable.get(), values.iterator().next());
        }

    }

    public static void main(String[] args) throws Exception {
        String source_table = args[0];
        String family = args[1];
        String source = args[2];
        String config = args[3];
        String target_table = args[4];
        System.out.println("source_table = " + source_table);
        System.out.println("family = " + family);
        System.out.println("source = " + source);
        System.out.println("config = " + config);
        System.out.println("target_table = " + target_table);
        Configuration conf = new Configuration();
        List<Element> list = XmlUtil.parseXml2List(config);
        conf.set("family", family);
        conf.set("source", source);
        for (Element element : list) {
            if (source.equals(element.getName().trim())) {
                for (int i = 0, size = element.nodeCount(); i < size; i++) {
                    Node node = element.node(i);
                    if (node instanceof Element) {
                        conf.set(node.getName().trim(), node.getText().trim());
                        System.out.println(node.getName().trim() + ":" + node.getText().trim());
                    }
                }
            }
        }

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);

        Job job = Job.getInstance(conf, "RoadDetectionMR");

        job.setJarByClass(RoadDetectionMR.class);
        Scan scan = new Scan();
        // 2015-10-16新增
        scan.setCacheBlocks(false);
        scan.setCaching(10);

        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());
        scan.addColumn(family.getBytes(),
                source.getBytes());
        TableMapReduceUtil.initTableMapperJob(source_table,
                scan, RoadDetectionMR.RoadDetectionMapper.class,
                ImmutableBytesWritable.class, Put.class, job);
        TableMapReduceUtil.initTableReducerJob(target_table,
                RoadDetectionMR.RoadDetectionReducer.class, job);
        job.setNumReduceTasks(16);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static void azkabanRun(String source_table, String family, String source, String config, String target_table) throws Exception {
        Configuration conf = new Configuration();
        List<Element> list = XmlUtil.parseXml2List(config);
        conf.set("family", family);
        conf.set("source", source);
        for (Element element : list) {
            if (source.equals(element.getName().trim())) {
                for (int i = 0, size = element.nodeCount(); i < size; i++) {
                    Node node = element.node(i);
                    if (node instanceof Element) {
                        conf.set(node.getName().trim(), node.getText().trim());
                        System.out.println(node.getName().trim() + ":" + node.getText().trim());
                    }
                }
            }
        }

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);

        Job job = Job.getInstance(conf, "RoadDetectionMR");

        job.setJarByClass(RoadDetectionMR.class);
        Scan scan = new Scan();
        // 2015-10-16新增
        scan.setCacheBlocks(false);
        scan.setCaching(10);

        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());
        scan.addColumn(family.getBytes(),
                source.getBytes());
        TableMapReduceUtil.initTableMapperJob(source_table,
                scan, RoadDetectionMR.RoadDetectionMapper.class,
                ImmutableBytesWritable.class, Put.class, job);
        TableMapReduceUtil.initTableReducerJob(target_table,
                RoadDetectionMR.RoadDetectionReducer.class, job);
        job.setNumReduceTasks(16);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
