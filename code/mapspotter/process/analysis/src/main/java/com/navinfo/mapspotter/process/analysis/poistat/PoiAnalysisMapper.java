package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.model.PoiHang;
import com.navinfo.mapspotter.foundation.model.PoiStatResult;
import com.navinfo.mapspotter.foundation.util.GeoHashUtil;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Poi下挂后分析
 *
 * Created by gaojian on 2016/2/2.
 */
public class PoiAnalysisMapper extends Mapper<LongWritable, Text, Text, Text> {
    private AreaAnalysis areaAnalysis = null;
    private GeoHashUtil geoHashUtil = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        areaAnalysis = new AreaAnalysis();
        areaAnalysis.initialize();
        geoHashUtil = new GeoHashUtil(10);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        areaAnalysis.destroy();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        PoiHang poiHang = PoiHang.parse(value.toString());

        PoiStatResult result = new PoiStatResult();
        result.setPid(poiHang.getPid());
        result.setCount(poiHang.getCount());

        String keyStr = null;
        double x, y;
        if (StringUtil.isEmpty(poiHang.getPid())) {
            x = poiHang.getoLon();
            y = poiHang.getoLat();
            keyStr = poiHang.getPid();

            result.setName(poiHang.getName());
            result.setAddr(poiHang.getAddr());
            result.setTel(poiHang.getTel());
        } else {
            x = poiHang.getLon();
            y = poiHang.getLat();
            keyStr = poiHang.getName() + geoHashUtil.encode(x, y);

            result.setName(poiHang.getoName());
            result.setAddr(poiHang.getoAddr());
            result.setTel(poiHang.getoTel());
        }
        result.setLon(x);
        result.setLat(y);
        result.setMesh(poiHang.getMesh());
        result.setKind(poiHang.getKind());

        String areaId = areaAnalysis.locateArea(GeoUtil.createPoint(x, y));
        result.setAdmin(areaId);

        context.write(new Text(keyStr), new Text(result.toString()));
    }
}
