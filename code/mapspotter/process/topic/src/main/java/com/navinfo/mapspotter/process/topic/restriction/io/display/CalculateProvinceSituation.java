package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
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
import java.util.HashMap;

/**
 * 统计省级的结果
 * Created by SongHuiXing on 2016/3/17.
 */
public class CalculateProvinceSituation {
    public static class CalculateProvinceMapper
            extends Mapper<LongWritable, Text, IntWritable, Text> {

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private IntWritable outkey = new IntWritable();

        private Text outvalue = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            RestricCross cross = jsonUtil.readValue(value.toString(), RestricCross.class);

            String countStr = String.format("%d,%d", cross.getDelCount(), cross.getNewCount());

            outkey.set(cross.getProvinceId());
            outvalue.set(countStr);

            context.write(outkey, outvalue);
        }
    }

    public static class CalculateProvinceReducer
            extends Reducer<IntWritable, Text, NullWritable, Text> {

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private Text outvalue = new Text();

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int delCount = 0, newCount = 0;

            for (Text t : values){
                String[] counts = t.toString().split(",");

                delCount += Integer.parseInt(counts[0]);
                newCount += Integer.parseInt(counts[1]);
            }

            HashMap<String,Integer> province = new HashMap<>();

            province.put("provinceid", key.get());
            province.put("newcount", newCount);
            province.put("delcount", delCount);
            province.put("totalcount", newCount+delCount);

            outvalue.set(jsonUtil.write2String(province));

            context.write(NullWritable.get(), outvalue);
        }
    }

    public static class ProvinceDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if (2 > args.length) {
                System.err.printf("Usage: %s [generic option] <output> <commaSeperatedPaths>\n", getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            Job job = Job.getInstance(conf, "Calculate province situations");
            job.setJarByClass(getClass());

            FileInputFormat.addInputPaths(job, args[1]);
            FileOutputFormat.setOutputPath(job, new Path(args[0]));

            job.setMapOutputKeyClass(IntWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(CalculateProvinceMapper.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(CalculateProvinceReducer.class);

            job.setNumReduceTasks(1);

            return job.waitForCompletion(true) ? 0 : 1;
        }

    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new ProvinceDriver(), args));
    }
}
