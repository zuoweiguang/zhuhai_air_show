package com.navinfo.mapspotter.foundation.util;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by gaojian on 2016/3/21.
 */
public class MatrixUtil {
    private static SerializeUtil<int[][]> icu = new SerializeUtil<>();
    private static SerializeUtil<Integer[][]> ocu = new SerializeUtil<>();


    /**
     * 反序列化
     * @param bytes   序列化的byte数组
     * @param sparse  是否为稀疏矩阵
     * @return
     */
    public static Integer[][] deserializeMatrix(byte[] bytes, boolean sparse) {
        if(sparse){
            int[][] sparseMatrix = icu.deserialize(bytes);
            DoubleMatrix dMatrix = new DoubleMatrix(sparseMatrix[0], sparseMatrix[1], sparseMatrix[2]);
            return dMatrix.toIntegerArray2();
        }
        else{
            return ocu.deserialize(bytes);
        }
    }

    /**
     * 序列化
     * @param matrix  矩阵
     * @param sparse  是否为稀疏矩阵
     * @return
     */
    public static byte[] serializeSparseMatrix(Integer[][] matrix, boolean sparse) {
        DoubleMatrix dm = new DoubleMatrix(matrix);
        DoubleMatrix.SparseMatrix sparseMatrix = dm.toSparse();
        int[][] resultMatrix = {sparseMatrix.data, sparseMatrix.indices, sparseMatrix.indptr};
        return sparse ? icu.serialize(resultMatrix): ocu.serialize(matrix);
    }

    /**
     * 二维数组转图片
     */
    public static void matrix2Image(String[][] array, String path) {
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

    public static void matrix2Image(Integer[][] array, String path) {
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

    public static void matrix2ImageWithColor(String[][] array, String path, Color color) {
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



    public static void matrix2Image(int[][] array, String path) {
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

    public static void matrix2Image(Byte[][] array, String path) {
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

    public static void array2Image(java.util.List<java.util.List<String>> array, String path) {
        BufferedImage bImage = new BufferedImage(1024, 1024,
                BufferedImage.TYPE_INT_RGB);
        java.util.List<java.util.List<String>> byteToArray = array;
        for (int i = 0; i < byteToArray.size(); i++) {
            java.util.List<String> arr = byteToArray.get(i);
            for (int j = 0; j < arr.size(); j++) {
                String integer = arr.get(j);
                if (integer != null && Integer.parseInt(integer) > 0) {
//                    System.out.println(i + ", " + j);
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