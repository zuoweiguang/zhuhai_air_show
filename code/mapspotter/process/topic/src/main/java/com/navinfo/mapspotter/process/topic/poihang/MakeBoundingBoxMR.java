package com.navinfo.mapspotter.process.topic.poihang;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;

import com.navinfo.mapspotter.foundation.model.oldPoiHang.FastSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Created by cuiliang on 2016/2/2.
 */
public class MakeBoundingBoxMR {

    /**
     * @author cuiliang 根据sources 生成外接矩形 ，并适当放大
     */
    public static class MyMapper extends Mapper<LongWritable, Text, Text, Text> {

        private double min_x = 180, min_y = 90;
        private double max_x = 0, max_y = 0;
        private double x = 0, y = 0;

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {
            context.write(new Text("1"), new Text("" + min_x + ";" + min_y
                    + ";" + max_x + ";" + max_y));
        }

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            {
                String v = value.toString();
                if (v.length() < Constants.MR_MAIN_SEPARATOR.length() + 1)
                    return;
                v = v.substring(Constants.MR_MAIN_SEPARATOR.length() + 1);
                FastSource s = (FastSource) JSONObject.toJavaObject(
                        JSONObject.parseObject(v), FastSource.class);
                try {
                    x = s.getX();
                    y = s.getY();
                    if (x <= 0 || y <= 0)
                        return;
                    if (x > max_x)
                        max_x = x;
                    else if (x < min_x)
                        min_x = x;
                    if (y > max_y)
                        max_y = y;
                    else if (y < min_y)
                        min_y = y;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text, Text> {
        private double min_x = 180, min_y = 90;
        private double max_x = 0, max_y = 0;

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            for (Text val : values) {
                String a[] = val.toString().split(";");
                double x1, y1, x2, y2;
                try {
                    x1 = Double.valueOf(a[0]);
                    y1 = Double.valueOf(a[1]);
                    x2 = Double.valueOf(a[2]);
                    y2 = Double.valueOf(a[3]);

                    if (x1 < min_x)
                        min_x = x1;
                    if (y1 < min_y)
                        min_y = y1;
                    if (x2 > max_x)
                        max_x = x2;
                    if (y2 > max_y)
                        max_y = y2;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void cleanup(Context context) throws IOException,
                InterruptedException {
            if ((min_x != 180) && (min_y != 90) && (max_x != 0) && (max_y != 0)
                    && (min_x <= max_x) && (min_y <= max_y)) {

                min_x -= 0.1;

                min_y -= 0.1;

                max_x += 0.1;

                max_y += 0.1;

                context.write(new Text("" + min_x + ";" + min_y + ";" + max_x
                        + ";" + max_y), new Text(""));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "makBox");
        job.setJarByClass(MakeBoundingBoxMR.class);
        job.setMapperClass(MakeBoundingBoxMR.MyMapper.class);
        job.setReducerClass(MakeBoundingBoxMR.MyReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileInputFormat
                .addInputPath(
                        job,
                        new Path(
                                "hdfs://Master.Hadoop:9000/mapreduce/sources/qingbao/20150814"));
        FileOutputFormat.setOutputPath(job, new Path(
                "hdfs://Master.Hadoop:9000/mapreduce/workspace/qingbao/20150814/box"));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static Job makeBoundingBoxJob(String[] args) throws Exception {
        String cpDate = args[0];
        String sourceInput = args[1];
        String makeBoxOutput = args[2];
        Configuration conf = new Configuration();

        Job makeBoundingBox = Job.getInstance(conf, "1-MakeBoundingBoxMR_datax_"
                + cpDate);

        makeBoundingBox.setJarByClass(MakeBoundingBoxMR.class);
        makeBoundingBox.setMapperClass(MakeBoundingBoxMR.MyMapper.class);
        makeBoundingBox.setReducerClass(MakeBoundingBoxMR.MyReducer.class);
        makeBoundingBox.setMapOutputKeyClass(Text.class);
        makeBoundingBox.setMapOutputValueClass(Text.class);
        makeBoundingBox.setOutputKeyClass(Text.class);
        makeBoundingBox.setOutputValueClass(Text.class);
        makeBoundingBox.setNumReduceTasks(1);
        FileInputFormat.setInputPaths(makeBoundingBox, new Path(sourceInput));
        FileOutputFormat
                .setOutputPath(makeBoundingBox, new Path(makeBoxOutput));

        return makeBoundingBox;
    }


}
