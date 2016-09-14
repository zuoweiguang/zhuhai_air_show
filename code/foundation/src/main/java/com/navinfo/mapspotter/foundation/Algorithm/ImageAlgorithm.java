package com.navinfo.mapspotter.foundation.algorithm;

import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * 图像处理相关的算法
 * Created by cuiliang on 2016/1/5.
 */
public class ImageAlgorithm {

    public static double UNCIA = 1d / 12d;

    /**
     * 中值滤波
     *
     * @param inputMatrix 传入待滤波矩阵
     * @param size        滤波框大小
     * @return 滤波后矩阵
     */
    public static Integer[][] medianFilter(Integer[][] inputMatrix, int size) {
        Integer[][] outputMatrix = new Integer[inputMatrix.length][inputMatrix.length];
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

    private static int getMatrixValue(Integer[][] inputMatrix, int x, int y) {
        if (x < 0 || x >= inputMatrix.length)
            return 0;
        if (y < 0 || y >= inputMatrix.length)
            return 0;
        return inputMatrix[x][y] == null ? 0 : inputMatrix[x][y];
    }

    /**
     * 获取矩阵中间值
     *
     * @param matrix
     * @param size
     * @return
     */
    private static int getMedian(int[][] matrix, int size) {
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
    public static Integer[][] floodFill(Integer[][] track) {
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
    public static Integer[][] initFloodFill(Integer[][] track) {
        Integer[][] newArray = new Integer[track.length][track.length];
        for (int i = 0; i < track.length; i++) {
            Integer[] trackArray = track[i];
            for (int j = 0; j < trackArray.length; j++) {
                if (track[i][j] != null) {
                    newArray[i][j] = -1;
                }
            }
        }
        return newArray;
    }

    /**
     * 二维矩阵8个方向找连通性
     *
     * @param x     矩阵x坐标
     * @param y     矩阵y坐标
     * @param value 连通分组值
     * @param track 矩阵
     */
    private static void floodFillCell(int x, int y, int value, Integer[][] track) {
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
                if (matrix[i][j] != null) {
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
    private static double average(int[] array) {
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
    private static double[] newArray(int[] array, double avg, int positive) {
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
    private static double muCalculation(double[] arrayX, double[] arrayY,
                                        double add) {
        double amount = 0d;
        for (int i = 0; i < arrayX.length; i++) {
            amount = amount + arrayX[i] * arrayY[i];
        }
        return amount / arrayX.length + add;
    }

    public static Map<String, List<int[]>> CalculationLineStrel(int len, Map<String, Object> oriMap) {

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

    private static List<int[]> makeLineStrel(int len, double angdeg) {
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
    private static List<int[]> getIntLine(int x1, int y1, int x2, int y2) {
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

    private static int round(double r) {
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
    public Integer[][] expansion(Map<String, List<int[]>> regions,
                                 Map<String, List<int[]>> expline) {

        List<int[]> list = new ArrayList<int[]>();
        Map<String, int[]> map = new HashMap<String, int[]>();

        Iterator iter = expline.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();

            List<int[]> region = regions.get(key);
            List<int[]> line = expline.get(key);
            try {
                for (int[] region_xy : region) {
                    int region_x = region_xy[0];
                    int region_y = region_xy[1];

                    for (int[] line_xy : line) {
                        int line_x = line_xy[0];
                        int line_y = line_xy[1];

                        int exp_x = region_x + line_x;
                        int exp_y = region_y + line_y;

                        if (exp_x >= 0 && exp_x < 1024 && exp_y >= 0
                                && exp_y < 1024) {
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
        return this.listToMatrix(list);
    }

    public Integer[][] listToMatrix(List<int[]> list) {
        Integer[][] expMatrix = new Integer[1024][1024];
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

    /**
     * 抽骨骼算法
     *
     * @param matrix 传入矩阵
     * @return 抽骨骼后矩阵
     */
    public static Integer[][] skeletonize(Integer[][] matrix) {
        ByteProcessor bp = new ByteProcessor(matrix.length, matrix.length);
        BinaryProcessor ibp = new BinaryProcessor(bp);
        ibp.setColor(Color.white);
        ibp.fill();
        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 1024; y++) {
                if (matrix[x][y] != null)
                    ibp.putPixel(x, y, 0);
            }
        }

        ibp.skeletonize();

        Integer[][] skeletonMatrix = new Integer[matrix.length][matrix.length];
        for (int x = 0; x < 1024; x++) {
            for (int y = 0; y < 1024; y++) {
                if (ibp.get(x, y) == 0) {
                    skeletonMatrix[x][y] = 1;
                }
            }
        }
        boolean isEmpty = true;
        for (int i = 0; i < 1024; i++) {
            for (int j = 0; j < 1024; j++) {
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

    public static void main(String[] args) {
        Integer[][] a = {{1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}, {1, null, 1, null, 1}};
        Integer[][] b = ImageAlgorithm.initFloodFill(a);
        System.out.println(b);
    }
}
