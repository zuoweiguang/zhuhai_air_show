package com.navinfo.mapspotter.process.topic.missingroad;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;

import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;

import com.navinfo.mapspotter.foundation.util.StringUtil;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/2/1.
 */
public class SingleRoadDetection {


    Configuration configuration = null;
    Hbase hbase = null;

    public SingleRoadDetection() {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "Master.Hadoop:2181,Slave3.Hadoop:2181");
        //configuration.set("hbase.zookeeper.quorum","datanode01:2181,datanode02:2181,datanode03:2181");
        hbase = Hbase.createWithConfiguration(configuration);
    }


    private int initFilter = 1;
    private int pixelFilter = 10;
    private int squareExpansionRoad= 3;
    private int squareExpansionTunnel= 9;
    private int expansionLength = 10;


    public void singleDetect(String rowKey) throws Exception {
        Table table_base = hbase.getTable("road_base");
        Get get = new Get(rowKey.getBytes());
        String source = "baidu";
        Result result_base = table_base.get(get);

        boolean existRoad = false;
        boolean existSource = false;


        Table table_traj = hbase.getTable("road_traj_chengdu");
        Result result_traj = table_traj.get(get);

        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        ConnectedAlgorithm ca = new ConnectedAlgorithm();

        for (Cell cell : result_base.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals("16sum03") && qualifier.equals("road")) {
                existRoad = true;
            }
        }

        for (Cell cell : result_traj.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals("0603_0621") && qualifier.equals(source)) {
                existSource = true;
            }
        }



        if (existSource && existRoad) {

            byte[] roadByte = result_base.getValue("16sum03".getBytes(), "road".getBytes());
            byte[] tunnelByte = result_base.getValue("16sum03".getBytes(), "tunnel".getBytes());
            byte[] sourceByte = result_traj.getValue("0603_0621".getBytes(), source.getBytes());

            if (tunnelByte == null) {
                System.out.println("0");
            } else {
                System.out.println("1");
            }

            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte, true);
            Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte, true);

            imageAlgorithm.arrayToFile(roadMatrix, "E:\\fusion\\road\\test\\road.txt");
            imageAlgorithm.arrayToFile(sourceMatrix, "E:\\fusion\\road\\test\\source.txt");
            MatrixUtil.matrix2Image(roadMatrix, "E:\\fusion\\road\\test\\road.jpg");
            MatrixUtil.matrix2Image(sourceMatrix, "E:\\fusion\\road\\test\\source.jpg");
            //imageAlgorithm.filterByPercent(sourceMatrix, 0.05d);
            //step 1: 基础过滤
            Integer[][] filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, 1);
            imageAlgorithm.arrayToFile(filterMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_1.txt");
            MatrixUtil.matrix2Image(filterMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_1.jpg");

            //step 2: 路网轨迹二值化
            Integer[][] minusMatrix = imageAlgorithm.matrixMinus(filterMatrix, roadMatrix);
            imageAlgorithm.arrayToFile(minusMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_2.txt");
            MatrixUtil.matrix2Image(minusMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_2.jpg");

            //step 3: 中值滤波
            Integer[][] medianMatrix = imageAlgorithm.medianFilter(minusMatrix, 3);
            imageAlgorithm.arrayToFile(medianMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_3.txt");
            MatrixUtil.matrix2Image(medianMatrix, "E:\\fusion\\road\\test\\" + rowKey + "_3.jpg");

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
            imageAlgorithm.arrayToFile(exp_Matrix1, "E:\\fusion\\road\\test\\" + rowKey + "_4.txt");
            MatrixUtil.matrix2Image(exp_Matrix1, "E:\\fusion\\road\\test\\" + rowKey + "_4.jpg");

            //step 5.1: step2结果与step4结果做交集
            Integer[][] intersectionMatrix = imageAlgorithm.matrixIntersection(minusMatrix, exp_Matrix1);
            //step 5.2: 过滤连通区域小于25的
            Integer[][] init_ff_Matrix2 = imageAlgorithm.initFloodFill(intersectionMatrix);
            Integer[][] ff_Matrix2 = ca.doLabel(init_ff_Matrix2);
            Map<String, List<Integer[]>> region2 = imageAlgorithm.matrixToRegion(ff_Matrix2, 10);
            Integer[][] final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);
            imageAlgorithm.arrayToFile(final_Matrix, "E:\\fusion\\road\\test\\" + rowKey + "_5.txt");
            MatrixUtil.matrix2Image(final_Matrix, "E:\\fusion\\road\\test\\" + rowKey + "_5.jpg");

            Integer[][] final_Matrix_01 = imageAlgorithm.mapToMatrix(region2);
            imageAlgorithm.arrayToFile(final_Matrix_01, "E:\\fusion\\road\\test\\" + rowKey + "_6.txt");
            MatrixUtil.matrix2Image(final_Matrix_01, "E:\\fusion\\road\\test\\" + rowKey + "_6.jpg");

            double tile_avg = imageAlgorithm.martixAverage(sourceMatrix);
            Map<String, Double> analysisArg1 = imageAlgorithm.analysisArg1(region2, sourceMatrix, tile_avg);

            Map<String, Map<String, Object>> calculateResult2 = imageAlgorithm.CalEccentricityAndOrientation(region2, false, 0);

            Map<String, Object> analysisArg2 = calculateResult2.get("eccentricity");


            Map<String, List<Integer[]>> squareExpansion = imageAlgorithm.regionSquareExpansion(region2, 3);


            Map<String, Double> analysisArg3 = imageAlgorithm.analysisArg3(squareExpansion, roadMatrix, region2);

            Map<String, Double> analysisArg4 = null;

            if (tunnelByte != null) {
                Integer[][] tunnelMatrix = MatrixUtil.deserializeMatrix(tunnelByte, true);
                Integer[][] init_tunnel_Matrix = imageAlgorithm.initFloodFill(tunnelMatrix);
                Integer[][] tunnel_Matrix = ca.doLabel(init_tunnel_Matrix);

                Map<String, List<Integer[]>> tunnel_region = imageAlgorithm.matrixToRegion(tunnel_Matrix, 0);
                Map<String, Map<String, Object>> calculateTunnel = imageAlgorithm.CalEccentricityAndOrientation(tunnel_region, false, 0);
                //step 4.3: 顺斜率方向膨胀，二值化
                Map<String, Object> tunnelMap = calculateTunnel.get("orientation");
                Map<String, List<int[]>> tunnel_lineMap = imageAlgorithm.CalculationLineStrel(expansionLength, tunnelMap);
                Integer[][] tunnelLineExpansion = imageAlgorithm.expansion(tunnel_region, tunnel_lineMap);


                Map<String, List<Integer[]>> tunnelRegion = imageAlgorithm.matrixToRegion(tunnelLineExpansion, 0);
                Map<String, List<Integer[]>> tunnelSquareExpansion = imageAlgorithm.regionSquareExpansion(tunnelRegion, 9);
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
                //double arg4 = analysisArg4.get(key);
                if (analysisArg4 != null) {
                    arg4 = analysisArg4.get(key);
                }
                double _result = (arg1 * 0.5 + arg2 * 0.25 + (1 - arg3) * 0.25) * arg4;
                JSONObject json = new JSONObject();
                json.put("key", Integer.parseInt(key));
                json.put("intensity", arg1);
                json.put("eccentricity", arg2);
                json.put("overlapr", 1 - arg3);
                json.put("tunnel", arg4==1?0:1);
                json.put("confidence", _result);
                System.out.println(json);
                array.add(json);
            }
            //System.out.println(array.toJSONString());
        }
    }

    public static List<String> fileToList(String fileName) {
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            File file = new File(fileName);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line != null && !"".equals(line.trim()))
                    list.add(line.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
    String family = "201604";
    String source = "didi";
    public void singleDetect2(String rowKey) throws Exception {
        Table table_base = hbase.getTable("road_base");
        Get get = new Get(rowKey.getBytes());
        Result result_base = table_base.get(get);

        boolean existRoad = false;
        boolean existSource = false;


        Table table_traj = hbase.getTable("road_traj_chengdu");
        Result result_traj = table_traj.get(get);

        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        ConnectedAlgorithm ca = new ConnectedAlgorithm();

        for (Cell cell : result_base.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals("16sum03") && qualifier.equals("road")) {
                existRoad = true;
                break;
            }
        }

        for (Cell cell : result_traj.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals("0603_0621") && qualifier.equals(source)) {
                existSource = true;
                break;
            }
        }
        if (existSource && existRoad) {
            byte[] roadByte = result_base.getValue("16sum03".getBytes(), "road".getBytes());
            byte[] tunnelByte = result_base.getValue("16sum03".getBytes(), "tunnel".getBytes());
            byte[] sourceByte = result_traj.getValue("0603_0621".getBytes(), source.getBytes());



            String path = "E:\\fusion\\road\\201605\\";
            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte, true);
            Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte, true);

            //sourceMatrix = imageAlgorithm.medianFilter(sourceMatrix, 3);

            imageAlgorithm.arrayToFile(roadMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_road.txt");
            MatrixUtil.matrix2Image(roadMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_road.jpg");
            imageAlgorithm.arrayToFile(sourceMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_source.txt");
            MatrixUtil.matrix2Image(sourceMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_source.jpg");

            //step 1: 基础过滤
            Integer[][] filterMatrix = null;

            if (this.source.equals("baidu")) {
                filterMatrix = imageAlgorithm.filterByPercent(sourceMatrix, 0.05D);
            } else if (this.source.equals("didi")) {
                filterMatrix = imageAlgorithm.filterByPercent(sourceMatrix, 0.01D);
            } else {
                filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, this.initFilter);
            //    filterMatrix = imageAlgorithm.filterByPercent(sourceMatrix, 0.25D);
            }
            imageAlgorithm.arrayToFile(filterMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_noise.txt");
            MatrixUtil.matrix2Image(filterMatrix, path + "01_" + StringUtil.reverse(rowKey) + "_noise.jpg");


            //step 2: 路网轨迹二值化
            Integer[][] minusMatrix = imageAlgorithm.matrixMinus(filterMatrix, roadMatrix);
            //step 3: 中值滤波
            Integer[][] medianMatrix = imageAlgorithm.medianFilter(minusMatrix, 3);
//            medianMatrix = imageAlgorithm.medianFilter(medianMatrix, 3);
//            medianMatrix = imageAlgorithm.medianFilter(medianMatrix, 3);
            //step 4.1: 寻找连通区域
            Integer[][] init_ff_Matrix1 = imageAlgorithm.initFloodFill(medianMatrix);
            Integer[][] ff_Matrix1 = ca.doLabel(init_ff_Matrix1);
            //step 4.2: 计算区域斜率、离心率
            Map<String, List<Integer[]>> region1 = imageAlgorithm.matrixToRegion(ff_Matrix1, 15);
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

            imageAlgorithm.arrayToFile(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final1.txt");
            MatrixUtil.matrix2Image(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final1.jpg");

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



                //计算参数2：斜率
                Map<String, Map<String, Object>> calculateResult2 = imageAlgorithm.CalEccentricityAndOrientation(region2, false, 0);
                Map<String, Object> analysisArg2 = calculateResult2.get("eccentricity");

                Iterator<Map.Entry<String, List<Integer[]>>> regionEntries = region2.entrySet().iterator();

                while (regionEntries.hasNext()) {
                    Map.Entry<String, List<Integer[]>> region2Entry = regionEntries.next();
                    String region2Key =  region2Entry.getKey();
                    int regionCount = region2Entry.getValue().size();

                    if(regionCount >= 500 ){
                        if((Double) analysisArg2.get(region2Key) < 0.98){
                            regionEntries.remove();
                        }
                    }
                    else{
                        if((Double) analysisArg2.get(region2Key) < 0.7){
                            regionEntries.remove();
                        }
                    }
                }

                final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);

                imageAlgorithm.arrayToFile(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final2.txt");
                MatrixUtil.matrix2Image(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final2.jpg");

                //计算参数1：密度
                double tile_avg = imageAlgorithm.martixAverage(sourceMatrix);
                Map<String, Double> analysisArg1 = imageAlgorithm.analysisArg1(region2, sourceMatrix, tile_avg);

                //计算参数3：路网相交比
                Map<String, List<Integer[]>> squareExpansion = imageAlgorithm.regionSquareExpansion(region2, squareExpansionRoad);
                Map<String, Double> analysisArg3 = imageAlgorithm.analysisArg3(squareExpansion, roadMatrix, region2);


                Iterator<Map.Entry<String, List<Integer[]>>> regionEntries2 = region2.entrySet().iterator();

                while (regionEntries2.hasNext()) {
                    Map.Entry<String, List<Integer[]>> region2Entry2 = regionEntries2.next();
                    String region2Key =  region2Entry2.getKey();
                    double value = analysisArg3.get(region2Key);

                    if(value >= 0.2){
                        regionEntries2.remove();
                    }
                }
                final_Matrix = imageAlgorithm.mapToMatrixWithLabelNumber(region2);

                imageAlgorithm.arrayToFile(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final3.txt");
                MatrixUtil.matrix2Image(final_Matrix, path + "01_" + StringUtil.reverse(rowKey) + "_final3.jpg");


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
                    System.out.println(json);
                    array.add(json);
                }

                byte[] finalByte = MatrixUtil.serializeSparseMatrix(final_Matrix, true);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SingleRoadDetection singleRoadDetection = new SingleRoadDetection();
        singleRoadDetection.singleDetect2(StringUtil.reverse("3230_1679"));
    }
}
