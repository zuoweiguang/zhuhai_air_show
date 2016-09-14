package com.navinfo.mapspotter.process.topic.construction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by ZhangJin1207 on 2016/3/3.
 */
public class FilterConstructionLinkMR {
    public static class FilterConstructionLinkMapper extends Mapper<LongWritable , Text , NullWritable , Text>{

        @Override
        public void map(LongWritable key , Text value , Context context) throws IOException , InterruptedException {
            String sValue = value.toString();
            if (sValue == null || sValue.isEmpty()){
                return;
            }

            BaseRoadModle baseRoadModle = BaseRoadModle.PraseJsonStr(sValue);

            if (baseRoadModle.getKind() == 1){
                context.write(null ,value);
            }
        }
    }

    public static class FilterConstrutionLinkDriver extends Configured implements Tool{
        @Override
        public int run(String[] args) throws Exception{
            if (args.length != 2){
                System.err.printf("Usage: %s [generic option] <input> <output>\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
                return 1;
            }

            Configuration conf = getConf();
            Job job = Job.getInstance(conf , "FilterConstructionLink");
            job.setJarByClass(FilterConstrutionLinkDriver.class);
            FileInputFormat.addInputPath(job , new Path(args[0]));
            FileOutputFormat.setOutputPath(job , new Path(args[1]));

            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(FilterConstructionLinkMapper.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setNumReduceTasks(0);
            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception{
        System.exit(ToolRunner.run(new FilterConstrutionLinkDriver() , args));
    }
}
