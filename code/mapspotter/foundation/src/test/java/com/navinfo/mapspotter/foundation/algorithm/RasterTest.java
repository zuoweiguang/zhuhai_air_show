package com.navinfo.mapspotter.foundation.algorithm;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import junit.framework.TestCase;
import org.junit.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/1/25.
 */
public class RasterTest extends TestCase {

    public void testGetBresenhamline() throws Exception {

    }

    public void testGetAdvanceBresenhamline() throws Exception {
//        List<int[]> lines = Raster.getAdvanceBresenhamline(90, 180, 50, 50);
//
//        BufferedImage bimage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
//
//        for(int[] coords : lines){
//            bimage.setRGB(coords[0], coords[1], 255);
//        }
//
//        ImageIO.write(bimage, "jpg", new File("E:\\Test2.jpg"));

        List<int[]> lines = Raster.getAdvanceBresenhamline(143, 37, 142, 35);

        System.out.print(JsonUtil.getInstance().write2String(lines));
    }

    public void testGetBresenhamEllipse() throws Exception {
        List<int[]> ecllipse = Raster.getBresenhamEllipse(511, 511, 100, 50);

        List<int[]> trans = SpatialUtil.transform(ecllipse, 0, -511, -511);

        List<double[]> zoom = null;
        List<double[]> src = new ArrayList<>();
        for (int[] c : trans){
            src.add(new double[]{c[0], c[1]});
        }
        zoom = SpatialUtil.zoom(src, 2, 2);

        trans.clear();
        for(double[] c : zoom){
            trans.add(new int[]{(int)Math.floor(c[0]), (int)Math.floor(c[1])});
        }
        trans = SpatialUtil.transform(trans, Math.PI/4, 511, 511);

        BufferedImage bimage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);

        for(int[] coords : trans){
            bimage.setRGB(coords[0], 1023-coords[1], 255);
        }

        for(int i=0;i<20;i++){
            for (int j=20;j<40;j++){
                bimage.setRGB(i, j, 255);
            }
        }

        ImageIO.write(bimage, "jpg", new File("E:\\Test2.jpg"));
    }
}