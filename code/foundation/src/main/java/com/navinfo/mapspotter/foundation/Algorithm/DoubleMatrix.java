package com.navinfo.mapspotter.foundation.algorithm;


/**
 * Created by cuiliang on 2016/1/5.
 * 由于DoubleMatrix构造函数中没有传入Integer数组的构造函数，继承增加
 */
public class DoubleMatrix extends org.jblas.DoubleMatrix {

    public DoubleMatrix(Integer[][] data){
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

        int startRowInx = indptr[0];
        for(int i=1;i<indptr.length;i++){
            int currentRow = i -1;
            int endRowInx = indptr[i];

            for(int col = startRowInx; col < endRowInx; col++){
                int currentCol = indices[col];

                this.put(currentRow, currentCol, data[col]);
            }
        }
    }

    /**
     * 转出为CRS稀疏矩阵
     * @param data
     * @param indices
     * @param indptr
     * @return
     */
    public boolean toSparse(int[] data, int[] indices, int[] indptr){
        indptr = new int[this.rows+1];

        indices = findIndices();

        data = new int[indices.length];

        org.jblas.DoubleMatrix nonZeroMx = gt(0);

        int pos = 0;
        for(int i=0; i<indptr.length-1;i++){
            org.jblas.DoubleMatrix rowMx = get(i, nonZeroMx);

            int[] rowEles = rowMx.toIntArray();

            indptr[i] = pos;
            System.arraycopy(rowEles, 0, data, pos, rowEles.length);

            pos += rowEles.length;
        }
        indptr[this.rows] = pos;

        return true;
    }
}
