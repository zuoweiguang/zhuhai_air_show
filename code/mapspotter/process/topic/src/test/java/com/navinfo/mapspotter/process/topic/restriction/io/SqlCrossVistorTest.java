package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.rtree.SimplePointRTree;
import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.CrossRaster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/2/18.
 */
public class SqlCrossVistorTest {

    private SqlCrossVistor vistor = new SqlCrossVistor("192.168.4.128",
                                                        "navinfo",
                                                        3306,
                                                        "reynold",
                                                        "1qaz");

    @Before
    public void setUp() throws Exception {
        vistor.prepare();
    }

    @After
    public void tearDown() throws Exception {
        vistor.shutdown();
    }

    @Test
    public void testInsertCrossIndex() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\部分路口数据.txt"));

        BufferedReader reader = new BufferedReader(input);

        String lineTxt;
        while (null != (lineTxt = reader.readLine())){

            BaseCrossJsonModel crossJsonModel = BaseCrossJsonModel.readCrossJson(lineTxt);

            String pageEnvStr = crossJsonModel.getEnvelope();

            double[] pageEnv = JsonUtil.getInstance().readDoubleArray(pageEnvStr);

            Assert.assertTrue(vistor.insertCrossIndex(crossJsonModel.getPID(), pageEnv));
        }

        input.close();
    }

    @Test
    public void testInsertCrossInfo() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\部分路口数据.txt"));

        BufferedReader reader = new BufferedReader(input);

        String lineTxt = null;
        for (int inx = 0; inx < 5; inx++) {

            if (null == (lineTxt = reader.readLine()))
                continue;

            BaseCrossJsonModel crossJsonModel = BaseCrossJsonModel.readCrossJson(lineTxt);

            CrossRaster raster = new CrossRaster();

            raster.setPid(crossJsonModel.getPID());
            raster.setCrossEnvelope(new double[]{119.23441, 39.23411, 119.74211, 40.1234});
            raster.setPageEnvelope(new double[]{119.13441, 39.03411, 119.94211, 40.8234});

            DoubleMatrix mx = new DoubleMatrix(org.jblas.DoubleMatrix.eye(876));
            DoubleMatrix.SparseMatrix sparseMatrix = mx.toSparse();

            raster.setSparseRaster(new int[][]{sparseMatrix.data, sparseMatrix.indices, sparseMatrix.indptr});

            Assert.assertTrue(vistor.insertCrossInfo(crossJsonModel, raster));
        }

        input.close();
    }

    @Test
    public void testSearch() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\track1000.txt"));

        BufferedReader reader = new BufferedReader(input);

        JsonUtil jsonUtil = JsonUtil.getInstance();
        String lineTxt;
        while (null != (lineTxt = reader.readLine())){
            String jsonS = lineTxt.toString().trim();

            if(jsonS.isEmpty())
                return;

            jsonS = jsonS.substring(jsonS.indexOf("{"));

            CarTrack track = jsonUtil.readValue(jsonS, CarTrack.class);

            double[] trackEnv = track.getEnvelope();

            Map<Long, double[]> crosses = vistor.searchCrosses(trackEnv);

            if(null != crosses && crosses.size() > 0){
                Assert.assertTrue(false);
            }
        }
    }

    @Test
    public void testGetCrossRaster() throws IOException {
        CrossRaster raster = vistor.getCrossRaster(754);

        Assert.assertNotNull(raster);

        int[][] mx = raster.getDenseRaster();

        int rowCt = mx.length;
        int colCt = mx[0].length;
        BufferedImage bimage = new BufferedImage(colCt,
                rowCt,
                BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < rowCt; i++) {
            for (int j = 0; j < colCt; j++) {
                int v = mx[i][j];
                if(v != 255)
                    v = v*50 % 255;
                bimage.setRGB(j, i, v);
            }
        }

        ImageIO.write(bimage, "jpg", new File(String.format("E:\\WorkSpace\\testdata\\%d.jpg", raster.getPid())));
    }

    @Test
    public void testGetOriginalRestrictionMatrix() throws SQLException {
        int[][] resMx = vistor.getOriginalRestrictionMatrix(754);

        Assert.assertTrue(resMx.length > 0);
        Assert.assertTrue(resMx[0].length >0);
    }

    @Test
    public void testBuildCrossIndex() throws SQLException {
        Runtime run = Runtime.getRuntime();

        long max = run.maxMemory();
        System.out.println("最大内存 = " + max);

        long total = run.totalMemory();
        System.out.println("已分配内存 = " + total);

        long free = run.freeMemory();
        System.out.println("已分配内存中的剩余空间 = " + free);

        long usable = max - total + free;
        System.out.println("最大可用内存 = " + usable);

        SimplePointRTree tree = vistor.buildMemIndex();

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
    }
}