package com.navinfo.mapspotter.process.topic.restriction.trackbuild;

import com.navinfo.mapspotter.foundation.io.CustomTextOutputFormat;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * 从GPS点log数据构建轨迹
 * Created by SongHuiXing on 2016/2/29.
 */
public class BuildTrack extends Configured implements Tool {

    enum BuildCounter{
        InvalidData,
        IgnoreData,
    }

    private Logger log = Logger.getLogger(BuildTrack.class);

    @Override
    public int run(String[] args) throws Exception {
        if (4 != args.length) {
            System.err.printf("Usage: %s <minutes> <prefix> <output> <inputs> [reducecount]\n",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
        }

        int minutes = Integer.parseInt(args[0]);

        String[] inputs = Arrays.copyOfRange(args, 3, args.length);

        int reduceCount = 20;
        if(args.length > 4){
            reduceCount = Integer.parseInt(args[4]);
        }

        Job job = getJob(getConf(), minutes, inputs, args[2], args[1], reduceCount);

        int res = job.waitForCompletion(true) ? 0 : 1;

        //针对Counter结果的显示
        Counters counters = job.getCounters();
        Counter counter1 = counters.findCounter(BuildCounter.InvalidData);
        log.info(String.format("Skiped invalid data record count: %d", counter1.getValue()));

        return res;
    }

    public static void azkabanRun(BuildTrack tracker, Properties props) throws Exception {
        String[] args = new String[5];

        args[0] = props.getProperty(RestrictionConfig.TRACK_TIME, "10");
        args[1] = props.getProperty(RestrictionConfig.SRCDATA_PREFIX, "part");
        args[2] = props.getProperty(RestrictionConfig.TRACKDATA_PATH);
        args[3] = props.getProperty(RestrictionConfig.SRCDATA_PATHS);
        args[4] = props.getProperty(RestrictionConfig.BUILDTRACK_REDUCECOUNT, "20");

        System.exit(ToolRunner.run(tracker, args));
    }

    protected Job getJob(Configuration cfg, int minutes,
                         String[] inputPaths, String outputPath,
                         String prefix, int reduceCount) throws Exception {

        cfg.setInt(RestrictionConfig.TRACK_TIME, minutes);

        Job job = Job.getInstance(cfg, "Build Car Track");
        job.setJarByClass(BuildTrack.class);

        FileSystem fileSystem = FileSystem.get(cfg);

        ArrayList<Path> files = new ArrayList<>();
        for (String input : inputPaths) {
            Hdfs.addInputPath(new Path(input), fileSystem, files, prefix);
        }

        job.setOutputFormatClass(CustomTextOutputFormat.class);

        for (Path f : files){
            FileInputFormat.addInputPath(job, f);
        }

        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(reduceCount);

        return job;
    }
}
