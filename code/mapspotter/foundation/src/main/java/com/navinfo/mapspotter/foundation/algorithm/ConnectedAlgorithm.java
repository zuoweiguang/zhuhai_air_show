package com.navinfo.mapspotter.foundation.algorithm;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by cuiliang on 2016/3/31.
 */
public class ConnectedAlgorithm {

    private int dw;
    private int dh;
    private int bgColor;

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }
    private int[] outData;

    public ConnectedAlgorithm() {
        bgColor = 0; // black color
    }

    public Integer[][] doLabel(Integer[][] matrix) {
        int width = matrix.length;
        int[] inputArray = new int[width * width];
        int i = 0;
        for(Integer[] row: matrix){
            for(Integer pixels: row){
                if(pixels != null)
                    inputArray[i] = pixels;
                else
                    inputArray[i] = 0;
                i++;
            }
        }
        int[] result = doLabel(inputArray,  width,  width);
        Integer[][] resultMatrix = new Integer[width][width];
        for(int row = 0 ; row < width ; row++){
            for(int col = 0 ; col < width ; col++){
                resultMatrix[row][col] = result[row * width + col];
            }
        }
        return resultMatrix;
    }

    public int[] doLabel(int[] inPixels, int width, int height) {
        dw = width;
        dh = height;
        int nextlabel = 1;
        int result = 0;
        outData = new int[dw * dh];
        for (int i = 0; i < outData.length; i++) {
            outData[i] = 0;
        }

        // we need to define these two variable arrays.
        int[] eightNeighborhoodPixels = new int[8];
        int[] eightNeighborhoodLabels = new int[8];
        int[] knownLabels = new int[8];

        int srcrgb = 0, index = 0;
        boolean existedLabel = false;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                srcrgb = inPixels[index] & 0x000000ff;
                if (srcrgb == bgColor) {
                    result = 0; // which means no labeled for this pixel.
                } else {
                    // we just find the eight neighborhood pixels.
                    eightNeighborhoodPixels[0] = getPixel(inPixels, row - 1, col); // upper cell
                    eightNeighborhoodPixels[1] = getPixel(inPixels, row, col - 1); // left cell
                    eightNeighborhoodPixels[2] = getPixel(inPixels, row + 1, col); // bottom cell
                    eightNeighborhoodPixels[3] = getPixel(inPixels, row, col + 1); // right cell

                    // four corners pixels
                    eightNeighborhoodPixels[4] = getPixel(inPixels, row - 1, col - 1); // upper left corner
                    eightNeighborhoodPixels[5] = getPixel(inPixels, row - 1, col + 1); // upper right corner
                    eightNeighborhoodPixels[6] = getPixel(inPixels, row + 1, col - 1); // left bottom corner
                    eightNeighborhoodPixels[7] = getPixel(inPixels, row + 1, col + 1); // right bottom corner

                    // get current possible existed labels
                    eightNeighborhoodLabels[0] = getLabel(outData, row - 1, col); // upper cell
                    eightNeighborhoodLabels[1] = getLabel(outData, row, col - 1); // left cell
                    eightNeighborhoodLabels[2] = getLabel(outData, row + 1, col); // bottom cell
                    eightNeighborhoodLabels[3] = getLabel(outData, row, col + 1); // right cell

                    // four corners labels value
                    eightNeighborhoodLabels[4] = getLabel(outData, row - 1, col - 1); // upper left corner
                    eightNeighborhoodLabels[5] = getLabel(outData, row - 1, col + 1); // upper right corner
                    eightNeighborhoodLabels[6] = getLabel(outData, row + 1, col - 1); // left bottom corner
                    eightNeighborhoodLabels[7] = getLabel(outData, row + 1, col + 1); // right bottom corner

                    int minLabel = 0;
                    int count = 0;
                    for (int i = 0; i < knownLabels.length; i++) {
                        if (eightNeighborhoodLabels[i] > 0) {
                            existedLabel = true;
                            knownLabels[i] = eightNeighborhoodLabels[i];
                            minLabel = eightNeighborhoodLabels[i];
                            count++;
                        }
                    }

                    if (existedLabel) {
                        if (count == 1) {
                            result = minLabel;
                        } else {
                            int[] tempLabels = new int[count];
                            int idx = 0;
                            for (int i = 0; i < knownLabels.length; i++) {
                                if (knownLabels[i] > 0) {
                                    tempLabels[idx++] = knownLabels[i];
                                }
                                if (minLabel > knownLabels[i] && knownLabels[i] > 0) {
                                    minLabel = knownLabels[i];
                                }
                            }
                            result = minLabel;
                            mergeLabels(index, result, tempLabels);
                        }
                    } else {
                        result = nextlabel;
                        nextlabel++;
                    }
                    outData[index] = result;
                    // reset and cleanup the known labels now...
                    existedLabel = false;
                    for (int kl = 0; kl < knownLabels.length; kl++) {
                        knownLabels[kl] = 0;
                    }
                }
            }
        }
        return outData;
    }

    private void mergeLabels(int index, int result, int[] labels) {
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == result) continue;
            mergeLabel(index, result, labels[i]);
        }
    }

    private void mergeLabel(int index, int result, int i) {

        int row = index / dw;
        int idx = 0;
        for (int k = 0; k <= row; k++) {
            for (int col = 0; col < dw; col++) {
                idx = k * dw + col;
                if (outData[idx] == i) {
                    outData[idx] = result;
                }
            }
        }
    }

    private int getLabel(int[] data, int row, int col) {
        // handle the edge pixels
        if (row < 0 || row >= dh) {
            return 0;
        }

        if (col < 0 || col >= dw) {
            return 0;
        }

        int index = row * dw + col;
        return (data[index] & 0xffffffff);
    }

    private int getLabel(Integer[][] data, int row, int col) {
        // handle the edge pixels
        if (row < 0 || row >= dh) {
            return 0;
        }
        if (col < 0 || col >= dw) {
            return 0;
        }
        return (data[row][col] & 0xffffffff);
    }

    private int getPixel(int[] data, int row, int col) {
        // handle the edge pixels
        if (row < 0 || row >= dh) {
            return bgColor;
        }

        if (col < 0 || col >= dw) {
            return bgColor;
        }

        int index = row * dw + col;
        return (data[index] & 0x000000ff);
    }

    public static int[] imageData = new int[]{
            0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0,
            0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 1,
            0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1,
            0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0,
            0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0,
            0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0
    };

    public static Integer[][] imageMatrix = new Integer[][]{
            {0, 1, 1, 0, 1, 0, 0, 1, 0, 0},
            {0, 1, 1, 0, 1, 0, 0, 1, 1, 0},
            {0, 1, 1, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 1, 0, 0, 0, 1, 0, 1, 1},
            {0, 0, 1, 1, 1, 1, 0, 0, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
            {0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
            {0, 0, 1, 1, 0, 0, 0, 1, 1, 0}
    };

    public static void main(String[] args) {
        ConnectedAlgorithm ca = new ConnectedAlgorithm();

//        int[] outData = ccl.doLabel(imageData, 11, 8);
//        for(int i=0; i<8; i++) {
//            for(int j = 0; j<11; j++) {
//                int index = i * 11 + j;
//                if(j != 0) {
//                    System.out.print(",");
//                }
//                System.out.print(outData[index]);
//            }
//            System.out.println();
//        }

        Integer[][] outMatrix = ca.doLabel(imageMatrix);
        for (int i = 0; i < outMatrix.length; i++) {
            for (int j = 0; j < outMatrix.length; j++) {
                if (j != 0) {
                    System.out.print(",");
                }
                System.out.print(outMatrix[i][j]);
            }
            System.out.println();
        }
    }
}
