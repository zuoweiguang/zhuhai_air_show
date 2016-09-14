package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.model.PoiHang;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 热力图数据生成Mapper类
 *
 * Created by gaojian on 2016/1/25.
 */
public class HeatmapMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger logger = Logger.getLogger(HeatmapMapper.class);

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        PoiHang poiHang = PoiHang.parse(value.toString());
        if (poiHang == null) return;

        double lon = poiHang.getoLon();
        double lat = poiHang.getoLat();
        int count = poiHang.getCount();

        Text valueOut = new Text(String.valueOf(count));

        for (int i = 5; i < 13; i++) {
            IntCoordinate pixelCoord = MercatorUtil.getDefaultInstance().lonLat2Pixels(
                    new Coordinate(lon, lat), i
            );
            Text keyOut = new Text(String.format(
                    "%d_%d_%d",
                    i, pixelCoord.x, pixelCoord.y
            ));
            context.write(keyOut, valueOut);
        }
    }

}
