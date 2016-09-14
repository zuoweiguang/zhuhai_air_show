package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.JsonObject;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 按区域几何统计Mapper类
 * Created by gaojian on 2016/1/25.
 */
public class AreaCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private static final Logger logger = Logger.getLogger(AreaCountMapper.class);

    private AreaAnalysis areaAnalysis = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        areaAnalysis = new AreaAnalysis();
        areaAnalysis.initialize();
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        areaAnalysis.destroy();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        JsonObject obj = JsonUtil.getInstance().readJson(value.toString());

        String geometry = obj.getStringValue("geometry");
        int count = obj.getIntValue("count");
        if (count == 0)
            count = 1;

        String areaId = areaAnalysis.locateArea(GeoUtil.wkt2Geometry(geometry));
        if (areaId == null) {
            logger.info(geometry);
            areaId = "";
        }

        context.write(new Text(areaId), new IntWritable(count));
    }

}
