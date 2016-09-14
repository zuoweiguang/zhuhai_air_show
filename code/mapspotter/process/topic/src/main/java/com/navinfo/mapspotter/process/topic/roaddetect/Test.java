package com.navinfo.mapspotter.process.topic.roaddetect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * Created by cuiliang on 2016/1/31.
 */
public class Test {

    public static void main(String[] args) throws Exception{
//        MercatorUtil mkt = new MercatorUtil(1024, 12);
//        IntCoordinate _m = mkt.lonLat2Pixels(new Coordinate(119, 39));
//        System.out.println(_m.x);
//        System.out.println(_m.y);
//        IntCoordinate _m2= mkt.pixelsInTile(_m);
//        System.out.println(_m2.x);
//        System.out.println(_m2.y);
//
//        String rowKey = mkt.lonLat2MCode(new Coordinate(
//                Double.parseDouble("119"),
//                Double.parseDouble("39")
//        ));
//        System.out.println(rowKey);
//        Envelope bound = mkt.mercatorBound(rowKey.toString());
//        // 左下角坐标转换为mercator坐标
//        Coordinate om = mkt.lonLat2Meters(new Coordinate(bound.getMinX(), bound.getMinY()));
//        TransformationUtil transformation = new TransformationUtil();
//        transformation.SetDeviceFrame(1024, 1024);
//        transformation.SetWorldBounds(om.x, om.y, 9784, 9784);
//
//        Coordinate _m3 = mkt.lonLat2Meters(new Coordinate(119, 39));
//        double[] indexs = transformation.TransformDevice(_m3.x, _m3.y);
//
//        int x = (int) Math.floor(indexs[0]);
//        int y = (int) Math.floor(indexs[1]);
//        System.out.println(x);
//        System.out.println(y);
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("E:\\fusion\\road\\emergency\\20160326\\baidu_result\\detect\\3547_1465.txt")));
        String[][] matrix = new String[1024][];
        int i = 0;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            matrix[i] = (line.split("\t"));
            i ++;
        }
        array2Image(matrix,"E:\\fusion\\road\\emergency\\20160326\\baidu_result\\detect\\3547_1465.jpg");
    }
    /**
     * 二维数组转图片
     */
    public static void array2Image(String[][] array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        String[][] byteToArray = array;
        for (int i = 0; i < byteToArray.length; i++) {
            String[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                String integer = byteToArray[i][j];
                if (integer != null && Integer.parseInt(integer) > 0) {
                    bImage.setRGB(j, i, Color.BLUE.getRGB());
                } else {
                    bImage.setRGB(j, i, Color.WHITE.getRGB());
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void array2Image(Integer[][] array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        Integer[][] byteToArray = array;
        for (int i = 0; i < byteToArray.length; i++) {
            Integer[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                Integer integer = byteToArray[i][j];
                if (integer != null && integer > 0) {
                    bImage.setRGB(j, i, Color.BLUE.getRGB());
                } else {

                    bImage.setRGB(j, i, Color.WHITE.getRGB());
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void array2ImageWithColor(String[][] array, String path, Color color) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        String[][] byteToArray = array;
        for (int i = 0; i < byteToArray.length; i++) {
            String[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                String integer = byteToArray[i][j];
                if (integer != null && !integer.equals("0")) {
                    if(integer.equals("x")){
                        bImage.setRGB(j, i, Color.BLACK.getRGB());
                    }
                    else{
                        bImage.setRGB(j, i, color.getRGB());
                    }
                } else {
                    bImage.setRGB(j, i, Color.WHITE.getRGB());
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void array2Image(int[][] array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        int[][] byteToArray = array;
        for (int i = 0; i < byteToArray.length; i++) {
            int[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                Integer integer = byteToArray[i][j];
                if (integer != null && integer > 0) {
                    bImage.setRGB(j, i, 222);
                } else {
                    bImage.setRGB(j, i, 0);
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void array2Image(Byte[][] array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        Byte[][] byteToArray = array;
        for (int i = 0; i < byteToArray.length; i++) {
            Byte[] arr = byteToArray[i];
            for (int j = 0; j < arr.length; j++) {
                Byte integer = byteToArray[i][j];
                if (integer != null && integer > 0) {
                    bImage.setRGB(j, i, 222);
                } else {
                    bImage.setRGB(j, i, 0);
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void array2Image(List<List<String>> array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        List<List<String>> byteToArray = array;
        for (int i = 0; i < byteToArray.size(); i++) {
            List<String> arr = byteToArray.get(i);
            for (int j = 0; j < arr.size(); j++) {
                String integer = arr.get(i);
                if (integer != null && Integer.parseInt(integer) > 0) {
                    System.out.println(i + ", " + j);
                    bImage.setRGB(j, i, 222);
                } else {
                    bImage.setRGB(j, i, 0);
                }
            }
        }
        try {
            ImageIO.write(bImage, "jpg", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
