package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by zhangjin1207 on 2016/5/27.
 */
public class ConstructioncombinMR {
    public static class  ConstructioncombinDriver extends Configured implements Tool{
        private final Logger logger = Logger.getLogger(ConstructioncombinDriver.class);
        @Override
        public int run(String[] args) throws Exception{
            if (args.length != 2){
                logger.error("Param input error!");
                return 1;
            }
            String input = args[0];
            String output = args[1];
            Configuration conf = getConf();
            Job job = Job.getInstance(conf , "ConstructiondetectMR");
            job.setJarByClass(ConstructioncombinDriver.class);
            FileInputFormat.addInputPath(job , new Path(input));
            FileOutputFormat.setOutputPath(job , new Path(output));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(ConstructioncombinMapper.class);

            if (Hdfs.deleteIfExists(conf, output)) {
                System.out.println("存在此输出路径，已删除！！！");
            }

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(ConstructioncombinReducer.class);
            job.setNumReduceTasks(1);
            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static class ConstructioncombinMapper extends Mapper<LongWritable , Text , Text , Text> {
        @Override
        public void map(LongWritable key , Text value , Context context) throws IOException , InterruptedException{

            String val = value.toString();
            ConstructionResultInfo resultInfo = ConstructionResultInfo.GetInfo(val);
            if (resultInfo == null){
                return;
            }

            context.write(new Text(String.valueOf(resultInfo.getLink_pid())) , value);
        }
    }

    public static class ConstructioncombinReducer extends Reducer<Text , Text , NullWritable, Text>{
        @Override
        public void reduce(Text key , Iterable<Text> values , Context context) throws IOException , InterruptedException{

            ConstructionResultInfo resultInfo = new ConstructionResultInfo();
            int count = 0 ;
            for (Text val : values){
                String value = val.toString();
                ConstructionResultInfo info = ConstructionResultInfo.GetInfo(value);
                count++;
                resultInfo.setLink_pid(info.getLink_pid());
                resultInfo.setLink_pn(resultInfo.getLink_pn() + info.getLink_pn());
                resultInfo.setFar_link_pn(resultInfo.getFar_link_pn() + info.getFar_link_pn());
                resultInfo.setFar_track_pn(resultInfo.getFar_track_pn() + info.getFar_track_pn());
                resultInfo.setNear_link_pn(resultInfo.getNear_link_pn() + info.getNear_link_pn());
                resultInfo.setNear_track_pn(resultInfo.getNear_track_pn() + info.getNear_track_pn());
                resultInfo.setLink_indensity(resultInfo.getLink_indensity() + info.getLink_indensity());
                resultInfo.setFar_link_indensity(resultInfo.getFar_link_indensity() + info.getFar_link_indensity());
                resultInfo.setNear_link_indensity(resultInfo.getNear_link_indensity() + info.getNear_link_indensity());
                resultInfo.setTrack_pn(resultInfo.getTrack_pn() + info.getTrack_pn());
                resultInfo.setTile_indensity(resultInfo.getTile_indensity() + info.getTile_indensity());
                resultInfo.setWeight(resultInfo.getWeight() * info.getWeight());
                resultInfo.setTile(info.getTile());
            }

            resultInfo.setTile_indensity(resultInfo.getTile_indensity() / count);

            context.write(NullWritable.get() , new Text(resultInfo.toString()));
        }
    }

    public static void main(String[] args) throws Exception{
        System.exit(ToolRunner.run(new ConstructioncombinDriver() , args));
    }

    public void azkabanRun(String input , String output) throws Exception{
        ToolRunner.run(new ConstructioncombinDriver() , new String[]{input , output});
    }
}
