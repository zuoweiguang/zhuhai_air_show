package com.navinfo.mapspotter.process.topic.missingroad;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;
import com.navinfo.mapspotter.foundation.util.XmlUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/12.
 */
public class MissingRoadDetectionMR extends Configured implements Tool {


    public static class MissingRoadDetectionMapper extends
            TableMapper<ImmutableBytesWritable, Put> {
        public static final Logger logger = Logger.getLogger(MissingRoadDetectionMR.class);

        private String traj_table = "";
        private String road_family = "";
        private String traj_family = "";
        private String source = "";
        private double initFilter = 10;
        private int pixelFilter = 25;
        private int squareExpansionRoad = 3;
        private int squareExpansionTunnel = 9;
        private static int expansionLength = 10;

        private static Configuration configuration;
        public Hbase hbase = null;
        public Table table = null;

        public ConnectedAlgorithm ca;
        public ImageAlgorithm imageAlgorithm;

        @Override
        public void setup(Context context) {

            traj_table = context.getConfiguration().get("traj_table");
            road_family = context.getConfiguration().get("road_family");
            traj_family = context.getConfiguration().get("traj_family");
            source = context.getConfiguration().get("source");
            initFilter = Double.parseDouble(context.getConfiguration().get("initFilter"));
            pixelFilter = Integer.parseInt(context.getConfiguration().get("pixelFilter"));
            squareExpansionRoad = Integer.parseInt(context.getConfiguration().get("squareExpansionRoad"));
            squareExpansionTunnel = Integer.parseInt(context.getConfiguration().get("squareExpansionTunnel"));
            configuration = HBaseConfiguration.create();
            hbase = Hbase.createWithConfiguration(configuration);
            table = hbase.getTable(traj_table);
            ca = new ConnectedAlgorithm();
            imageAlgorithm = new ImageAlgorithm();

        }

        public static String COLUMN_ROAD = "road";
        public static String COLUMN_TUNNEL = "tunnel";
        public static String COLUMN_FERRY = "ferry";
        public static String COLUMN_RAILWAY = "railway";

        public static String SOURCE_SOGOU = "sogou";
        public static String SOURCE_BAIDU = "baidu";
        public static String SOURCE_DIDI = "didi";

        @Override
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            boolean existRoad = false;
            boolean existSource = false;
            String rowKey = new String(rowkey.get());
            Get get = new Get(rowKey.getBytes());
            Result traj_result = table.get(get);

            for (Cell cell : result.rawCells()) {
                String cell_family = new String(CellUtil.cloneFamily(cell));
                String cell_qualifier = new String(CellUtil.cloneQualifier(cell));
                if (cell_family.equals(road_family) && cell_qualifier.equals(COLUMN_ROAD)) {
                    existRoad = true;
                }
            }
            byte[] sourceByte = traj_result.getValue(traj_family.getBytes(), source.getBytes());

            if(sourceByte != null){
                existSource = true;
            }
            if (existRoad && existSource) {
                byte[] roadByte = result.getValue(road_family.getBytes(), COLUMN_ROAD.getBytes());
                byte[] tunnelByte = result.getValue(road_family.getBytes(), COLUMN_TUNNEL.getBytes());
                byte[] railwayByte = result.getValue(road_family.getBytes(), COLUMN_RAILWAY.getBytes());
                byte[] ferryByte = result.getValue(road_family.getBytes(), COLUMN_FERRY.getBytes());

                Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte, true);
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte, true);

                //step 1: 基础过滤
                Integer[][] noiseMatrix = null;
                if (source.equals(SOURCE_BAIDU)) {
                    noiseMatrix = imageAlgorithm.filterByPercent(sourceMatrix, initFilter);
                }
                if (source.equals(SOURCE_DIDI)) {
                    noiseMatrix = imageAlgorithm.filterByPercent(sourceMatrix, initFilter);
                }
                if (source.equals(SOURCE_SOGOU)) {
                    noiseMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, initFilter);
                }

                //step 2: 路网轨迹二值化
                Integer[][] minusMatrix = imageAlgorithm.matrixMinus(noiseMatrix, roadMatrix);
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

                    Integer[][] filterMatrix = null;

                    if (source.equals(SOURCE_SOGOU)) {
                        if (tunnelByte != null) {
                            filterMatrix = MatrixUtil.deserializeMatrix(tunnelByte, true);
                        }
                    }
                    if (source.equals(SOURCE_BAIDU)) {
                        if (railwayByte != null) {
                            filterMatrix = MatrixUtil.deserializeMatrix(railwayByte, true);
                        }
                    }
                    if (filterMatrix != null) {
                        Integer[][] init_filter_Matrix = imageAlgorithm.initFloodFill(filterMatrix);
                        Integer[][] filter_Matrix = ca.doLabel(init_filter_Matrix);

                        Map<String, List<Integer[]>> filter_region = imageAlgorithm.matrixToRegion(filter_Matrix, 0);
                        Map<String, Map<String, Object>> calculateFilter = imageAlgorithm.CalEccentricityAndOrientation(filter_region, false, 0);
                        Map<String, Object> filterMap = calculateFilter.get("orientation");
                        Map<String, List<int[]>> filter_lineMap = imageAlgorithm.CalculationLineStrel(expansionLength, filterMap);
                        Integer[][] filterLineExpansion = imageAlgorithm.expansion(filter_region, filter_lineMap);

                        Map<String, List<Integer[]>> filterRegion = imageAlgorithm.matrixToRegion(filterLineExpansion, 0);
                        Map<String, List<Integer[]>> filterSquareExpansion = imageAlgorithm.regionSquareExpansion(filterRegion, squareExpansionTunnel);
                        Integer[][] finalFilterMatrix = imageAlgorithm.mapToMatrixWithLabelNumber(filterSquareExpansion);
                        region2 = imageAlgorithm.filterMatrix(region2, finalFilterMatrix);

                        final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);
                    }

                    if (ferryByte != null) {
                        Integer[][] ferryMatrix = MatrixUtil.deserializeMatrix(ferryByte, true);

                        Integer[][] init_ferry_Matrix = imageAlgorithm.initFloodFill(ferryMatrix);
                        Integer[][] ferry_Matrix = ca.doLabel(init_ferry_Matrix);

                        Map<String, List<Integer[]>> ferry_region = imageAlgorithm.matrixToRegion(ferry_Matrix, 0);
                        Map<String, Map<String, Object>> calculateFerry = imageAlgorithm.CalEccentricityAndOrientation(ferry_region, false, 0);
                        Map<String, Object> ferryMap = calculateFerry.get("orientation");
                        Map<String, List<int[]>> ferry_lineMap = imageAlgorithm.CalculationLineStrel(expansionLength, ferryMap);
                        Integer[][] ferryLineExpansion = imageAlgorithm.expansion(ferry_region, ferry_lineMap);

                        Map<String, List<Integer[]>> ferryRegion = imageAlgorithm.matrixToRegion(ferryLineExpansion, 0);
                        Map<String, List<Integer[]>> ferrySquareExpansion = imageAlgorithm.regionSquareExpansion(ferryRegion, squareExpansionTunnel);
                        Integer[][] finalFerryMatrix = imageAlgorithm.mapToMatrixWithLabelNumber(ferrySquareExpansion);
                        region2 = imageAlgorithm.filterMatrix(region2, finalFerryMatrix);
                        final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);

                    }

                    Iterator<Map.Entry<String, Double>> entries = analysisArg1.entrySet().iterator();
                    JSONArray array = new JSONArray();
                    while (entries.hasNext()) {
                        Map.Entry<String, Double> entry = entries.next();
                        String key = entry.getKey();

                        double arg1 = analysisArg1.get(key);
                        double arg2 = (Double) analysisArg2.get(key);
                        double arg3 = analysisArg3.get(key);

                        double _result = (arg1 * 0.5 + arg2 * 0.25 + (1 - arg3) * 0.25);

                        JSONObject json = new JSONObject();
                        json.put("key", Integer.parseInt(key));
                        json.put("intensity", arg1);
                        json.put("eccentricity", arg2);
                        json.put("overlapr", 1 - arg3);
                        json.put("confidence", _result);
                        array.add(json);
                    }

                    byte[] finalByte = MatrixUtil.serializeSparseMatrix(final_Matrix, true);
                    Put put = new Put(rowKey.getBytes());
                    put.addColumn(traj_family.getBytes(), source.getBytes(), finalByte);
                    put.addColumn(traj_family.getBytes(), (source + "_confidence").getBytes(), array.toJSONString().getBytes());
                    context.write(rowkey, put);
                }
            }
        }
    }

    public static class MissingRoadDetectionReducer extends
            TableReducer<ImmutableBytesWritable, Put, NullWritable> {
        @Override
        public void reduce(ImmutableBytesWritable key, Iterable<Put> values,
                           Context context) throws IOException, InterruptedException {
            context.write(NullWritable.get(), values.iterator().next());
        }

    }

    @Override
    public int run(String[] args) throws Exception {
        String road_table = args[0];
        String road_family = args[1];
        String traj_table = args[2];
        String traj_family = args[3];
        String source = args[4];
        String config = args[5];
        String target_table = args[6];
        System.out.println("road_table = " + road_table);
        System.out.println("road_family = " + road_family);
        System.out.println("traj_table = " + traj_table);
        System.out.println("traj_family = " + traj_family);
        System.out.println("source = " + source);
        System.out.println("config = " + config);
        System.out.println("target_table = " + target_table);

        Configuration conf = new Configuration();
        List<Element> list = XmlUtil.parseXml2List(config);

        conf.set("road_family", road_family);

        conf.set("traj_table", traj_table);
        conf.set("traj_family", traj_family);
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

        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);

        Job job = Job.getInstance(conf, "MissingRoadDetect_" + road_family + "_" + traj_family + "_" + source);

        job.setJarByClass(MissingRoadDetectionMR.class);

        Scan roadScan = new Scan();
        roadScan.setCaching(10);
        roadScan.setCacheBlocks(false);
        roadScan.addColumn(road_family.getBytes(), "road".getBytes());
        roadScan.addColumn(road_family.getBytes(), "tunnel".getBytes());
        roadScan.addColumn(road_family.getBytes(), "ferry".getBytes());
        roadScan.addColumn(road_family.getBytes(), "railway".getBytes());

        TableMapReduceUtil.initTableMapperJob(road_table,
                roadScan, MissingRoadDetectionMR.MissingRoadDetectionMapper.class,
                ImmutableBytesWritable.class, Put.class, job);
        TableMapReduceUtil.initTableReducerJob(target_table,
                MissingRoadDetectionMR.MissingRoadDetectionReducer.class, job);
        job.setNumReduceTasks(16);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MissingRoadDetectionMR(), args);
        System.exit(res);
    }

    public static void azkabanRun(String road_table, String road_family, String traj_table, String traj_family,
                                  String source, String config, String target_table) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MissingRoadDetectionMR(), new String[]{road_table, road_family, traj_table, traj_family,
                source, config, target_table});
        System.exit(res);
    }
}
