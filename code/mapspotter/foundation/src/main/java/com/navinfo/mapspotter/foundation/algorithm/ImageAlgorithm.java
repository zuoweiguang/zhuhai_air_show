package com.navinfo.mapspotter.foundation.algorithm;

import com.navinfo.mapspotter.foundation.util.DateTimeUtil;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by cuiliang on 2016/1/5.
 * 图像处理相关的算法
 */
public class ImageAlgorithm {

    public int default_size = 1024;
    public int size = 0;

    public ImageAlgorithm() {
        this.size = this.default_size;
    }

    public ImageAlgorithm(int size) {
        this.size = size;
    }


    /**
     * 计算矩阵有值数据的平均值
     *
     * @param matrix
     * @return
     */
    public double martixAverage(Integer[][] matrix) {
        double amount = 0;
        double count = 0;
        for (Integer[] array : matrix) {
            for (Integer value : array) {
                if (value != null && value != 0) {
                    amount += value;
                    count++;
                }
            }
        }
        if (count == 0){
            return 0;
        }
        return amount/count;
    }

    /**
     * 后续分析参数1，每一个连通区域的均值 / 瓦片均值
     *
     * @param region
     * @param matrix
     * @param tile_avg
     * @return
     */
    public Map<String, Double> analysisArg1(Map<String, List<Integer[]>> region, Integer[][] matrix, double tile_avg) {
        Map<String, Double> retMap = new HashMap();
        Iterator iter = region.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();
            double amount = 0;
            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    amount += matrix[x][y];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            double region_avg = amount / list.size();
            double retDouble = region_avg / tile_avg;
            if (retDouble > 1) {
                retDouble = 1;
            }
            retMap.put(key, retDouble);
        }
        return retMap;
    }

    /**
     * 将连通区域按照正方形膨胀
     *
     * @param regions
     * @param length  正方形边长
     * @return
     */
    public Map<String, List<Integer[]>> regionSquareExpansion(Map<String, List<Integer[]>> regions, int length) {
        int size = 1024;
        int radius = (length - 1) / 2;
        List<int[]> expSquare = new ArrayList();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int num[] = {i, j};
                expSquare.add(num);
            }
        }
        Map<String, List<Integer[]>> retMap = new HashMap();
        Iterator iter = regions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> region = (List<Integer[]>) entry.getValue();
            Map<String, Integer[]> map = new HashMap<>();
            for (Integer[] region_xy : region) {
                int region_x = region_xy[0];
                int region_y = region_xy[1];

                for (int[] line_xy : expSquare) {
                    int line_x = line_xy[0];
                    int line_y = line_xy[1];
                    int exp_x = region_x + line_x;
                    int exp_y = region_y + line_y;

                    if (exp_x >= 0 && exp_x < size && exp_y >= 0
                            && exp_y < size) {
                        Integer[] exp_xy = {exp_x, exp_y};
                        map.put(exp_x + "_" + exp_y, exp_xy);
                    }

                }
            }
            Iterator<Map.Entry<String, Integer[]>> iter2 = map.entrySet().iterator();
            List<Integer[]> retList = new ArrayList();
            while (iter2.hasNext()) {
                Map.Entry entry2 = iter2.next();
                Integer[] xy = (Integer[]) entry2.getValue();
                retList.add(xy);
            }
            retMap.put(key, retList);
        }
        return retMap;
    }


    /**
     * 后续分析参数3，每一连通区域正方形膨胀后的覆盖的路网格子数 / 原有连通区域格子数 + 每一连通区域正方形膨胀后的覆盖的路网格子数
     *
     * @param expRegion
     * @param matrix
     * @param region
     * @return
     */
    public Map<String, Double> analysisArg3(Map<String, List<Integer[]>> expRegion, Integer[][] matrix, Map<String, List<Integer[]>> region) {
        Map<String, Double> retMap = new HashMap();
        Iterator iter = expRegion.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();
            double road_count = 0;
            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    Integer value = matrix[x][y];
                    if (value != null && value != 0) {
                        road_count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            double region_count = region.get(key).size();
            retMap.put(key, road_count / (region_count + road_count));
        }
        return retMap;
    }

    /**
     * 判断是否与隧道数据相交，如果相交结果返回0.1，否则为1
     *
     * @param regions
     * @param matrix
     * @return
     */
    public Map<String, Double> analysisArg4(Map<String, List<Integer[]>> regions, Integer[][] matrix) {
        Map<String, Double> retMap = new HashMap();
        Iterator iter = regions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();
            double retDouble = 1;
            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    Integer value = matrix[x][y];
                    if (value != null && value != 0) {
                        retDouble = 0.1;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            retMap.put(key, retDouble);
        }
        return retMap;
    }


    /**
     * 判断连通区域是否与某个矩阵有交有则过滤掉该区域
     *
     * @param regions
     * @param matrix
     * @return
     */
    public Map<String, List<Integer[]>> filterMatrix(Map<String, List<Integer[]>> regions, Integer[][] matrix) {
        Iterator iterator = regions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();
            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    Integer value = matrix[x][y];
                    if (value != null && value != 0) {
                        iterator.remove();
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return regions;
    }



    /**
     * 过滤小于指定参数的矩阵元素
     *
     * @param matrix
     * @param num
     * @return
     */
    public Integer[][] filterLessThanPara(Integer[][] matrix, double num) {
        Integer[][] returnMatrix = new Integer[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            Integer[] didiA = matrix[i];
            for (int j = 0; j < didiA.length; j++) {
                Integer didiValue = didiA[j];
                if (didiValue != null) {
                    if (didiValue >= num)
                        returnMatrix[i][j] = didiValue;
                }
            }
        }
        return returnMatrix;
    }

    public Integer[][] filterStepOne(Integer[][] input, int filter) {

        List<Integer> sortList = new ArrayList<Integer>();

        Integer[][] returnArray = new Integer[input.length][input[0].length];

        Integer[][] didiArray = input;
        for (Integer[] didiA : didiArray) {
            for (Integer didiValue : didiA) {
                if (didiValue != null) {
                    sortList.add(didiValue);
                } else {
                    sortList.add(0);
                }
            }
        }
        Collections.sort(sortList);

        int startIndex = Math.round(sortList.size()
                * 0.9f);
        int endIndex = Math.round(sortList.size()
                * 0.99f);

        for (int i = 0; i < didiArray.length; i++) {
            Integer[] didiA = didiArray[i];
            for (int j = 0; j < didiA.length; j++) {
                Integer didiValue = didiA[j];
                if (didiValue != null) {
                    int lowThreshold;
                    int a = sortList.get(startIndex);
                    int b = filter;
                    lowThreshold = a > b ? a : b;
                    if (didiValue >= lowThreshold
                            && didiValue <= sortList.get(endIndex))
                        returnArray[i][j] = didiValue;
                }
            }
        }

        return returnArray;
    }


    public Integer[][] filterByPercent(Integer[][] input, double per) {
        Integer[][] returnArray = new Integer[input.length][input[0].length];
        double amount = 0;
        double num = 0;
        for (Integer[] inputArray : input) {
            for (Integer value : inputArray) {
                if (value != null && value > 0) {
                    amount += value;
                    num++;
                }
            }
        }
        double avg = amount / num;
        System.out.println(avg);
        double lowThreshold = avg * per;
        System.out.println(lowThreshold);
        for (int i = 0; i < input.length; i++) {
            Integer[] inputArray = input[i];
            for (int j = 0; j < inputArray.length; j++) {
                Integer value = inputArray[j];

                if (value != null && value > lowThreshold) {
                    returnArray[i][j] = value;
                }
            }
        }
        return returnArray;
    }


    /**
     * 过滤小于指定参数的矩阵元素
     *
     * @param matrix
     * @param num
     * @return
     */
    public Integer[][] filterLessThanPara(int[][] matrix, int num) {
        Integer[][] returnMatrix = new Integer[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            int[] didiA = matrix[i];
            for (int j = 0; j < didiA.length; j++) {
                Integer didiValue = didiA[j];
                if (didiValue != null) {
                    if (didiValue >= num)
                        returnMatrix[i][j] = didiValue;
                }
            }
        }
        return returnMatrix;
    }

    /**
     * 矩阵相减二值化
     *
     * @param source
     * @param road
     * @return
     */
    public Integer[][] matrixMinus(Integer[][] source, Integer[][] road) {
        Integer[][] result = new Integer[source.length][source.length];
        for (int i = 0; i < source.length; i++) {
            Integer[] trackArray = source[i];
            for (int j = 0; j < trackArray.length; j++) {
                if ((source[i][j] != null && source[i][j] != 0) && (road[i][j] == null || road[i][j] == 0))
                    result[i][j] = 1;
                else
                    result[i][j] = null;
            }
        }
        return result;
    }

    /**
     * 矩阵相减二值化
     *
     * @param source
     * @param road
     * @return
     */
    public Integer[][] matrixMinus(Integer[][] source, int[][] road) {
        Integer[][] result = new Integer[source.length][source.length];
        for (int i = 0; i < source.length; i++) {
            Integer[] trackArray = source[i];
            for (int j = 0; j < trackArray.length; j++) {
                if ((source[i][j] != null && source[i][j] != 0) && road[i][j] == 0)
                    result[i][j] = 1;
                else
                    result[i][j] = null;
            }
        }
        return result;
    }

    /**
     * 矩阵相减二值化
     *
     * @param source
     * @param road
     * @return
     */
    public Integer[][] matrixMinus(Integer[][] source, Byte[][] road) {
        Integer[][] result = new Integer[source.length][source.length];
        for (int i = 0; i < source.length; i++) {
            Integer[] trackArray = source[i];
            for (int j = 0; j < trackArray.length; j++) {
                if ((source[i][j] != null && source[i][j] != 0) && (road[i][j] == null || road[i][j] == 0))
                    result[i][j] = 1;
                else
                    result[i][j] = null;
            }
        }
        return result;
    }


    public double UNCIA = 1d / 12d;

    /**
     * 中值滤波
     *
     * @param inputMatrix 传入待滤波矩阵
     * @param size        滤波框大小
     * @return 滤波后矩阵
     */
    public Integer[][] medianFilter(Integer[][] inputMatrix, int size) {
        Integer[][] outputMatrix = new Integer[inputMatrix.length][inputMatrix[0].length];
        for (int i = 0; i < inputMatrix.length; i++) {
            Integer[] input = inputMatrix[i];
            for (int j = 0; j < input.length; j++) {

                int[][] matrix = new int[size][size];
                int dis = (size - 1) / 2;

                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
                        matrix[m][n] = getMatrixValue(inputMatrix, i + m - dis,
                                j + n - dis);
                    }
                }
                int median = getMedian(matrix, size);
                if (median == 0)
                    outputMatrix[i][j] = null;
                else
                    outputMatrix[i][j] = median;
            }
        }
        return outputMatrix;
    }


    public int getMatrixValue(Integer[][] inputMatrix, int x, int y) {
        if (x < 0 || x >= inputMatrix.length)
            return 0;
        if (y < 0 || y >= inputMatrix[0].length)
            return 0;
        return inputMatrix[x][y] == null ? 0 : inputMatrix[x][y];
    }

    public int getMatrixValue(int[][] inputMatrix, int x, int y) {
        if (x < 0 || x >= inputMatrix.length)
            return 0;
        if (y < 0 || y >= inputMatrix[0].length)
            return 0;
        return inputMatrix[x][y];
    }

    public String getMatrixValue(String[][] inputMatrix, int x, int y) {
        if (x < 0 || x >= inputMatrix.length)
            return "0";
        if (y < 0 || y >= inputMatrix[0].length)
            return "0";
        return inputMatrix[x][y] == null ? "0" : inputMatrix[x][y];
    }

    /**
     * 获取矩阵中间值
     *
     * @param matrix
     * @param size
     * @return
     */
    private int getMedian(int[][] matrix, int size) {
        int[] sortMatrix = new int[size * size];
        int k = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] t_matrix = matrix[i];
            for (int j = 0; j < t_matrix.length; j++) {
                sortMatrix[k] = matrix[i][j];
                k++;
            }
        }
        Arrays.sort(sortMatrix);
        return sortMatrix[(size * size - 1) / 2];
    }


    /**
     * 找连通区域
     * 000001100000000000000000           000001100000000000000000
     * 000011000000000000000000           000011000000000000000000
     * 001100000001100000000000           001100000002200000000000
     * 001100000001100000000000           001100000002200000000000
     * 000110000011000001100000           000110000022000003300000
     * 000000000110000000110000   ----->  000000000220000000330000
     * 000000001100000000011000           000000002200000000033000
     * 000000011000000000110000           000000022000000000330000
     * 000000000000000000011000           000000000000000000033000
     * 000000000000000000001100           000000000000000000003300
     *
     * @param track 轨迹矩阵(有轨迹为1，没有为0)
     * @return 轨迹矩阵（同一连通区域的赋同一数字）
     */
    public Integer[][] floodFill(Integer[][] track) {
        track = initFloodFill(track);
        int value = 0;
        for (int i = 0; i < track.length; i++) {
            Integer[] trackArray = track[i];
            for (int j = 0; j < trackArray.length; j++) {
                if (track[i][j] != null) {
                    if (track[i][j] == -1) {
                        value++;
                        floodFillCell(i, j, value, track);
                    }
                }
            }
        }
        return track;
    }

    /**
     * 矩阵连通性初始化
     *
     * @param track 矩阵
     * @return 将矩阵中非空值转为 -1
     */
    public Integer[][] initFloodFill(Integer[][] track) {
        Integer[][] newArray = new Integer[track.length][track.length];
        for (int i = 0; i < track.length; i++) {
            Integer[] trackArray = track[i];
            for (int j = 0; j < trackArray.length; j++) {
                if (track[i][j] != null && track[i][j] != 0) {
                    newArray[i][j] = -1;
                }
            }
        }
        return newArray;
    }

    /**
     * 矩阵连通性初始化
     *
     * @param track 矩阵
     * @return 将矩阵中非空值转为 -1
     */
    public Integer[][] initFloodFill2(Integer[][] track) {
        Integer[][] newArray = new Integer[track.length][track.length];
        for (int i = 0; i < track.length; i++) {
            Integer[] trackArray = track[i];
            for (int j = 0; j < trackArray.length; j++) {
                if (track[i][j] != null && track[i][j] > 0) {
                    newArray[i][j] = -1;
                }
            }
        }
        return newArray;
    }

    /**
     * 用于处理分叉数据
     */
    public String[][] batchDeal(String[][] resultArray) {

        String[][] finalArray = copyMatrix(resultArray);

        int size = 3;
        int dis = (size - 1) / 2;
        for (int i = 0; i < resultArray.length; i++) {
            for (int j = 0; j < resultArray.length; j++) {

                String[][] scan = new String[size][size];
                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
                        scan[m][n] = getMatrixValue(resultArray, i + m - dis, j + n - dis);
                    }
                }
                if ("x".equals(scan[1][1])) {
                    String str = "";
                    for (int x = 0; x < scan.length; x++) {
                        for (int y = 0; y < scan.length; y++) {
                            if (!"x".equals(scan[x][y]) && !"0".equals(scan[x][y]) && !"".equals(scan[x][y])) {
                                if (str.indexOf(scan[x][y]) < 0)
                                    str = str + scan[x][y] + "|";
                            }
                        }
                    }
                    if ("".equals(str)) {
                        finalArray[i][j] = "x";
                    } else {
                        finalArray[i][j] = str.substring(0, str.length() - 1);
                    }

                }
            }
        }

//        int x_num = 0;
//        for(String[] array : finalArray){
//            for(String str: array){
//                if(str.equals("x")){
//                    x_num ++;
//                }
//            }
//        }
        /**
         * 递归调用如果还存在字符"x"的数据则继续执行，知道矩阵中"x"数为0
         */
//        if(x_num > 0){
//            return batchDeal(finalArray);
//        }
//        else{
//            return finalArray;
//        }
        return finalArray;
    }

    /**
     * 二维矩阵8个方向找连通性
     *
     * @param x     矩阵x坐标
     * @param y     矩阵y坐标
     * @param value 连通分组值
     * @param track 矩阵
     */
    private void floodFillCell(int x, int y, int value, Integer[][] track) {
        if (x >= 0 && x < track.length && y >= 0 && y < track.length
                && track[x][y] != null && track[x][y] != value) {
            track[x][y] = value;
            floodFillCell(x + 1, y, value, track);
            floodFillCell(x - 1, y, value, track);
            floodFillCell(x, y + 1, value, track);
            floodFillCell(x, y - 1, value, track);
            floodFillCell(x + 1, y + 1, value, track);
            floodFillCell(x - 1, y - 1, value, track);
            floodFillCell(x - 1, y + 1, value, track);
            floodFillCell(x + 1, y - 1, value, track);
        }

    }

    /**
     * 获取矩阵的连通区域
     *
     * @param matrix    矩阵
     * @param threshold 阀值（只取像素数大于该阀值的连通区域）
     * @return 按照连通区域转成Map<String, List<Integer[]>>
     */
    public Map<String, List<Integer[]>> matrixToRegion(Integer[][] matrix,
                                                       int threshold) {
        Map<String, List<Integer[]>> region = new HashMap<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != null && matrix[i][j] != 0) {
                    List<Integer[]> xyList = region.get(String
                            .valueOf(matrix[i][j]));
                    if (xyList == null) {
                        xyList = new ArrayList<>();
                    }
                    Integer[] xy = {i, j};
                    xyList.add(xy);
                    region.put(String.valueOf(matrix[i][j]), xyList);
                }
            }
        }
        Map<String, List<Integer[]>> returnRegion = new HashMap<>();
        Iterator iter = region.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> xyList = (List<Integer[]>) entry.getValue();
            if (xyList.size() >= threshold) {
                returnRegion.put(key, xyList);
            }
        }
        return returnRegion;
    }

    /**
     * 分叉数据的matrix转region
     *
     * @param matrix
     * @return
     */
    public Map<String, List<int[]>> matrixToRegion(String[][] matrix) {
        Map<String, List<int[]>> region = new HashMap<String, List<int[]>>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (!matrix[i][j].equals("0")) {
                    List<int[]> xyList = null;
                    if (matrix[i][j].indexOf("|") < 0) {
                        xyList = region.get(matrix[i][j]);
                        if (xyList == null) {
                            xyList = new ArrayList<int[]>();
                        }
                        int[] xy = {i, j};
                        xyList.add(xy);
                        region.put(String.valueOf(matrix[i][j]), xyList);
                    } else {
                        String[] groups = matrix[i][j].split("\\|");
                        for (String group : groups) {
                            xyList = region.get(group);
                            if (xyList == null) {
                                xyList = new ArrayList();
                            }
                            int[] xy = {i, j};
                            xyList.add(xy);
                            region.put(group, xyList);
                        }
                    }
                }
            }
        }

        Map<String, List<int[]>> returnRegion = new HashMap<String, List<int[]>>();

        Iterator iter = region.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<int[]> xyList = (List<int[]>) entry.getValue();
            returnRegion.put(key, xyList);
        }

        return returnRegion;
    }

    /**
     * 矩阵点根据距离排序
     *
     * @param input
     * @return
     */
    public List<int[]> sortNode(List<int[]> input) {
        int[][] matrix = new int[1024][1024];
        Map<String, Double> hashMap = new HashMap();
        for (int[] node : input) {
            matrix[node[0]][node[1]] = 1;
            hashMap.put(node[0] + "," + node[1], 0d);
        }
        List<int[]> sortList = new ArrayList();

        int size = 3;
        int dis = (size - 1) / 2;
        for (int i = 0; i < matrix.length; i++) {
            boolean breakFlag = false;
            for (int j = 0; j < matrix[i].length; j++) {
                int[][] scan = new int[size][size];
                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
                        scan[m][n] = getMatrixValue(matrix, i + m - dis, j + n - dis);
                    }
                }
                int sum = 0;
                for (int x = 0; x < scan.length; x++) {
                    for (int y = 0; y < scan[x].length; y++) {
                        sum = sum + scan[x][y];
                    }
                }
                if (scan[1][1] == 1 && sum <= 2) {
                    int[] coord = {i, j};
                    sortList.add(coord);
                    hashMap.remove((i) + "," + (j));
                    matrix[i][j] = 0;
                    breakFlag = true;
                    break;
                }
            }
            if (breakFlag) {
                break;
            }
        }
        while (true) {
            if (sortList.size() == 0) {
                break;
            }

            int[] coord = sortList.get(sortList.size() - 1);
            int s_x = coord[0];
            int s_y = coord[1];

            Iterator iter = hashMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                int x = Integer.parseInt(key.split(",")[0]);
                int y = Integer.parseInt(key.split(",")[1]);
                double distance = Math.sqrt(Math.pow((s_x - x), 2) + Math.pow((s_y - y), 2));
                hashMap.put(key, distance);
            }
            double minValue = Double.MAX_VALUE;
            Iterator iter2 = hashMap.entrySet().iterator();
            while (iter2.hasNext()) {
                Map.Entry entry2 = (Map.Entry) iter2.next();
                double value = (Double) entry2.getValue();
                if (value < minValue) {
                    minValue = value;
                }
            }


            Iterator iter3 = hashMap.entrySet().iterator();
            while (iter3.hasNext()) {
                Map.Entry entry3 = (Map.Entry) iter3.next();
                String key = (String) entry3.getKey();
                double value = (Double) entry3.getValue();
                if (value == minValue) {
                    minValue = value;
                    int x = Integer.parseInt(key.split(",")[0]);
                    int y = Integer.parseInt(key.split(",")[1]);
                    int[] nodeCoord = {x, y};
                    sortList.add(nodeCoord);
                    hashMap.remove(key);
                    break;
                }
            }


            if (hashMap.isEmpty()) {
                break;
            }
        }
        return sortList;
    }

    /**
     * 矩阵点根据距离排序
     *
     * @param input
     * @return
     */
    public List<int[]> sortNodes(List<int[]> input) {
        int[][] matrix = new int[1024][1024];
        Map<String, Double> hashMap = new HashMap();
        for (int[] node : input) {
            matrix[node[0]][node[1]] = 1;
            hashMap.put(node[0] + "," + node[1], 0d);
        }
        List<int[]> sortList = new ArrayList();

        int size = 3;
        int dis = (size - 1) / 2;
        for (int i = 0; i < matrix.length; i++) {
            boolean breakFlag = false;
            for (int j = 0; j < matrix[i].length; j++) {
                int[][] scan = new int[size][size];
                for (int m = 0; m < size; m++) {
                    for (int n = 0; n < size; n++) {
                        scan[m][n] = getMatrixValue(matrix, i + m - dis, j + n - dis);
                    }
                }
                int sum = 0;
                for (int x = 0; x < scan.length; x++) {
                    for (int y = 0; y < scan[x].length; y++) {
                        sum = sum + scan[x][y];
                    }
                }
                if (scan[1][1] == 1 && sum == 2) {
                    sort(matrix, i, j, sortList);
                    breakFlag = true;
                    break;
                }
            }
            if (breakFlag) {
                break;
            }
        }
        return sortList;
    }


    public void sort(int[][] matrix, int i, int j, List<int[]> sortList) {
        int size = 3;
        int dis = (size - 1) / 2;
        int[][] scan = new int[size][size];
        int[] coordinate = {i, j};
        sortList.add(coordinate);
        matrix[i][j] = 0;
        scan[0][0] = matrix[i - 1][j - 1];
        scan[0][1] = matrix[i - 1][j];
        scan[0][2] = matrix[i - 1][j + 1];
        scan[1][0] = matrix[i][j - 1];
        scan[1][1] = matrix[i][j];
        scan[1][2] = matrix[i][j + 1];
        scan[2][0] = matrix[i + 1][j - 1];
        scan[2][1] = matrix[i + 1][j];
        scan[2][2] = matrix[i + 1][j + 1];

        for (int x = 0; x < scan.length; x++) {
            for (int y = 0; y < scan[x].length; y++) {
                if (scan[x][y] == 1) {
                    sort(matrix, i + x - dis, j + y - dis, sortList);
                }
            }
        }

    }

    /**
     * 计算连通区域的离心率和方向
     *
     * @param region      区域
     * @param isJudgeline
     * @param threshold
     * @return
     */
    public Map<String, Map<String, Object>> CalEccentricityAndOrientation(Map<String, List<Integer[]>> region, boolean isJudgeline, double threshold) {
        Iterator iter = region.entrySet().iterator();
        Map<String, Map<String, Object>> map = new HashMap<>();

        Map<String, Object> xyList_map = new HashMap<>();
        Map<String, Object> eccentricity_map = new HashMap<>();
        Map<String, Object> orientation_map = new HashMap<>();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> xyList = (List<Integer[]>) entry.getValue();

            int[] x = new int[xyList.size()];
            int[] y = new int[xyList.size()];
            for (int i = 0; i < xyList.size(); i++) {
                x[i] = xyList.get(i)[0];
                y[i] = xyList.get(i)[1];
            }

            double x_avg = average(x);
            double y_avg = average(y);

            double[] new_x = newArray(x, x_avg, 1);
            double[] new_y = newArray(y, y_avg, -1);

            double mu_xx = muCalculation(new_x, new_x, UNCIA);
            double mu_yy = muCalculation(new_y, new_y, UNCIA);
            double mu_xy = muCalculation(new_x, new_y, 0);

            double common = Math.sqrt(Math.pow((mu_xx - mu_yy), 2) + 4
                    * Math.pow(mu_xy, 2));

            double majorAxislength = 2 * Math.sqrt(2)
                    * Math.sqrt(mu_xx + mu_yy + common);
            double minorAxislength = 2 * Math.sqrt(2)
                    * Math.sqrt(mu_xx + mu_yy - common);

            double eccentricity = 2 * Math.sqrt(Math.pow((majorAxislength / 2),
                    2) - Math.pow((minorAxislength / 2), 2)) / majorAxislength;
            double orientation;

            double num;
            double den;

            if (mu_yy > mu_xx) {
                num = mu_yy - mu_xx + common;
                den = 2 * mu_xy;
            } else {
                num = 2 * mu_xy;
                den = mu_xx - mu_yy + common;
            }

            if (num == 0 && den == 0) {
                orientation = 0;
            } else {
                orientation = (180 / Math.PI) * Math.atan(num / den);
            }

            if (isJudgeline) {
                if (eccentricity > threshold) {
                    xyList_map.put(key, xyList);
                    eccentricity_map.put(key, eccentricity);
                    orientation_map.put(key, orientation);
                }
            } else {
                xyList_map.put(key, xyList);
                eccentricity_map.put(key, eccentricity);
                orientation_map.put(key, orientation);
            }

        }
        map.put("xyList", xyList_map);
        map.put("eccentricity", eccentricity_map);
        map.put("orientation", orientation_map);

        return map;
    }

    /**
     * 计算数组元素平均数
     *
     * @param array 数组
     * @return 平均数
     */
    private double average(int[] array) {
        double amount = 0d;
        for (int number : array) {
            amount = amount + number;
        }
        return amount / array.length;
    }

    /**
     * 将数组中每一个元素减去数组内平均值
     * x = x - avg(sum(x))
     * y = -(y - avg(sum(y)))
     *
     * @param array
     * @param avg
     * @param positive
     * @return
     */
    private double[] newArray(int[] array, double avg, int positive) {
        double[] newarray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            newarray[i] = (array[i] - avg) * positive;
        }
        return newarray;
    }

    /**
     * 计算μ
     *
     * @param arrayX
     * @param arrayY
     * @param add
     * @return μ
     */
    private double muCalculation(double[] arrayX, double[] arrayY,
                                 double add) {
        double amount = 0d;
        for (int i = 0; i < arrayX.length; i++) {
            amount = amount + arrayX[i] * arrayY[i];
        }
        return amount / arrayX.length + add;
    }

    public Map<String, List<int[]>> CalculationLineStrel(int len, Map<String, Object> oriMap) {

        Map<String, List<int[]>> resultMap = new HashMap<String, List<int[]>>();

        Iterator iter = oriMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            double orientation = (double) entry.getValue();

            List<int[]> list = makeLineStrel(len, orientation);
            resultMap.put(key, list);
        }
        return resultMap;
    }

    private List<int[]> makeLineStrel(int len, double angdeg) {
        if (len > 0) {
            double angle = Math.toRadians(angdeg);
            int x = round((double) (len - 1) / 2 * Math.cos(angle));
            int y = -round((double) (len - 1) / 2 * Math.sin(angle));
            return getIntLine(-x, -y, x, y);
        }
        return new ArrayList();
    }

    /**
     * 通过坐标点计算经过的矩阵坐标
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    private List<int[]> getIntLine(int x1, int y1, int x2, int y2) {
        List<int[]> list = new ArrayList<>();
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        double k = (y2 - y1) / (double) (x2 - x1);
        if (k > 0) {
            if (x1 > x2 && y1 > y2) {
                // 交换x
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
                // 交换y
                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            int[] p = new int[2];
            p[0] = x1;
            p[1] = y1;
            list.add(p);
            if (dx > dy) {
                for (int i = 1; i < dx; i++) {
                    p = new int[2];
                    p[0] = x1 + i;
                    p[1] = round(y1 + k * i);
                    list.add(p);
                }
            } else {
                k = (double) (x2 - x1) / (y2 - y1);
                for (int i = 1; i < dy; i++) {
                    p = new int[2];
                    p[0] = round(x1 + k * i);
                    p[1] = y1 + i;
                    list.add(p);
                }
            }
            p = new int[2];
            p[0] = x2;
            p[1] = y2;
            list.add(p);
        } else {
            if (x2 < x1 && y2 > y1) {
                // 交换x
                int tmp = x1;
                x1 = x2;
                x2 = tmp;
                // 交换y
                tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            int[] p = new int[2];
            p[0] = x1;
            p[1] = y1;
            list.add(p);

            if (dx > dy) {
                for (int i = 1; i < dx; i++) {
                    p = new int[2];
                    p[0] = x1 + i;
                    p[1] = round(y1 + k * i);
                    list.add(p);
                }
            } else {
                k = (double) (x2 - x1) / (y2 - y1);
                for (int i = 1; i < dy; i++) {
                    p = new int[2];
                    p[0] = round(x1 - k * i);
                    p[1] = y1 - i;
                    list.add(p);
                }
            }
            p = new int[2];
            p[0] = x2;
            p[1] = y2;
            list.add(p);
        }

        return list;

    }

    private int round(double r) {
        if (r < 0) {
            return -(int) Math.round(Math.abs(r));
        }
        return (int) Math.round(r);
    }


    /**
     * 膨胀
     *
     * @param regions
     * @param expline
     * @return
     */
    public Integer[][] expansion(Map<String, List<Integer[]>> regions,
                                 Map<String, List<int[]>> expline) {

        List<int[]> list = new ArrayList<>();
        Map<String, int[]> map = new HashMap<>();

        Iterator iter = expline.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();

            List<Integer[]> region = regions.get(key);
            List<int[]> line = expline.get(key);
            try {
                for (Integer[] region_xy : region) {
                    int region_x = region_xy[0];
                    int region_y = region_xy[1];

                    for (int[] line_xy : line) {
                        int line_x = line_xy[0];
                        int line_y = line_xy[1];

                        int exp_x = region_x + line_x;
                        int exp_y = region_y + line_y;

                        if (exp_x >= 0 && exp_x < size && exp_y >= 0
                                && exp_y < size) {
                            int[] exp_xy = {exp_x, exp_y};
                            map.put(exp_x + "_" + exp_y, exp_xy);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Iterator iter2 = map.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry entry2 = (Map.Entry) iter2.next();
            int[] xy = (int[]) entry2.getValue();
            list.add(xy);
        }
        return listToMatrix(list);
    }

    public Integer[][] listToMatrix(List<int[]> list) {
        Integer[][] expMatrix = new Integer[size][size];
        for (int[] exp_xy : list) {
            int x = exp_xy[0];
            int y = exp_xy[1];
            try {
                expMatrix[x][y] = 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return expMatrix;
    }

    public Integer[][] regionToMatrix(Map<String, Object> region) {
        Integer[][] matrix = new Integer[size][size];
        Iterator iter = region.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            List<Integer[]> xyList = (List<Integer[]>) entry.getValue();
            for (Integer[] array : xyList) {
                matrix[array[0]][array[1]] = 1;
            }
        }
        return matrix;
    }

    public Integer[][] matrixIntersection(Integer[][] matrix1, Integer[][] matrix2) {
        Integer[][] result = new Integer[matrix1.length][matrix1.length];
        for (int i = 0; i < matrix1.length; i++) {
            Integer[] trackArray = matrix1[i];
            for (int j = 0; j < trackArray.length; j++) {
                if ((matrix1[i][j] != null && matrix1[i][j] != 0) && (matrix2[i][j] != null && matrix2[i][j] != 0)) {
                    result[i][j] = 1;
                } else {
                    result[i][j] = null;
                }
            }
        }
        return result;
    }

    /**
     * 连通区域Map转矩阵
     *
     * @param map
     * @return
     */
    public Integer[][] mapToMatrix(Map<String, List<Integer[]>> map) {
        Integer[][] expMatrix = new Integer[size][size];

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();

            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    expMatrix[x][y] = 1;
                } catch (Exception e) {
                    System.out.println(x);
                    System.out.println(y);
                    e.printStackTrace();
                }
            }
        }
        return expMatrix;
    }

    /**
     * 连通区域Map转矩阵
     *
     * @param map
     * @return
     */
    public Integer[][] mapToMatrixWithLabelNumber(Map<String, List<Integer[]>> map) {
        Integer[][] expMatrix = new Integer[size][size];

        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            List<Integer[]> list = (List<Integer[]>) entry.getValue();
            for (Integer[] exp_xy : list) {
                int x = exp_xy[0];
                int y = exp_xy[1];
                try {
                    expMatrix[x][y] = Integer.parseInt(key);
                } catch (Exception e) {
                    System.out.println(x);
                    System.out.println(y);
                    e.printStackTrace();
                }
            }
        }
        return expMatrix;
    }

    /**
     * 抽骨骼算法
     *
     * @param matrix 传入矩阵
     * @return 抽骨骼后矩阵
     */
    public Integer[][] skeletonize(Integer[][] matrix) {
        ByteProcessor bp = new ByteProcessor(matrix.length, matrix.length);
        BinaryProcessor ibp = new BinaryProcessor(bp);
        ibp.setColor(Color.white);
        ibp.fill();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (matrix[x][y] != null)
                    ibp.putPixel(x, y, 0);
            }
        }

        ibp.skeletonize();

        Integer[][] skeletonMatrix = new Integer[matrix.length][matrix.length];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (ibp.get(x, y) == 0) {
                    skeletonMatrix[x][y] = 1;
                }
            }
        }
        boolean isEmpty = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (skeletonMatrix[i][j] != null
                        && skeletonMatrix[i][j] != 0) {
                    isEmpty = false;
                }
            }
        }
        if (!isEmpty) {
            return skeletonMatrix;
        } else {
            return null;
        }

    }

    public void arrayToFile(String[][] array, String path) throws Exception {
        File outFile = new File(path);
        BufferedWriter bw;

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        String[][] byteToArray = array;
        for (int i = 0; i < 1024; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < 1024; j++) {
                String str = byteToArray[i][j];
                if (str != null && !str.equals("0")) {
                    line.append(str + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bw.write((line).toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();

    }

    public void arrayToFile(Integer[][] array, String path) throws Exception {
        File outFile = new File(path);
        BufferedWriter bw;

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        Integer[][] byteToArray = array;
        for (int i = 0; i < 1024; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < 1024; j++) {
                Integer integer = byteToArray[i][j];
                if (integer != null && integer > 0) {
                    line.append(integer + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bw.write((line).toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();

    }

    public void arrayToFile(int[][] array, String path) throws Exception {
        int rowcount = array.length;
        if (rowcount <= 0)
            return;

        int colcount = array[0].length;

        File outFile = new File(path);
        BufferedWriter bw;

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        int[][] byteToArray = array;

        for (int i = 0; i < rowcount; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < colcount; j++) {
                int integer = byteToArray[i][j];
                if (integer > 0) {
                    line.append(integer + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bw.write((line).toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();

    }

    /**
     * 二维数组转文件
     */
    public void arrayToFile(Byte[][] array, String path) throws Exception {
        File outFile = new File(path);
        BufferedWriter bw;

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));

        Byte[][] byteToArray = array;
        for (int i = 0; i < 1024; i++) {
            StringBuilder line = new StringBuilder();
            for (int j = 0; j < 1024; j++) {
                Byte integer = byteToArray[i][j];
                if (integer != null && integer > 0) {
                    line.append(integer + "\t");
                } else {
                    line.append(0 + "\t");
                }
            }
            bw.write((line).toString());
            bw.newLine();
        }
        bw.flush();
        bw.close();

    }

    public void byteToFile(byte[] columnValue, String path) throws Exception {
        File outFile = new File(path);
        BufferedWriter bw;

        bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile)));
        bw.write(new String(columnValue, "utf-8"));
        bw.flush();
        bw.close();

    }


    /**
     * 文件转二维数组
     */
    public Integer[][] fileToArray(String path) {
        Integer[][] rArray = new Integer[1024][1024];

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    path)));
            int j = 0;
            for (String line = br.readLine(); line != null; line = br
                    .readLine()) {
                String[] lineArray = line.split("\t");
                int i = 0;
                for (String node : lineArray) {
                    int a = Integer.parseInt(node);
                    if (a > 0) {
                        rArray[i][j] = a;
                    }
                    i++;
                }
                j++;
            }
            br.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rArray;
    }

    public Integer[][] filterTunnelMatrix(Integer[][] matrix, Map<Integer, String> tunnelMap) {

        Integer[][] filterMatrix = new Integer[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            Integer array[] = matrix[i];
            for (int j = 0; j < array.length; j++) {
                Integer number = matrix[i][j];
                if (number != null && number > 0) {
                    if (tunnelMap.get(number) == null) {
                        filterMatrix[i][j] = number;
                    } else {
                        filterMatrix[i][j] = 0;
                    }
                } else {
                    filterMatrix[i][j] = 0;
                }
            }
        }


        return filterMatrix;
    }


    public Integer[][] binarizationMatrix(Integer[][] inputMatrix) {
        Integer[][] matrix = new Integer[inputMatrix.length][inputMatrix[0].length];

        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[0].length; j++) {
                if (inputMatrix[i][j] != null && inputMatrix[i][j] != 0)
                    matrix[i][j] = 1;
            }
        }
        return matrix;
    }

    public Integer[][] copyMatrix(Integer[][] inputMatrix) {
        Integer[][] matrix = new Integer[inputMatrix.length][inputMatrix[0].length];
        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[0].length; j++) {
                matrix[i][j] = inputMatrix[i][j];
            }
        }
        return matrix;
    }

    public String[][] copyMatrix(String[][] inputMatrix) {
        String[][] matrix = new String[inputMatrix.length][inputMatrix[0].length];
        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[0].length; j++) {
                matrix[i][j] = inputMatrix[i][j];
            }
        }
        return matrix;
    }

    public void main(String[] args) {
        ImageAlgorithm ia = new ImageAlgorithm();
        Integer[][] a = {{1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}};
        Integer[][] b = ia.initFloodFill(a);
        System.out.println(b);
    }
}
