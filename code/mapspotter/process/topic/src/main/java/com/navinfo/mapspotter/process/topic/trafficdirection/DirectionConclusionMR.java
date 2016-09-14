package com.navinfo.mapspotter.process.topic.trafficdirection;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.Redis;
import com.navinfo.mapspotter.foundation.util.Logger;
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
 * Created by gaojian on 2016/4/14.
 */
public class DirectionConclusionMR {
    private static final Logger logger = Logger.getLogger(TrackMatchMR.class);

    public static class DirectionConclusionMapper extends Mapper<LongWritable, Text, Text, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split("\t");

            context.write(new Text(line[0]), new Text(line[1] + " " + line[2]));
        }
    }

    public static class DirectionConclusionReducer extends Reducer<Text, Text, Text, Text> {
        private Redis redis = null;
        private int minsum = 0; //双向最少打点次数
        private int sratio = 0; //双变单打点数比例
        private int dratio = 0; //单边双打点数比例

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            redis = (Redis) DataSource.getDataSource(IOUtil.makeRedisParam(
                    "192.168.4.128", 6379
            ));

            minsum = Integer.parseInt(context.getConfiguration().get("minsum"));
            sratio = Integer.parseInt(context.getConfiguration().get("sratio"));
            dratio = Integer.parseInt(context.getConfiguration().get("dratio"));
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            redis.close();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String linkpid = key.toString();
            int posNum = 0;
            int negNum = 0;

            for (Text value : values) {
                String[] num = value.toString().split(" ");
                posNum += Integer.parseInt(num[0]);
                negNum += Integer.parseInt(num[1]);
            }

            if (posNum + negNum < minsum) { //200
                return;
            }

            int direction;
            if (posNum == 0) {
                direction = 3;
            } else {
                double ratio = (double) negNum / (double) posNum;
                if (ratio > sratio) { // 20
                    direction = 3;
                } else if (ratio < 1.0 / sratio) {
                    direction = 2;
                } else if (ratio > 1.0 / dratio && ratio < dratio) { // 2
                    direction = 1;
                } else {
                    direction = 0;
                }
            }

            String oldD = redis.queryString(linkpid);
            if (oldD == null || oldD.isEmpty()){
                logger.error("-----" + linkpid + "is not in redis!");
            }
            else{
                int old = Integer.parseInt(oldD);
                if ((old <= 1 && direction > 1) || (old > 1 && direction != 0 && old != direction)) {
                    context.write(new Text(linkpid), new Text(old + "->" + direction));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String inputPath = args[0];
        String outPath = args[1];
//        long split = Long.parseLong(args[2]);
        String minsum = args[2];
        String sratio = args[3];
        String dradio = args[4];
        Configuration conf = new Configuration();
        conf.set("minsum" , minsum);
        conf.set("sratio" , sratio);
        conf.set("dratio" , dradio);

        Job job = Job.getInstance(conf, "DirectionConclusionMR");
        job.setJarByClass(DirectionConclusionMR.class);
        job.setMapperClass(DirectionConclusionMapper.class);
        job.setReducerClass(DirectionConclusionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        // 计算分割文件大小，控制map个数
//        long totalSize = Hdfs.getFileSize(conf, inputPath);
//        long splitSize = totalSize / split;
//        FileInputFormat.setMaxInputSplitSize(job, splitSize);

        if (Hdfs.deleteIfExists(conf, outPath)) {
            System.out.println("存在此输出路径，已删除！！！");
        }
        FileOutputFormat.setOutputPath(job, new Path(outPath));

        job.waitForCompletion(true);
    }
}
