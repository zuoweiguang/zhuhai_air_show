package com.navinfo.mapspotter.process.topic.restriction.analysis;

import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.model.SogouCarTrack;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SongHuiXing on 6/30 0030.
 */
public class CalculateValidDataMR {

    enum CalculateSogouCounter{
        InvalidData,
        FormData,
    }

    private static final String MINTIME = "min_timestamp";
    private static final String MAXTIME = "max_timestamp";

    public static class CalculateSogouMapper extends Mapper<LongWritable, Text, NullWritable, BooleanWritable> {
        private long minlevel = 0;
        private long maxlevel = Integer.MAX_VALUE;

        private BooleanWritable outvalue = new BooleanWritable();

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            minlevel = cfg.getLong(MINTIME, 0);

            Date now = new Date();
            maxlevel = cfg.getLong(MAXTIME, now.getTime() / 1000);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] fields = value.toString().split("\t");

            long timeStamp = SogouCarTrack.isValid(fields);
            if(timeStamp < minlevel || timeStamp > maxlevel){
                outvalue.set(false);
                context.getCounter(CalculateSogouCounter.InvalidData).increment(1);
            } else {
                outvalue.set(true);
                context.getCounter(CalculateSogouCounter.FormData).increment(1);
            }

            //context.write(NullWritable.get(), outvalue);
        }
    }

    public static class CalculateSogouReduccer extends Reducer<NullWritable, BooleanWritable, NullWritable, LongWritable>{
        @Override
        protected void reduce(NullWritable key, Iterable<BooleanWritable> mapValues, Context context) throws IOException, InterruptedException {
            long invalidCount = 0;
            for (BooleanWritable v : mapValues){
                if(!v.get()){
                    invalidCount++;
                }
            }

            context.getCounter(CalculateSogouCounter.InvalidData).setValue(invalidCount);

            context.write(NullWritable.get(), new LongWritable(invalidCount));
        }
    }

    public static class CalculateSogouDriver extends Configured implements Tool {

        private static DateFormat DateFormater = new SimpleDateFormat("yyyy-MM-dd");

        @Override
        public int run(String[] args) throws Exception {
            if(args.length < 4){
                System.err.printf("Usage: %s <inputpath> <outputpath> <mindate> <maxdata> [prefix]\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            long minTime = DateFormater.parse(args[2].trim()).getTime() / 1000;
            long maxTime = DateFormater.parse(args[3].trim()).getTime() / 1000;

            System.out.println("Calculate invalid data from mintime: " + minTime +
                                " to maxtime: " + maxTime);

            String prefix = "part";
            if(args.length > 4){
                prefix = args[4];
            }

            Job job = getJob(getConf(), args[0], args[1],
                                minTime, maxTime,
                                prefix);

            boolean res = job.waitForCompletion(true);

            Counters cs = job.getCounters();

            long form = cs.findCounter(CalculateSogouCounter.FormData).getValue();

            System.out.println("Total sogou form data count:" + form);

            long invalidCount = cs.findCounter(CalculateSogouCounter.InvalidData).getValue();

            System.out.println("Total sogou invalid data count:" + invalidCount);

            System.out.println("Invalid rate:" + (double)invalidCount / (form + invalidCount));

            return  res ? 0 : 1;
        }

        public static Job getJob(Configuration cfg, String inputpath,
                                 String outputpath, long mintime, long maxtime,
                                 String prefix) throws IOException {

            cfg.setLong(MINTIME, mintime);
            cfg.setLong(MAXTIME, maxtime);

            Job job = Job.getInstance(cfg, "Calculate sogou invalid data.");
            job.setJarByClass(CalculateValidDataMR.class);

            FileSystem fileSystem = FileSystem.get(cfg);

            ArrayList<Path> files = new ArrayList<>();
            Hdfs.addInputPath(new Path(inputpath), fileSystem, files, prefix);

            job.setOutputFormatClass(TextOutputFormat.class);

            for (Path f : files){
                FileInputFormat.addInputPath(job, f);
            }

            FileOutputFormat.setOutputPath(job, new Path(outputpath));

            job.setMapperClass(CalculateSogouMapper.class);
            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(BooleanWritable.class);

            job.setNumReduceTasks(0);

            return job;
        }
    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new CalculateSogouDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
