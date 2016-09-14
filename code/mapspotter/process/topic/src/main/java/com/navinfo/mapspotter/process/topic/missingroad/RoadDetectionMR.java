package com.navinfo.mapspotter.process.topic.missingroad;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;
import com.navinfo.mapspotter.foundation.util.XmlUtil;
import com.navinfo.mapspotter.process.topic.roaddetect.Constants;
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

import java.io.IOException;
import java.util.Iterator;
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
        private double initFilter = 10d;
        private int pixelFilter = 25;
        private int squareExpansionRoad = 3;
        private int squareExpansionTunnel = 9;

        private static int expansionLength = 10;

        @Override
        public void setup(Context context) {
            family = context.getConfiguration().get("family");
            source = context.getConfiguration().get("source");
            initFilter = Double.parseDouble(context.getConfiguration().get("initFilter"));
            pixelFilter = Integer.parseInt(context.getConfiguration().get("pixelFilter"));
            squareExpansionRoad = Integer.parseInt(context.getConfiguration().get("squareExpansionRoad"));
            squareExpansionTunnel = Integer.parseInt(context.getConfiguration().get("squareExpansionTunnel"));
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
            if (existSource && existRoad) {
                byte[] roadByte = result.getValue(
                        Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                        Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());

                byte[] sourceByte = result.getValue(
                        family.getBytes(),
                        source.getBytes());

                byte[] tunnelByte = result.getValue(
                        Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                        Constants.ROAD_DETECT_TUNNEL_QUALIFIER.getBytes());

                Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte, true);
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte, true);


                //step 1: 基础过滤
                Integer[][] filterMatrix = null;

                if (this.source.equals("baidu")) {
                    filterMatrix = imageAlgorithm.filterByPercent(sourceMatrix, 0.05D);
                }
                else if (this.source.equals("didi")) {
                    filterMatrix = imageAlgorithm.filterByPercent(sourceMatrix, 0.25D);
                }
                else {
                    filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, this.initFilter);
                }
                //step 2: 路网轨迹二值化
                Integer[][] minusMatrix = imageAlgorithm.matrixMinus(filterMatrix, roadMatrix);
                //step 3: 中值滤波
                Integer[][] medianMatrix = imageAlgorithm.medianFilter(minusMatrix, 3);
                //step 4.1: 寻找连通区域
                Integer[][] init_ff_Matrix1 = imageAlgorithm.initFloodFill(medianMatrix);
                Integer[][] ff_Matrix1 = ca.doLabel(init_ff_Matrix1);
                //step 4.2: 计算区域斜率、离心率
                Map<String, List<Integer[]>> region1 = imageAlgorithm.matrixToRegion(ff_Matrix1, 0);
                Map<String, Map<String, Object>> calculateResult1 = imageAlgorithm.CalEccentricityAndOrientation(region1, false, 0);
                //step 4.3: 顺斜率方向膨胀，二值化
                Map<String, Object> oriMap1 = calculateResult1.get("orientation");
                Map<String, List<int[]>> lineMap1 = imageAlgorithm.CalculationLineStrel(expansionLength, oriMap1);
                Integer[][] exp_Matrix1 = imageAlgorithm.expansion(region1, lineMap1);

                //step 5.1: step2结果与step4结果做交集
                Integer[][] intersectionMatrix = imageAlgorithm.matrixIntersection(minusMatrix, exp_Matrix1);
                //step 5.2: 过滤连通区域小于25的
                Integer[][] init_ff_Matrix2 = imageAlgorithm.initFloodFill(intersectionMatrix);
                Integer[][] ff_Matrix2 = ca.doLabel(init_ff_Matrix2);
                Map<String, List<Integer[]>> region2 = imageAlgorithm.matrixToRegion(ff_Matrix2, pixelFilter);
                Integer[][] final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);

                boolean isEmpty = true;

                for (int i = 0; i < 1024; i++) {
                    for (int j = 0; j < 1024; j++) {
                        if (final_Matrix[i][j] != null
                                && final_Matrix[i][j] != 0) {
                            isEmpty = false;
                        }
                    }
                }
                if (!isEmpty) {

                    //计算参数1：密度
                    double tile_avg = imageAlgorithm.martixAverage(sourceMatrix);
                    Map<String, Double> analysisArg1 = imageAlgorithm.analysisArg1(region2, sourceMatrix, tile_avg);

                    //计算参数2：斜率
                    Map<String, Map<String, Object>> calculateResult2 = imageAlgorithm.CalEccentricityAndOrientation(region2, false, 0);
                    Map<String, Object> analysisArg2 = calculateResult2.get("eccentricity");

                    //计算参数3：路网相交比
                    Map<String, List<Integer[]>> squareExpansion = imageAlgorithm.regionSquareExpansion(region2, squareExpansionRoad);
                    Map<String, Double> analysisArg3 = imageAlgorithm.analysisArg3(squareExpansion, roadMatrix, region2);

                    //计算参数4：隧道相交参数
                    Map<String, Double> analysisArg4 = null;

                    if (tunnelByte != null) {
                        Integer[][] tunnelMatrix = MatrixUtil.deserializeMatrix(tunnelByte, true);
                        Integer[][] init_tunnel_Matrix = imageAlgorithm.initFloodFill(tunnelMatrix);
                        Integer[][] tunnel_Matrix = ca.doLabel(init_tunnel_Matrix);

                        Map<String, List<Integer[]>> tunnel_region = imageAlgorithm.matrixToRegion(tunnel_Matrix, 0);
                        Map<String, Map<String, Object>> calculateTunnel = imageAlgorithm.CalEccentricityAndOrientation(tunnel_region, false, 0);
                        Map<String, Object> tunnelMap = calculateTunnel.get("orientation");
                        Map<String, List<int[]>> tunnel_lineMap = imageAlgorithm.CalculationLineStrel(expansionLength, tunnelMap);
                        Integer[][] tunnelLineExpansion = imageAlgorithm.expansion(tunnel_region, tunnel_lineMap);

                        Map<String, List<Integer[]>> tunnelRegion = imageAlgorithm.matrixToRegion(tunnelLineExpansion, 0);
                        Map<String, List<Integer[]>> tunnelSquareExpansion = imageAlgorithm.regionSquareExpansion(tunnelRegion, squareExpansionTunnel);
                        Integer[][] finalTunnelMatrix = imageAlgorithm.mapToMatrixWithLabelNumber(tunnelSquareExpansion);
                        analysisArg4 = imageAlgorithm.analysisArg4(region2, finalTunnelMatrix);
                    }

                    Iterator<Map.Entry<String, Double>> entries = analysisArg1.entrySet().iterator();
                    JSONArray array = new JSONArray();
                    while (entries.hasNext()) {
                        Map.Entry<String, Double> entry = entries.next();
                        String key = entry.getKey();

                        double arg1 = analysisArg1.get(key);
                        double arg2 = (Double) analysisArg2.get(key);
                        double arg3 = analysisArg3.get(key);
                        double arg4 = 1;
                        if (analysisArg4 != null) {
                            arg4 = analysisArg4.get(key);
                        }
                        double _result = (arg1 * 0.5 + arg2 * 0.25 + (1 - arg3) * 0.25) * arg4;

                        JSONObject json = new JSONObject();
                        json.put("key", Integer.parseInt(key));
                        json.put("intensity", arg1);
                        json.put("eccentricity", arg2);
                        json.put("overlapr", 1 - arg3);
                        json.put("tunnel", arg4 == 1 ? 0 : 1);
                        json.put("confidence", _result);
                        array.add(json);
                    }

                    byte[] finalByte = MatrixUtil.serializeSparseMatrix(final_Matrix, true);
                    Put put = new Put(rowKey.getBytes());
                    put.addColumn(family.getBytes(),
                            source.getBytes(), finalByte);
                    put.addColumn(family.getBytes(),
                            (source + "_confidence").getBytes(), array.toJSONString().getBytes());
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
//        scan.setCacheBlocks(false);
//        scan.setCaching(10);
//
//        Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL,
//                new BinaryComparator(StringUtil.reverse("3253_1694").getBytes()));

//        scan.setStartRow(StringUtil.reverse("3253_1694").getBytes());

//        scan.setStopRow((StringUtil.reverse("3253_1694")+"{").getBytes());

        //scan.setFilter(filter1);
        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());
        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_TUNNEL_QUALIFIER.getBytes());
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
        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_TUNNEL_QUALIFIER.getBytes());
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
