package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by gaojian on 2016/2/20.
 */
public class HeatmapGroupMR {
    private static final Logger logger = Logger.getLogger(HeatmapGroupMR.class);

    public static class HeatmapGroupMapper extends Mapper<LongWritable, Text, Text, Text> {
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
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] values = value.toString().split("\t");
            if (values.length != 4) return;

            int level = Integer.parseInt(values[0]);
            double lon = Double.parseDouble(values[1]);
            double lat = Double.parseDouble(values[2]);
            int count = Integer.parseInt(values[3]);

            String areaId = areaAnalysis.locateArea(GeoUtil.createPoint(lon, lat));
            if (StringUtil.isEmpty(areaId)) {
                return;
            }

            String keyOut = areaId + "\t" + level;

            String valueOut = String.format("%f,%f,%d", lon, lat, count);

            context.write(new Text(keyOut), new Text(valueOut));
        }
    }

    public static class HeatmapGroupReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String result = "";
            int count = 0;
            for (Text value : values) {
                result = result + "[" + value.toString() + "]";
                count++;
            }

            result = result + "\t" + count;

            context.write(key, new Text(result));
        }
    }

    public static void main(String[] args) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        if (args.length != 3) {
            logger.error("Args is wrong.");
            return;
        }
        String inputPath = args[0];
        String outPath = args[1];
        long split = Long.parseLong(args[2]);

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "HeatmapGroup");
        job.setJarByClass(HeatmapGroupMR.class);
        job.setMapperClass(HeatmapGroupMapper.class);
        job.setReducerClass(HeatmapGroupReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        // 计算分割文件大小，控制map个数
        long totalSize = Hdfs.getFileSize(conf, inputPath);
        long splitSize = totalSize / split;
        FileInputFormat.setMaxInputSplitSize(job, splitSize);

        FileOutputFormat.setOutputPath(job, new Path(outPath));

        job.waitForCompletion(true);
        System.out.println("Total time : "
                + (System.currentTimeMillis() - currentTimeMillis));
    }
}
