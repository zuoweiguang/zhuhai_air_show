package com.navinfo.mapspotter.foundation.algorithm;


import org.jblas.ranges.IntervalRange;
import org.jblas.ranges.Range;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cuiliang on 2016/1/5.
 * 由于DoubleMatrix构造函数中没有传入Integer数组的构造函数，继承增加
 */
public class DoubleMatrix extends org.jblas.DoubleMatrix {

    public static class SparseMatrix{
        public int[] data;
        public int[] indices;
        public int[] indptr;
    }

    public DoubleMatrix(org.jblas.DoubleMatrix src){
        super(src.rows, src.columns);

        copy(src);
    }

    public DoubleMatrix(int rows, int cols){
        super(rows, cols);
    }

    public DoubleMatrix(Integer[][] data){
        super(data.length, data[0].length);

        int r;
        for(r = 0; r < this.rows; ++r) {
            assert data[r].length == this.columns;
        }

        for(r = 0; r < this.rows; ++r) {
            for(int c = 0; c < this.columns; ++c) {
                Integer value = data[r][c];

                if(null != value)
                    this.put(r, c, data[r][c]);
            }
        }
    }

    public DoubleMatrix(int[][] data){
        super(data.length, data[0].length);

        int r;
        for(r = 0; r < this.rows; ++r) {
            assert data[r].length == this.columns;
        }

        for(r = 0; r < this.rows; ++r) {
            for(int c = 0; c < this.columns; ++c) {
                this.put(r, c, data[r][c]);
            }
        }
    }

    public DoubleMatrix(short[][] data){
        super(data.length, data[0].length);

        int r;
        for(r = 0; r < this.rows; ++r) {
            assert data[r].length == this.columns;
        }

        for(r = 0; r < this.rows; ++r) {
            for(int c = 0; c < this.columns; ++c) {
                this.put(r, c, data[r][c]);
            }
        }
    }

    /**
     * 初始化行稀疏方阵 Compressed Row Storage (CRS)
     * @param data  data
     * @param indices col indexs
     * @param indptr row indexs
     */
    public DoubleMatrix(int[] data, int[] indices, int[] indptr){
        super(indptr.length-1, indptr.length-1);

        copyFromSparse(this, data, indices, indptr);
    }

    /**
     * 从一个源拷贝创建一个DoubleMatrix
     * @param src   源
     * @param srow  拷贝源的起始行
     * @param erow  拷贝源的终止行
     * @param scol  拷贝源的起始列
     * @param ecol  拷贝源的终止列
     */
    public DoubleMatrix(org.jblas.DoubleMatrix src, int srow, int erow, int scol, int ecol){
        super(erow - srow +1, ecol - scol +1);

        for(int i=0;i<this.rows;i++){
            int focusRow = i + srow;
            for(int j=0; j<this.columns; j++) {
                this.put(i, j, src.get(focusRow, j+scol));
            }
        }
    }

    /**
     * 以矩阵的左下为起始点，拷贝源到矩阵
     * @param src
     * @param srow
     * @param erow
     * @param scol
     * @param ecol
     * @return
     */
    public DoubleMatrix copyFromLeftbottom(org.jblas.DoubleMatrix src, int srow, int erow, int scol, int ecol){
        DoubleMatrix left = new DoubleMatrix(src, srow, erow, scol, ecol);

        Range rowRange = new IntervalRange(rows - (erow-srow+1), rows);

        Range colRange = new IntervalRange(0, ecol - scol + 1);

        return (DoubleMatrix) put(rowRange, colRange, left);
    }

    /**
     * 以矩阵的左上为起始点，拷贝源到矩阵
     * @param src
     * @param srow
     * @param erow
     * @param scol
     * @param ecol
     * @return
     */
    public DoubleMatrix copyFromLefttop(org.jblas.DoubleMatrix src, int srow, int erow, int scol, int ecol){
        DoubleMatrix top = new DoubleMatrix(src, srow, erow, scol, ecol);

        Range rowRange = new IntervalRange(0, erow - srow + 1);
        Range colRange = new IntervalRange(0, ecol - scol + 1);

        return (DoubleMatrix) put(rowRange, colRange, top);
    }

    /**
     * 以矩阵的右下为起始点，拷贝源到矩阵
     * @param src
     * @param srow
     * @param erow
     * @param scol
     * @param ecol
     * @return
     */
    public DoubleMatrix copyFromRightbottom(org.jblas.DoubleMatrix src, int srow, int erow, int scol, int ecol){
        DoubleMatrix right = new DoubleMatrix(src, srow, erow, scol, ecol);

        Range rowRange = new IntervalRange(rows - (erow-srow+1), rows);

        Range colRange = new IntervalRange(columns - (ecol-scol+1), columns);

        return (DoubleMatrix) put(rowRange, colRange, right);
    }

    /**
     * 以矩阵的右上为起始点，拷贝源到矩阵
     * @param src
     * @param srow
     * @param erow
     * @param scol
     * @param ecol
     * @return
     */
    public DoubleMatrix copyFromRighttop(org.jblas.DoubleMatrix src, int srow, int erow, int scol, int ecol){
        DoubleMatrix srcMx = new DoubleMatrix(src, srow, erow, scol, ecol);

        Range rowRange = new IntervalRange(0, erow - srow + 1);
        Range colRange = new IntervalRange(columns - (ecol - scol +1), columns);

        return (DoubleMatrix) put(rowRange, colRange, srcMx);
    }

    /**
     * 获取行优先的数据indices
     * @return
     */
    public int[] findRowIndices(){
        int[] linerIndices = findIndices();//列优先

        ArrayList<int[]> pos = new ArrayList<>();

        for(int inx : linerIndices){
            int curRow = inx % this.rows;
            int colInx = inx / this.rows;
            pos.add(new int[]{colInx, curRow});
        }

        Collections.sort(pos, new Raster.RasterCoordCompartor());

        int[] rowIndices = new int[linerIndices.length];

        for (int i=0;i<pos.size();i++){
            int[] p = pos.get(i);

            rowIndices[i] = p[1] * this.columns + p[0];
        }

        return rowIndices;
    }
    /**
     * 转出为CRS稀疏矩阵
     * @return CRS sparse matrix
     */
    public SparseMatrix toSparse(){
        SparseMatrix mx = new SparseMatrix();

        int[] linerIndices = findRowIndices();

        mx.data = new int[linerIndices.length];
        mx.indices = new int[linerIndices.length];
        mx.indptr = new int[this.rows+1];

        mx.indptr[0] = 0;
        int lastRow = 0;
        int indicePos = 0;
        for(; indicePos < linerIndices.length; indicePos++){
            int curRow = linerIndices[indicePos] / this.columns;
            int colInx = linerIndices[indicePos] % this.columns;

            mx.data[indicePos] = (int)get(curRow, colInx);
            mx.indices[indicePos] = colInx;

            for(int j=lastRow+1;j<=curRow;j++){
                mx.indptr[j] = indicePos;
            }

            lastRow = curRow;
        }

        for (int r = lastRow+1; r <= this.rows; r++) {
            mx.indptr[r] = indicePos;
        }

        return mx;
    }

    public static DoubleMatrix fromSparse(int[] data, int[] indices, int[] indptr, int colCount){
        DoubleMatrix mx = new DoubleMatrix(indptr.length-1, colCount);

        copyFromSparse(mx, data, indices, indptr);

        return mx;
    }

    /**
     * 将稀疏矩阵数据拷贝至矩阵
     * @param mx        目标矩阵
     * @param data      数据
     * @param indices   列下标
     * @param indptr    行指示器
     */
    private static void copyFromSparse(DoubleMatrix mx, int[] data, int[] indices, int[] indptr){
        int startRowInx = indptr[0];
        for(int i=1;i<indptr.length;i++){
            int currentRow = i -1;
            int endRowInx = indptr[i];

            for(int col = startRowInx; col < endRowInx; col++){
                int currentCol = indices[col];

                mx.put(currentRow, currentCol, data[col]);
            }

            startRowInx = endRowInx;
        }
    }

    public Integer[][] toIntegerArray2() {
        Integer[][] array = new Integer[this.rows][this.columns];

        for(int r = 0; r < this.rows; ++r) {
            for(int c = 0; c < this.columns; ++c) {
                array[r][c] = (int)Math.rint(this.get(r, c));
            }
        }

        return array;
    }
}
