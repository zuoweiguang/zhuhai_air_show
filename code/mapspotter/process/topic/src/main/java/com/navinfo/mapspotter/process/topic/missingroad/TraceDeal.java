package com.navinfo.mapspotter.process.topic.missingroad;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import java.awt.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Created by cuiliang on 2016/5/10.
 */
public class TraceDeal {

    public static Configuration configuration;
    Hbase hbase = null;

    public TraceDeal() {
        configuration = HBaseConfiguration.create();
        //configuration.set("hbase.zookeeper.quorum", "Master.Hadoop:2181,Slave3.Hadoop:2181");
        configuration.set("hbase.zookeeper.quorum", "datanode01:2181,datanode02:2181,datanode03:2181");
        hbase = Hbase.createWithConfiguration(configuration);
    }

    ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
    ConnectedAlgorithm ca = new ConnectedAlgorithm();
    private MercatorUtil mkt = new MercatorUtil(1024, 12);
    private DecimalFormat df = new DecimalFormat("#.#####");
    public Map<Integer, Double> confidenceMap;
    int size = 3;
    int dis = (size - 1) / 2;

    public Integer[][] readDetect(String key) throws Exception {
        Table table = hbase.getTable("road_detect_test");
        Get get = new Get(key.getBytes());
        String family = "201604";
        String qualifier = "sogou";
        Result result = table.get(get);

        byte[] detectByte = result.getValue(family.getBytes(), qualifier.getBytes());
        Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(detectByte, true);
        imageAlgorithm.arrayToFile(detectMatrix, path + "01_" + StringUtil.reverse(key) + ".txt");
        MatrixUtil.matrix2Image(detectMatrix, path + "01_" + StringUtil.reverse(key) + "_detect.jpg");
        int width = 1024;
        int height = 1024;

        byte[] confidenceByte = result.getValue(family.getBytes(), (qualifier + "_confidence").getBytes());

        String confidence = new String(confidenceByte);
        JSONArray jsonArray = JSON.parseArray(confidence);
//        Map<Integer, String> tunnelMap = new HashMap();

        confidenceMap = new HashMap();

        for (Object object : jsonArray) {
            JSONObject json = (JSONObject) object;
//            if (json.getInteger("tunnel") == 1)
//                tunnelMap.put(json.getInteger("key"), "");

            confidenceMap.put(json.getIntValue("key"),json.getDoubleValue("confidence"));
        }
//        detectMatrix = imageAlgorithm.filterTunnelMatrix(detectMatrix, tunnelMap);

        /**
         * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素中心点像素无值而其上下左右的点有值则给该点打上值
         */
        Integer[][] scanMatrix = new Integer[size][size];
        for (int i = 0; i < detectMatrix.length ; i++) {
            for (int j = 0; j < detectMatrix.length; j++) {
                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
                        scanMatrix[m][n] = imageAlgorithm.getMatrixValue(detectMatrix, i + m - dis, j + n - dis);
                    }
                }
                if (scanMatrix[1][1] == 0) {
                    if(scanMatrix[0][1] > 0 && scanMatrix[1][0] > 0 && scanMatrix[2][1] > 0 && scanMatrix[1][2] > 0)
                        detectMatrix[i][j] = scanMatrix[0][1];
                }
            }
        }
        MatrixUtil.matrix2Image(detectMatrix, path + "01_" + StringUtil.reverse(key) + "_patch.jpg");


        ByteProcessor bp = new ByteProcessor(width, height);
        BinaryProcessor ibp = new BinaryProcessor(bp);

        ibp.setColor(Color.white);
        ibp.fill();
        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 1024; y++) {
                if (detectMatrix[x][y] != null && detectMatrix[x][y] != 0)
                    ibp.putPixel(x, y, 0);
            }
        }

        ibp.skeletonize();

        Integer[][] skeletonArray = new Integer[1024][1024];
        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 1024; y++) {
                if (ibp.get(x, y) == 0) {
                    skeletonArray[x][y] = detectMatrix[x][y];
                }
            }
        }


        imageAlgorithm.arrayToFile(skeletonArray, path + "01_" + StringUtil.reverse(key) + "_skeleton.txt");
        MatrixUtil.matrix2Image(skeletonArray, path + "01_" + StringUtil.reverse(key) + "_skeleton.jpg");
        return skeletonArray;
    }

    String path = "E:\\fusion\\road\\trace\\";

    public void trace(Integer[][] inputMatrix, String mCode) throws Exception {
        System.out.println(DateTimeUtil.getNow());
        Integer[][] matrix = imageAlgorithm.binarizationMatrix(inputMatrix);
        Integer[][] matrix2 = imageAlgorithm.copyMatrix(matrix);

        Integer[][] scanMatrix = new Integer[size][size];
        java.util.List<int[]> list = new ArrayList<>();
        /**
         * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素大于3则将其记为0，并且将其坐标记录在list中
         */
        for (int i = 0; i < matrix.length ; i++) {
            for (int j = 0; j < matrix.length; j++) {
                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
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

        MatrixUtil.matrix2Image(matrix2, path + "03_" + mCode + ".jpg");
        Integer[][] init_Matrix = imageAlgorithm.initFloodFill2(matrix2);

        /**
         * 去掉交叉区域后找连通区域
         */
        Integer[][] ff_Matrix = ca.doLabel(init_Matrix);

        String[][] resultArray = new String[1024][1024];

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

        MatrixUtil.matrix2ImageWithColor(resultArray, path + "04_" + mCode + ".jpg", Color.CYAN);
        //String[][] finalArray;
        while(true){
            resultArray = imageAlgorithm.batchDeal(resultArray);
            int x_num = 0;
            for(String[] array : resultArray){
                for(String str: array){
                    if(str.equals("x")){
                        x_num ++;
                    }
                }
            }
            System.out.println("x_num:"+x_num);
            if(x_num == 0)
                break;
        }
        imageAlgorithm.arrayToFile(resultArray, path + "04_" + mCode + ".txt");
        Map<String, java.util.List<int[]>> region = imageAlgorithm
                .matrixToRegion(resultArray);
        System.out.println("region.size():"+region.size());
        Iterator iterator = region.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String new_group_code = (String) entry.getKey(); //重新找连通性后的编码
            List<int[]> xyList = (List<int[]>) entry.getValue();
            if (xyList != null && xyList.size() > 0) {
                int[] start = xyList.get(0);
                int group_code = inputMatrix[start[0]][start[1]];
                if(group_code == 96 ){
//                    System.out.println(new_group_code);
//                    System.out.println(xyList.size());
                }

                double confidenceValue = confidenceMap.get(group_code);

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
                    if(group_code == 96){
                        String key = mCode + "|" + group_code + "|" + new_group_code + "|" + confidenceValue;
                        System.out.println(key + ".size:"+xyList.size());
                        System.out.println(xyList.size());
                    //    System.out.println(json.toJSONString());
                    }
                }
            }

        }
    }




    public static void main(String[] args) throws Exception {
        String key = StringUtil.reverse(args[0]);
        TraceDeal deal = new TraceDeal();

        Integer[][] matrix = deal.readDetect(key);
        deal.trace(matrix, args[0]);
    }
}
