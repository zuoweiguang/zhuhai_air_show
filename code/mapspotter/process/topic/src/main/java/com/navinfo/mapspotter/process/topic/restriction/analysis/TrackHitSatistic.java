package com.navinfo.mapspotter.process.topic.restriction.analysis;

import com.navinfo.mapspotter.foundation.algorithm.string.SimpleCountCluster;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by SongHuiXing on 2016/3/24.
 */
public class TrackHitSatistic {

    enum SatisticType {
        EmptyTrack,
        Filter2Empty,
    }

    public static class TrackSatisticMapper
            extends Mapper<LongWritable, Text, LongWritable, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String hitResultLine = value.toString().trim();
            if(hitResultLine.isEmpty())
                return;

            String[] hitResultArray = hitResultLine.split("\t");
            if(2 != hitResultArray.length)
                return;

            long crossPid = Long.parseLong(hitResultArray[0]);

            String[] trackhitsLine = hitResultArray[1].split("#");

            short[] trackhits = JsonUtil.getInstance().readShortArray(trackhitsLine[1]);
            if(0 == trackhits.length) {
                context.getCounter(SatisticType.EmptyTrack).increment(1);
                return;
            }

            SimpleCountCluster encodeUtil = new SimpleCountCluster(255);
            for(short hit : trackhits){
                encodeUtil.insertHit(hit);
            }

            String simpleTrack = encodeUtil.getSimpleTrack();
            //String simpleTrack = encodeUtil.get2DRunningCode();
            if(simpleTrack.isEmpty()) {
                context.getCounter(SatisticType.Filter2Empty).increment(1);
                return;
            }

            context.write(new LongWritable(crossPid), new Text(simpleTrack));
        }

    }

    public static class TrackSatisticDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if (args.length < 2) {
                System.err.printf("Usage: %s [generic option] <input> <output> \n", getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            conf.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{args[0]},
                                                            FileSystem.get(conf),
                                                            "part", 20)));

            Job job = Job.getInstance(conf, "Statistic Track filter");

            job.setJarByClass(getClass());

            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            job.setMapOutputKeyClass(LongWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(TrackSatisticMapper.class);

            job.setNumReduceTasks(0);

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new TrackSatisticDriver(), args));
    }
}
