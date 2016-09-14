package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 按区域统计流程
 *
 * Created by gaojian on 2016/2/2.
 */
public class AreaCount {
    private static final Logger logger = Logger.getLogger(AreaCount.class);

    public static void main(String[] args) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        if (args.length != 4) {
            logger.error("Args is wrong.");
            return;
        }
//        String inputPath = args[0];
//        String outPath = args[1];
//        long split = Long.parseLong(args[2]);
//
//        Configuration conf = new Configuration();
//
//        Job job = Job.getInstance(conf, "AreaCount");
//        job.setJarByClass(AreaCount.class);
//        job.setMapperClass(AreaCountPOIMapper.class);
//        job.setReducerClass(AreaCountReducer.class);
//        job.setOutputKeyClass(NullWritable.class);
//        job.setOutputValueClass(Text.class);
//        job.setMapOutputKeyClass(Text.class);
//        job.setMapOutputValueClass(IntWritable.class);
//        job.setNumReduceTasks(1);
//
//        FileInputFormat.addInputPath(job, new Path(inputPath));
//        // 计算分割文件大小，控制map个数
//        long totalSize = Hdfs.getFileSize(conf, inputPath);
//        long splitSize = totalSize / split;
//        FileInputFormat.setMaxInputSplitSize(job, splitSize);
//
//        FileOutputFormat.setOutputPath(job, new Path(outPath));

        String table = args[0];
        String family = args[1];
        String qualifier = args[2];
        String outPath = args[3];

        Configuration conf = new Configuration();
        conf.set("family", family);
        conf.set("qualifier", qualifier);

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapreduce.map.speculative", false);

        Job job = Job.getInstance(conf, "AreaCountTrack");

        job.setJarByClass(AreaCount.class);

        Scan scan = new Scan();
        // 2015-10-16新增
        scan.setCacheBlocks(false);
        scan.setCaching(10);
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));

        TableMapReduceUtil.initTableMapperJob(table, scan,
                AreaCountTrackMapper.class,
                Text.class, IntWritable.class, job);

        job.setReducerClass(AreaCountReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileOutputFormat.setOutputPath(job, new Path(outPath));

        job.waitForCompletion(true);
        System.out.println("Total time : "
                + (System.currentTimeMillis() - currentTimeMillis));
    }
}
