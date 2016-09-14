package com.navinfo.mapspotter.process.topic.road;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * 从匹配下挂好的结果中提取匹配后的轨迹
 * Created by SongHuiXing on 7/26 0026.
 */
public class FliterOnRoadGPS {
    public static class FilterGPSMapper
            extends Mapper<LongWritable, Text, NullWritable, Text> {

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] fields = value.toString().split(",");

            if(fields.length < 30)
                return;

            String carid = fields[2];

            StringBuilder sb = new StringBuilder(carid);

            String onLinkGPS = fields[29];

            String[] gpsCoords = onLinkGPS.split("|");

            for (String gpsCoord : gpsCoords){
                String[] infos = gpsCoord.split(":");
                String timestamp = infos[5];
                String lon = infos[3];
                String lat = infos[4];
            }
        }
    }

    public static class FilterGPSDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(args.length != 2){
                System.err.printf("Usage: %s [generic option] <input path> <output path>\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            Job job = getJob(getConf(), args[0]);

            return job.waitForCompletion(true) ? 0 : 1;
        }

        public static Job getJob(Configuration cfg, String inputPath) throws IOException {

            cfg.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{inputPath},
                            FileSystem.get(cfg), "BASIS", 100)));

            Job job = Job.getInstance(cfg, "Filter On Road GPS");

            job.setJarByClass(FilterGPSDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));

            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(FilterGPSMapper.class);

            job.setOutputFormatClass(TextOutputFormat.class);

            job.setNumReduceTasks(0);

            return job;
        }

    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new FilterGPSDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
