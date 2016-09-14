package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.rtree.EnvlopeIndentifiedObject;
import com.navinfo.mapspotter.foundation.algorithm.rtree.SimplePointRTree;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by SongHuiXing on 2016/1/20.
 */
public class CrossInformationVistorTest {
    private CrossInformationVistor vistor = new CrossInformationVistor(null, null, null);

    @Before
    public void setup(){
        System.setProperty("hadoop.home.dir", "D:\\Library\\Hadoop");

        vistor.prepare();
    }

    @After
    public void teardown(){
        vistor.shutdown();
    }

    @Test
    public void testArrayLength() throws IOException {
        SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
        SerializeUtil<Integer[][]> serializeUtil1 = new SerializeUtil<>();

        int length = 1024;
        int[][] intArr = new int[length][length];
        Integer[][] intArr_1 = new Integer[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                intArr[i][j] = 0;
                intArr_1[i][j] = null;
            }
        }
        byte[] integerByte = serializeUtil1.serialize(intArr_1);
        byte[] intByte = serializeUtil.serialize(intArr);

        System.out.println(integerByte.length + "===" + intByte.length);
        FileOutputStream fos = new FileOutputStream(new File("E:\\int.txt"));
        fos.write(intByte);
        fos.close();

        FileOutputStream fos_1 = new FileOutputStream(new File("E:\\Integer.txt"));
        fos_1.write(integerByte);
        fos_1.close();
    }

    @Test
    public void testSparse() throws IOException {
        SerializeUtil<Integer[][]> serializeUtil = new SerializeUtil<>();
        SerializeUtil<int[][]> serializeUtil1 = new SerializeUtil<>();

        Random random = new Random();

        int length = 1024;
        Integer[][] intArr = new Integer[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if(Math.random() > 0.2)
                    intArr[i][j] = null;
                else
                    intArr[i][j] = random.nextInt();
            }
        }

        DoubleMatrix mx = new DoubleMatrix(intArr);

        DoubleMatrix.SparseMatrix sparse = mx.toSparse();

        int[][] intMx = new int[][]{sparse.data, sparse.indices, sparse.indptr};
        byte[] sparseByte = serializeUtil1.serialize(intMx);

        int[][] inverseSparse = serializeUtil1.deserialize(sparseByte);

        byte[] integerByte = serializeUtil.serialize(intArr);

        System.out.println("None null numbers:" + sparse.data.length);
        System.out.println(integerByte.length + "===" + sparseByte.length);
        FileOutputStream fos = new FileOutputStream(new File("E:\\sparse.txt"));
        fos.write(sparseByte);
        fos.close();

        FileOutputStream fos_1 = new FileOutputStream(new File("E:\\Integer.txt"));
        fos_1.write(integerByte);
        fos_1.close();
    }

    @Test
    public void testGetCrossInformation() throws IOException{
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\pid_list.txt"));

        BufferedReader reader = new BufferedReader(input);

        OutputStreamWriter fos =
                new OutputStreamWriter(
                        new FileOutputStream("E:\\WorkSpace\\testdata\\cross_1320.txt"));
        BufferedWriter writer = new BufferedWriter(fos);

        JsonUtil jsonUtil = JsonUtil.getInstance();

        String lineTxt = null;
        while(null != (lineTxt = reader.readLine())) {
            lineTxt = lineTxt.trim();

            if(lineTxt.isEmpty())
                continue;

            BaseCrossJsonModel crossJsonModel = vistor.getCrossInfomation(lineTxt);

            writer.write(jsonUtil.write2String(crossJsonModel));
            writer.newLine();
        }

        input.close();

        writer.flush();
        writer.close();
        fos.close();
    }

    @Test
    public void testExportAllCrossRaster() throws IOException{
        try{
            vistor.exportCrossRaster("E:\\WorkSpace\\testdata\\AllCrossRaster");
        }catch (Exception e){
            Assert.assertTrue(false);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testBuildCrossIndex(){
        Runtime run = Runtime.getRuntime();

        long max = run.maxMemory();
        System.out.println("最大内存 = " + max);

        long total = run.totalMemory();
        System.out.println("已分配内存 = " + total);

        long free = run.freeMemory();
        System.out.println("已分配内存中的剩余空间 = " + free);

        long usable = max - total + free;
        System.out.println("最大可用内存 = " + usable);

        long t1 = System.currentTimeMillis();

        SimplePointRTree tree = vistor.buildMemIndex();

        System.out.println("索引数量 = " + tree.getTotalObjCount());

        long t2 = System.currentTimeMillis();

        System.out.println("构建时间(毫秒) = " + (t2 - t1));

        max = run.maxMemory();
        System.out.println("最大内存 = " + max);

        total = run.totalMemory();
        System.out.println("已分配内存 = " + total);

        free = run.freeMemory();
        System.out.println("已分配内存中的剩余空间 = " + free);

        usable = max - total + free;
        System.out.println("最大可用内存 = " + usable);

        long crossCount = tree.getTotalObjCount();
        System.out.println("索引存储路口数 = " + crossCount);

        long t3 = System.currentTimeMillis();

        Map<Long, EnvlopeIndentifiedObject> objs = tree.find(new double[]{114.466176, 37.017490, 114.466212, 37.018806});

        long t4 = System.currentTimeMillis();

        System.out.println("查询时间(毫秒) = " + (t4 - t3));
        System.out.println("查询结果路口数 = " + objs.size());
    }

    @Test
    public void testGetCrossMatrix() {
        int[][] original = vistor.getOriginalRestrictionMatrix(678);
        for (int i = 0; i < original.length; i++) {
            for(int j=0; j<original[0].length;j++){
                System.out.print(original[i][j]);
            }
            System.out.println();
        }
    }
}
