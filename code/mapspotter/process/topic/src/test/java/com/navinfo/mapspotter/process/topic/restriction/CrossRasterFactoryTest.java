package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import org.geojson.Feature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SongHuiXing on 2016/1/27.
 */
public class CrossRasterFactoryTest {

    private RoadRasterSupplier roadRasterSupplier = new RoadRasterSupplier("road_raster", "1606");
    private CrossRasterFactory factory = null;

    @Before
    public void setup() throws Exception {
        factory = new CrossRasterFactory(14, 1024, roadRasterSupplier, 2, 10);
        factory.prepare();
    }

    @After
    public void cleanup() throws Exception {
        factory.shutdown();
        factory = null;
    }

    @Test
    public void testBuildRaster() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\data\\导出的母库数据\\1.txt"));

        BufferedReader reader = new BufferedReader(input);

//        SerializeUtil<ArrayList<Map.Entry<String, IntCoordinate>>> arraySerilizeUtil =
//                                                                    new SerializeUtil<>();

        //ImageAlgorithm imageAlgorithm = new ImageAlgorithm();

        String lineTxt = null;
        int count = 0;
        while (null != (lineTxt = reader.readLine())) {

            if(count > 100)
                break;

            BaseCrossJsonModel crossJson = BaseCrossJsonModel.readCrossJson(lineTxt);

            List<Node> nodes = Node.convert(JsonUtil.getInstance().readCollection(crossJson.getNodes(), Feature.class));

            List<Link> links = Link.convert(JsonUtil.getInstance().readCollection(crossJson.getLinks(), Feature.class));

            CrossRaster raster = factory.buildRaster(crossJson.getPID(), links, nodes);

            int[][] mx = raster.getDenseRaster();

            int rowCt = mx.length;
            int colCt = mx[0].length;
            BufferedImage bimage = new BufferedImage(colCt, rowCt,
                                                    BufferedImage.TYPE_INT_RGB);

            for (int i = 0; i < rowCt; i++) {
                for (int j = 0; j < colCt; j++) {
                    int v = mx[i][j];
                    Color color = null;

                    if(v == 255){
                        color = Color.BLACK;
                    }else if(v == 0) {
                        color = new Color(255, 255, 255, 255);
                    } else {
                        int g = 0, b = 0;
                        int r = v % 3;
                        v = v / 3;
                        if(v > 0) {
                            g = v % 3;
                            v = v / 3;
                        }
                        if(v > 0){
                            b = v % 3;
                        }
                        color = new Color(255*r/3, 255*g/3, 255*b/3);
                    }

                    bimage.setRGB(j, i, color.getRGB());
                }
            }

            ImageIO.write(bimage, "jpg", new File(String.format("E:\\WorkSpace\\testdata\\AllCrossRaster\\20160412\\%d.jpg", crossJson.getPID())));

//            imageAlgorithm.arrayToFile(mx, String.format("E:\\WorkSpace\\testdata\\AllCrossRaster\\20160322\\%d.txt",
//                                                        crossJson.getPID()));
            //byte[] bytes = arraySerilizeUtil.serialize(raster.getCornerTilePos());

            //ArrayList<Map.Entry<String, IntCoordinate>> info = arraySerilizeUtil.deserialize(bytes);

            count++;
        }

        input.close();
    }

    @Test
    public void testGetCrossRasterJson() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\路口Debug数据.txt"));

        BufferedReader reader = new BufferedReader(input);

        OutputStreamWriter output = new OutputStreamWriter(
                new FileOutputStream("E:\\WorkSpace\\testdata\\AllCrossRaster\\raster1.txt"));
        BufferedWriter writer = new BufferedWriter(output);

        JsonUtil jsonUtil = JsonUtil.getInstance();

        String lineTxt = null;
        int count = 0;
        while (null != (lineTxt = reader.readLine())) {

            if(count > 100)
                break;

            BaseCrossJsonModel crossJson = BaseCrossJsonModel.readCrossJson(lineTxt);

            List<Node> nodes = Node.convert(JsonUtil.getInstance().readCollection(crossJson.getNodes(), Feature.class));

            List<Link> links = Link.convert(JsonUtil.getInstance().readCollection(crossJson.getLinks(), Feature.class));

            CrossRaster raster = factory.buildRaster(crossJson.getPID(), links, nodes);

            int[][] mx = raster.getDenseRaster();
            double[] envelope = raster.getPageEnvelope();

            HashMap<String, Object> pojo = new HashMap<>(2);
            pojo.put("envelope", envelope);
            pojo.put("raster", mx);
            pojo.put("pid", raster.getPid());

            writer.write(jsonUtil.write2String(pojo));
            writer.newLine();

            count++;
        }

        input.close();
        output.flush();
        output.close();
    }

    @Test
    public void testBuildRoadRaster() throws Exception {
//        double[] pageenv = new double[4];
//        pageenv[0] = 116.26395;
//        pageenv[1] = 39.72146;
//        pageenv[2] = 116.29384;
//        pageenv[3] = 39.73821;
//
//        MercatorUtil mercatorUtil = new MercatorUtil(1024, 14);
//
//        String tileCode0 = mercatorUtil.lonLat2MCode(new Coordinate(pageenv[0], pageenv[1]));
//        System.out.println(tileCode0);
//
//        String tileCode1 = mercatorUtil.lonLat2MCode(new Coordinate(pageenv[0], pageenv[3]));
//        System.out.println(tileCode1);
//
//        String tileCode2 = mercatorUtil.lonLat2MCode(new Coordinate(pageenv[2], pageenv[3]));
//        System.out.println(tileCode2);
//
//        String tileCode3 = mercatorUtil.lonLat2MCode(new Coordinate(pageenv[2], pageenv[1]));
//        System.out.println(tileCode3);

//        org.jblas.DoubleMatrix bottomMx = roadRasterSupplier.getRoadRaster("13498_6190");
//        org.jblas.DoubleMatrix topMx = roadRasterSupplier.getRoadRaster("13498_6189");

        org.jblas.DoubleMatrix leftMx = roadRasterSupplier.getRoadRaster("13483_6199");
        org.jblas.DoubleMatrix rightMx = roadRasterSupplier.getRoadRaster("13484_6199");

        //org.jblas.DoubleMatrix roadMx = org.jblas.DoubleMatrix.concatVertically(topMx, bottomMx);
        org.jblas.DoubleMatrix roadMx = org.jblas.DoubleMatrix.concatHorizontally(leftMx, rightMx);

        BufferedImage bimage = new BufferedImage(roadMx.columns,
                                                roadMx.rows,
                                                BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < roadMx.rows; i++) {
            for (int j = 0; j < roadMx.columns; j++) {
                bimage.setRGB(j, i, (int)roadMx.get(i, j));
            }
        }

        ImageIO.write(bimage, "jpg", new File("E:\\concatRoad_lr_1.jpg"));

    }
}