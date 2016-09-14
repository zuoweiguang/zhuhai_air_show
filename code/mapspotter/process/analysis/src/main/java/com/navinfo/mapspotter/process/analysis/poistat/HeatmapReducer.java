package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * 热力图数据生成Reducer类
 *
 * Created by gaojian on 2016/1/25.
 */
public class HeatmapReducer extends Reducer<Text, Text, Text, Text> {

//    private static final GeoHashUtil geoHashUtil = new GeoHashUtil(8);
//    private static final byte[] family = Bytes.toBytes("info");
//    private static final byte[] qualifierLon = Bytes.toBytes("lon");
//    private static final byte[] qualifierLat = Bytes.toBytes("lat");
//    private static final byte[] qualifierCount = Bytes.toBytes("count");

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (Text value : values) {
            int count = Integer.parseInt(value.toString());
            sum = sum + count;
        }

        String keyStr = key.toString();
        String[] splits = keyStr.split("_");
        int level = Integer.parseInt(splits[0]);
        int px = Integer.parseInt(splits[1]);
        int py = Integer.parseInt(splits[2]);
        Coordinate coord = MercatorUtil.getDefaultInstance().pixels2LonLat(
                new IntCoordinate(px, py), level
        );
        String valueOut = String.format("%f\t%f\t%d", coord.x, coord.y, sum);

        context.write(new Text(String.valueOf(level)), new Text(valueOut));
    }
}
