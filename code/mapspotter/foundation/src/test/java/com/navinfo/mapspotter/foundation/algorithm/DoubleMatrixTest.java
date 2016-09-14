package com.navinfo.mapspotter.foundation.algorithm;

import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.*;

/**
 * Created by SongHuiXing on 2016/1/25.
 */
public class DoubleMatrixTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testToSparse() throws Exception {
        SerializeUtil<int[][]> serializeUtil1 = new SerializeUtil<>();

        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\parse.txt"));

        BufferedReader reader = new BufferedReader(input);

        int[][] data = new int[1024][1024];

        String lineTxt = null;
        while (null !=(lineTxt = reader.readLine())){
            String[] datas = lineTxt.split(",");

            int pid = Integer.parseInt(datas[2]);
            data[Integer.parseInt(datas[1])][Integer
                    .parseInt(datas[0])] = pid;
        }

        DoubleMatrix mx = new DoubleMatrix(data);

        OutputStreamWriter out1 = new OutputStreamWriter(new FileOutputStream("E:\\dense1.csv"));
        BufferedWriter writer = new BufferedWriter(out1);

        for(int i=0;i<mx.rows;i++){
            StringBuilder sb = new StringBuilder();
            for(int j=0;j<mx.columns;j++){
                sb.append(String.format("%d,", (int)mx.get(i,j)));
            }
            writer.write(sb.toString());
            writer.newLine();
        }

        writer.flush();
        writer.close();
        out1.close();

        DoubleMatrix.SparseMatrix sparse = mx.toSparse();

        int[][] intMx = new int[][]{sparse.data, sparse.indices, sparse.indptr};

        DoubleMatrix mx2 = new DoubleMatrix(sparse.data, sparse.indices, sparse.indptr);
        OutputStreamWriter out2 = new OutputStreamWriter(new FileOutputStream("E:\\dense2.csv"));
        BufferedWriter writer2 = new BufferedWriter(out2);

        for(int i=0;i<mx2.rows;i++){
            StringBuilder sb = new StringBuilder();
            for(int j=0;j<mx2.columns;j++){
                sb.append(String.format("%d,", (int)mx2.get(i,j)));
            }
            writer2.write(sb.toString());
            writer2.newLine();
        }

        writer2.flush();
        writer2.close();
        out2.close();

        byte[] sparseByte = serializeUtil1.serialize(intMx);

        int[][] inverseSparse = serializeUtil1.deserialize(sparseByte);

        DoubleMatrix mx3 = new DoubleMatrix(inverseSparse[0], inverseSparse[1], inverseSparse[2]);
        OutputStreamWriter out3 = new OutputStreamWriter(new FileOutputStream("E:\\dense3.csv"));
        BufferedWriter writer3 = new BufferedWriter(out3);

        for(int i=0;i<mx3.rows;i++){
            StringBuilder sb = new StringBuilder();
            for(int j=0;j<mx3.columns;j++){
                sb.append(String.format("%d,", (int)mx3.get(i,j)));
            }
            writer3.write(sb.toString());
            writer3.newLine();
        }

        writer3.flush();
        writer3.close();
        out3.close();
    }

    public void testProperty() {
        DoubleMatrix mx = new DoubleMatrix(org.jblas.DoubleMatrix.eye(3));
        mx.put(1, 2, 255);
        mx.put(2, 2, 20);
        mx.put(0, 1, 14);
        mx.put(0, 2, 17);

        org.jblas.DoubleMatrix pos = mx.eq(255);

        org.jblas.DoubleMatrix mx1 = mx.mul(mx.lt(20));
        mx1 = mx1.gti(1);

        int[][] array = mx1.toIntArray2();
        for (int i = 0; i < array.length; i++) {
            int[] d = array[i];
            for (int j = 0; j < d.length; j++) {
                System.out.print(d[j]+",");
            }
        }
    }
}