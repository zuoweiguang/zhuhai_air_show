package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;

/**
 * Created by cuiliang on 2016/6/24.
 */
public class DDSourceGroupByCarTypeMR {

    private static final Logger logger = Logger.getLogger(DDSourceGroupByCarTypeMR.class);
    private static final int level = 12;
    private static MercatorUtil mkt = new MercatorUtil(1024, level);


    public static class DDSourceGroupByCarTypeMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private String source = "";


        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
        }


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String v = value.toString();

            String userId = v.split(",")[0];
            long timestamp = Long.parseLong(v.split(",")[3]);
            double speed = Double.parseDouble(v.split(",")[4]);
            double direction = Double.parseDouble(v.split(",")[5]);

            String carType = v.split(",")[9].trim();

            if (speed < 0 || speed >= 250) {
                return;
            }
            if (direction < 0 || direction > 360) {
                return;
            }
            if (Long.parseLong(userId) <= 0) {
                return;
            }
            if (timestamp <= 0) {
                return;
            }

            context.write(new Text(carType), new IntWritable(1));
        }

    }

    public static class DDSourceGroupByCarTypeReducer
            extends Reducer<Text, IntWritable, Text, LongWritable> {
        private LongWritable result = new LongWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            long sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(DDSourceGroupByCarTypeMR.class);
        job.setMapperClass(DDSourceGroupByCarTypeMR.DDSourceGroupByCarTypeMapper.class);
        job.setReducerClass(DDSourceGroupByCarTypeMR.DDSourceGroupByCarTypeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        for (int i = 0; i < args.length-1; i++) {
            String inputPath = args[i];
            FileInputFormat.addInputPath(job, new Path(inputPath));
        }

        FileOutputFormat.setOutputPath(job, new Path(args[args.length-1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
