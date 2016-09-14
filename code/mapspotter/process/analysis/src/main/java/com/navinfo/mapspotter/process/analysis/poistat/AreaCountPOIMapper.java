package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.model.PoiHang;
import com.navinfo.mapspotter.foundation.util.*;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gaojian on 2016/3/8.
 */
public class AreaCountPOIMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private static final Logger logger = Logger.getLogger(AreaCountPOIMapper.class);

    private AreaAnalysis areaAnalysis = null;
    private Set<String> meshes = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        areaAnalysis = new AreaAnalysis();
        areaAnalysis.initialize();

        ProvinceUtil provinceUtil = new ProvinceUtil();
        provinceUtil.initProvinceMeshes();
        meshes = new HashSet<>();
        meshes.addAll(provinceUtil.getProvinceMeshes("北京市"));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        areaAnalysis.destroy();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        PoiHang poiHang = PoiHang.parse(value.toString());
        if (poiHang == null) {
            logger.info(value.toString());
            return;
        }

        String mesh = MeshUtil.coordinate2Mesh(poiHang.getoLon(), poiHang.getoLat());
        if (!meshes.contains(mesh)) return;

        String areaId = areaAnalysis.locateArea(GeoUtil.createPoint(poiHang.getoLon(), poiHang.getoLat()));
        if (areaId == null) {
            logger.info(poiHang.toString());
            areaId = "";
        }

        context.write(new Text(areaId), new IntWritable(poiHang.getCount()));
    }
}
