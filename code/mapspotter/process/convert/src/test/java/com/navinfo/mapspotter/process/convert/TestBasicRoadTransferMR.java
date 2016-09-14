package com.navinfo.mapspotter.process.convert;

import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.convert.road.transfer.Transformation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Created by huanghai on 2016/1/27.
 */
public class TestBasicRoadTransferMR {
    int[][] matrix = new int[1024][1024];

    public static void main(String args[]) throws Exception {
        TestBasicRoadTransferMR mr = new TestBasicRoadTransferMR();
        mr.reduce();
    }

    public void reduce() throws IOException, InterruptedException {
        MercatorUtil mercatorUtil = new MercatorUtil(256, 14);
        Transformation tf = new Transformation();
        tf.setDeviceFrame(1024, 1024);
        String value = "84884326|4\t13809700,6358049|13809681,6358049|13809670,6358049|";
        String[] strs = value.toString().split("\t");
        if (strs.length == 2) {
            String[] pidLane = strs[0].split("\\|");
            int pid = Integer.parseInt(pidLane[0]);
            int laneNum = (int) Math.round(Integer.parseInt(pidLane[1]) * 1.4) + 12;
            if (laneNum % 2 == 0) { // 偶数+1，必须为奇数
                laneNum = laneNum + 1;
            }
            String[] pixelCoords = strs[1].split("\\|");
            for (int i = 0; i < pixelCoords.length - 1; i++) {
                String[] xy_1 = pixelCoords[i].split(",");
                Coordinate meters_1 = mercatorUtil.pixels2Meters(
                        new IntCoordinate(Integer.parseInt(xy_1[0]), Integer.parseInt(xy_1[1])),
                        16);

                String[] xy_2 = pixelCoords[i + 1].split(",");
                Coordinate meters_2 = mercatorUtil.pixels2Meters(
                        new IntCoordinate(Integer.parseInt(xy_2[0]), Integer.parseInt(xy_2[1])),
                        16);

                //第一个点
                String tile_1 = mercatorUtil.meters2MCode(meters_1);
                // 第二个点
                String tile_2 = mercatorUtil.meters2MCode(meters_2);

                // 优先判断是否存在跨瓦片的情况
                if (tile_1.equals(tile_2)) {
                    Envelope tileBound = MercatorUtil.mercatorBound(tile_1, 14);
                    getDevicePixels(tf, meters_1.x, meters_1.y, meters_2.x, meters_2.y,
                            tile_1, tileBound, laneNum, pid);
                } else {
                    // 处理第一个坐标
                    Envelope tileBound_1 = MercatorUtil.mercatorBound(tile_1, 14);
                    getDevicePixels(tf, meters_1.x, meters_1.y, meters_2.x, meters_2.y,
                            tile_1, tileBound_1, laneNum, pid);
                    // 处理第二个坐标
                    Envelope tileBound_2 = MercatorUtil.mercatorBound(tile_2, 14);
                    getDevicePixels(tf, meters_1.x, meters_1.y, meters_2.x, meters_2.y,
                            tile_2, tileBound_2, laneNum, pid);
                }
            }
        }


        // 输出结果到文本文件
        File outFileOld = new File("E:\\one_link_test.txt");
        BufferedWriter bwOld = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFileOld)));
        for (int i = 0; i < matrix.length; i++) {
            StringBuilder line = new StringBuilder();
            int[] arr = matrix[i];
            for (int j = 0; j < arr.length; j++) {
                int integer = matrix[j][i];
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

    }

    /**
     * 计算像素坐标
     *
     * @param tf          Transformation对象
     * @param mercatorX_1 墨卡托坐标X
     * @param mercatorY_1 墨卡托坐标Y
     * @param mercatorX_2 墨卡托坐标X
     * @param mercatorY_2 墨卡托坐标Y
     * @param tile        墨卡托瓦片号
     * @param tileBound   瓦片矩阵边框
     * @param pid         linkpid
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    private void getDevicePixels(
            Transformation tf, double mercatorX_1, double mercatorY_1, double mercatorX_2, double mercatorY_2,
            String tile, Envelope tileBound, int laneNum, int pid)
            throws IOException, InterruptedException {
        // 获取瓦片bound
        Coordinate tileBoundMercator_min = MercatorUtil.lonLat2Meters(
                new Coordinate(tileBound.getMinX(), tileBound.getMinY()));

        Coordinate tileBoundMercator_max = MercatorUtil.lonLat2Meters(
                new Coordinate(tileBound.getMaxX(), tileBound.getMaxY()));
        // 设置矩阵
        tf.setWorldBounds(tileBoundMercator_min.x, tileBoundMercator_min.y,
                tileBoundMercator_max.x - tileBoundMercator_min.x,
                tileBoundMercator_max.y - tileBoundMercator_min.y);
        // 计算出对应矩阵的像素坐标
        double[] devicePixels_1 = tf.transformDevice(mercatorX_1,
                mercatorY_1);
        DecimalFormat format = new DecimalFormat("#,##0.00000");
        System.out.println(format.format(devicePixels_1[0]) + "===" + format.format(devicePixels_1[1]));
        double[] devicePixels_2 = tf.transformDevice(mercatorX_2,
                mercatorY_2);
        System.out.println(format.format(devicePixels_2[0]) + "===" + format.format(devicePixels_2[1]));
        // 计算直线经过了哪几个像素坐标
        gridOperate((int) devicePixels_1[0], (int) devicePixels_1[1],
                (int) devicePixels_2[0], (int) devicePixels_2[1], 1024,
                1024, tile, laneNum, pid);
    }

    /**
     * @param xStart 开始点坐标
     * @param yStart 开始点坐标
     * @param xEnd   结束点坐标
     * @param yEnd   结束点坐标
     * @param width  宽度分成多少份
     * @param height 高度分成多少份
     */
    public void gridOperate(int xStart, int yStart, int xEnd, int yEnd,
                            int width, int height, String tileNumber, int laneNum, int pid) throws IOException, InterruptedException {
        if (xStart >= width) {
            xStart = width - 1;
        }
        if (xStart < 0) {
            xStart = 0;
        }
        if (xEnd >= width) {
            xEnd = width - 1;
        }
        if (xEnd < 0) {
            xEnd = 0;
        }
        if (yStart >= height) {
            yStart = height - 1;
        }
        if (yStart < 0) {
            yStart = 0;
        }
        if (yEnd >= height) {
            yEnd = height - 1;
        }
        if (yEnd < 0) {
            yEnd = 0;
        }

        int tDeltaX, tDeltaY, x, y;

        int tDeltaXTimes2, tDeltaYTimes2;

        int incrE, incrNE, d, i;

        if (xStart == xEnd) {
            if (yStart > yEnd) {
                int temp = yStart;
                yStart = yEnd;
                yEnd = temp;
            }

            for (y = yStart; y <= yEnd; y++) {
                // [xStart, y]
                bufferRoadLon(xStart, y, tileNumber, laneNum, pid);
            }
            return;
        } else if (xStart > xEnd) {
            int temp = xStart;
            xStart = xEnd;
            xEnd = temp;

            temp = yStart;
            yStart = yEnd;
            yEnd = temp;
        }

        x = xStart;
        y = yStart;
        tDeltaX = xEnd - xStart;
        tDeltaY = yEnd - yStart;
        tDeltaXTimes2 = tDeltaX << 1;
        tDeltaYTimes2 = tDeltaY << 1;

        if (tDeltaY >= 0 && tDeltaX >= tDeltaY) { // 1>=k>=0
            d = tDeltaYTimes2 - tDeltaX;
            incrE = tDeltaYTimes2;
            incrNE = (tDeltaY - tDeltaX) << 1;

            for (i = 0; i <= tDeltaX; i++) {
                bufferRoadLat(x, y, tileNumber, laneNum, pid);
                x++;
                if (d >= 0) {
                    y++;
                    d += incrNE;
                } else
                    d += incrE;
            }
        } else if (tDeltaX > 0 && tDeltaY > tDeltaX) { // k>1
            d = tDeltaXTimes2 - tDeltaY;
            incrE = tDeltaXTimes2;
            incrNE = (tDeltaX - tDeltaY) << 1;
            for (i = 0; i <= tDeltaY; i++) {
                bufferRoadLon(x, y, tileNumber, laneNum, pid);
                y++;
                if (d >= 0) {
                    x++;
                    d += incrNE;
                } else
                    d += incrE;
            }
        } else if (tDeltaY < 0 && tDeltaY + tDeltaX >= 0) { // 0>k>=-1
            d = tDeltaYTimes2 + tDeltaX;
            incrE = tDeltaYTimes2;
            incrNE = (tDeltaY + tDeltaX) << 1;
            for (i = 0; i <= tDeltaX; i++) {
                bufferRoadLat(x, y, tileNumber, laneNum, pid);
                x++;
                if (d <= 0) {
                    y--;
                    d += incrNE;
                } else
                    d += incrE;
            }
        } else { // k<-1
            d = tDeltaXTimes2 + tDeltaY;
            incrE = tDeltaXTimes2;
            incrNE = (tDeltaY + tDeltaX) << 1;
            for (i = 0; i >= tDeltaY; i--) {
                bufferRoadLon(x, y, tileNumber, laneNum, pid);
                y--;
                if (d >= 0) {
                    x++;
                    d += incrNE;
                } else
                    d += incrE;
            }
        }
    }

    /**
     * context输出
     *
     * @param x          x位置
     * @param y          y位置
     * @param tileNumber 瓦片号
     * @param pid        linkpid
     * @throws IOException
     * @throws InterruptedException
     */
    private void contextWrite(int x, int y, String tileNumber, int pid) throws IOException, InterruptedException {
        String s_core = x + "," + y + ","
                + pid;
        // 对瓦片号进行反转，防止热点Region
        String sBuilder = new StringBuilder(tileNumber).reverse().toString();
        System.out.println(s_core);
        matrix[x][y] = pid;
    }


    private void bufferRoadLon(int x, int y, String tileNumber, int bufferLength, int pid) throws IOException, InterruptedException {
        for (int i = 0; i < bufferLength; i++) {
            int bufferLen = Math.abs(bufferLength / 2 - i);
            // buffer
            if ((x - bufferLen) >= 0 && i < bufferLength / 2) {
                contextWrite((x - bufferLen), y, tileNumber, pid);
            }
            if ((x + bufferLen) < 1024 && i > bufferLength / 2) {
                contextWrite((x + bufferLen), y, tileNumber, pid);
            }
            // 核心路网
            if (i == bufferLength / 2) {
                contextWrite(x, y, tileNumber, pid);
            }
        }
    }

    private void bufferRoadLat(int x, int y, String tileNumber, int bufferLength, int pid) throws IOException, InterruptedException {
        for (int i = 0; i < bufferLength; i++) {
            int bufferLen = Math.abs(bufferLength / 2 - i);
            // buffer
            if ((y - bufferLen) >= 0 && i < bufferLength / 2) {
                contextWrite(x, (y - bufferLen), tileNumber, pid);
            }
            if ((y + bufferLen) < 1024 && i > bufferLength / 2) {
                contextWrite(x, (y + bufferLen), tileNumber, pid);
            }
            // 核心路网
            if (i == bufferLength / 2) {
                contextWrite(x, y, tileNumber, pid);
            }
        }
    }
}
