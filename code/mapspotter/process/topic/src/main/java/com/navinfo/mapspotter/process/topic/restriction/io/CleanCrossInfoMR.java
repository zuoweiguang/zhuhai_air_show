package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.restriction.Link;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.geojson.Feature;

import java.io.IOException;
import java.util.*;

/**
 * 清洗导出的路口数据
 * Created by SongHuiXing on 2016/4/11.
 */
public class CleanCrossInfoMR {
    public static class CleanInfoMapper
            extends Mapper<LongWritable, Text, LongWritable, Text> {

        private LongWritable outkey = new LongWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            Map.Entry<Long, Long> ids = BaseCrossJsonModel.getParentCrossId(value.toString());

            if(-1 != ids.getValue()){
                outkey.set(ids.getValue());
                context.write(outkey, value);
            }

            outkey.set(ids.getKey());
            context.write(outkey, value);
        }
    }

    public static class CleanInfoReducer
            extends Reducer<LongWritable, Text, NullWritable, Text>{

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private Text outvalue = new Text();

        private Logger logger = Logger.getLogger(CleanInfoReducer.class);

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            BaseCrossJsonModel parent = null;
            ArrayList<BaseCrossJsonModel> children = new ArrayList<>();

            long mainPid = key.get();

            for (Text v : values){
                BaseCrossJsonModel cross = BaseCrossJsonModel.readCrossJson(v.toString());

                if(cross.getPID() == mainPid){
                    parent = cross;
                } else {
                    children.add(cross);
                }
            }

            if(null == parent){
                logger.error(String.format("Can't find parent cross %d", key.get()));
                return;
            }

            List<Link> links = Link.convert(jsonUtil.readCollection(parent.getLinks(), Feature.class));

            HashMap<Integer, Integer> linkId2Index = new HashMap<>(links.size());
            for (int i = 0; i < links.size(); i++) {
                linkId2Index.put(links.get(i).getPid(), i+1);
            }

            for (BaseCrossJsonModel child : children){
                long[] childForbidden = child.getForbiddenUTurn();

                int inIndex=0, outIndex = 0;
                if(!linkId2Index.containsKey(childForbidden[1]) ||
                    !linkId2Index.containsKey(childForbidden[0])){
                    logger.error(String.format("Can't find parent link index for %d", child.getPID()));
                    continue;
                }

                parent.addChild(child.getPID(),
                                linkId2Index.get(childForbidden[1]),
                                linkId2Index.get(childForbidden[0]));
            }

            outvalue.set(jsonUtil.write2String(parent));
            context.write(NullWritable.get(), outvalue);
        }
    }

    public static class CleanInfoDriver
            extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(2 > args.length){
                System.err.printf("Usage: %s <input> <output>\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            Job job = getJob(conf, args[0], args[1]);

            int res = job.waitForCompletion(true) ? 0 : 1;

            return res;
        }

        public static Job getJob(Configuration conf, String inputPath, String outputPath)
                throws Exception{

            Job job = Job.getInstance(conf, "Clean Cross informations");

            job.setJarByClass(CleanInfoDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            job.setMapOutputKeyClass(LongWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(CleanInfoMapper.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(CleanInfoReducer.class);

            job.setNumReduceTasks(10);

            return job;
        }
    }

    public void azkabanRun(Properties props) throws Exception {
        String[] args = new String[2];

        args[0] = props.getProperty(RestrictionConfig.CROSS_DATAPATH);
        args[1] = props.getProperty(RestrictionConfig.CLEANCROSS_DATAPATH);

        System.exit(ToolRunner.run(new CleanInfoDriver(), args));
    }

    public static void main( String[] args )
            throws Exception {
        System.exit(ToolRunner.run(new CleanInfoDriver(), args));
    }
}
