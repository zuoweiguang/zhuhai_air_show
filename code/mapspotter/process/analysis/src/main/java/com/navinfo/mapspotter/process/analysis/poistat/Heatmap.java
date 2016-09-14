package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Created by gaojian on 2016/2/20.
 */
public class Heatmap {
    private static final Logger logger = Logger.getLogger(Heatmap.class);

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

        Job job = Job.getInstance(conf, "Heatmap");
        job.setJarByClass(Heatmap.class);
        job.setMapperClass(HeatmapMapper.class);
        job.setReducerClass(HeatmapReducer.class);
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
