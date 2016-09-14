package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.jblas.DoubleMatrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 2016/3/17.
 */
public class RoadRasterSupplierTest {

    private RoadRasterSupplier supplier = new RoadRasterSupplier("road_raster", "1606");

    @Before
    public void setup(){
        supplier.prepare();
    }

    @Test
    public void testGetRoadRaster() throws Exception {
        InputStreamReader input =
                new InputStreamReader(
                        new FileInputStream("E:\\WorkSpace\\testdata\\给博士的统计数据\\tile_list.txt"));

        BufferedReader reader = new BufferedReader(input);

        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();

        String targetFolder = "E:\\WorkSpace\\testdata\\给博士的统计数据\\tile_docs";

        String lineTxt = null;
        while (null != (lineTxt = reader.readLine())) {
            String tileCode = lineTxt.trim();
            if(tileCode.isEmpty())
                continue;

            DoubleMatrix mx = supplier.getRoadRaster(tileCode);

            imageAlgorithm.arrayToFile(mx.toIntArray2(),
                                        String.format("%s"+File.separator+"%s.txt",
                                                        targetFolder,
                                                        tileCode));
        }

        reader.close();
        input.close();
    }

    @After
    public void shutdown(){
        supplier.shutdown();
    }
}