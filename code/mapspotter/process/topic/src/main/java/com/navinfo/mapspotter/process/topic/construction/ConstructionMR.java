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
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ZhangJin1207 on 2016/5/14.
 */
public class ConstructionMR {
    Logger logger = Logger.getLogger(ConstructionMR.class);
    enum AnalysisCounter{
        InvalidData,
        IgnoreData,
    }

    public static class ConstructionMapper extends Mapper<LongWritable , Text , Text , Text>{
        @Override
        public void setup(Context context) throws IOException , InterruptedException{

        }

        @Override
        public void map(LongWritable key , Text value , Context context) throws IOException , InterruptedException {
            String svalue = value.toString();
            if (toString().isEmpty()){
                return;
            }

            ConstructionRoadModle constructionRoadModle = ConstructionRoadModle.prase(svalue);

            if (constructionRoadModle == null){
                context.getCounter(AnalysisCounter.InvalidData).increment(1);
                return;
            }
            String slinkpid = constructionRoadModle.getLinkpid();
            for (String mcode : constructionRoadModle.getMcodslist()){
                context.write(new Text(mcode) , new Text(slinkpid));
            }
        }
    }

    public static class ConstructionReducer extends Reducer<Text , Text , NullWritable , Text> {

        ConstrctionHBaseInfo constrctionHBaseInfo = new ConstrctionHBaseInfo();
        int filter_value = 0;
        @Override
        public  void setup(Context context) throws IOException , InterruptedException{
            String HBaseHost = context.getConfiguration().get("HBASEHOST");
            String roadTableName = context.getConfiguration().get("ROADTABLENAME");
            String trackTableName = context.getConfiguration().get("TRACKTABLENAME");
            String RoadFamily = context.getConfiguration().get("ROADFAMILY");
            String RoadQualifier = context.getConfiguration().get("ROADQUALIFIER");
            String TrackFamily = context.getConfiguration().get("TRACKFAMILY");
            String TrackQualifier = context.getConfiguration().get("TRACKQUALIFIER");
            String filtervalue = context.getConfiguration().get("FILTER_VALUE");
            filter_value = Integer.parseInt(filtervalue);
            constrctionHBaseInfo.PrepareHBase(HBaseHost , roadTableName , trackTableName , RoadFamily , RoadQualifier , TrackFamily , TrackQualifier);
        }

        @Override
        public void reduce(Text key , Iterable<Text> values, Context context) throws IOException , InterruptedException{
            String mcode = key.toString();
            List<Integer> links = new ArrayList<>();

            for (Text val : values){
                links.add(Integer.parseInt(val.toString()));
            }

            RoadAnalysisInfo info = constrctionHBaseInfo.GetInfo(mcode , links , filter_value);
            List<ConstructionResultInfo> list = info.getTilelinks();
            for (ConstructionResultInfo rinfo : list){
                String result = rinfo.ConvertJsonStr();
                context.write(NullWritable.get() , new Text(result));
            }
        }
    }

    public static class ConstructionDriver extends Configured implements Tool {
        @Override
        public int run(String[] args) throws Exception {
            String input = args[0];
            String output = args[1];
            String HBaseHost = args[2];
            String roadTabName = args[3];
            String trackTabName = args[4];
            String RoadFamily = args[5];
            String RoadQualifier = args[6];
            String TrackFamily = args[7];
            String TrackQualifier = args[8];
            String filter_value = args[9];
            int reducenum = Integer.parseInt(args[10]);

            Configuration conf = getConf();
            conf.set("HBASEHOST" , HBaseHost);
            conf.set("ROADTABLENAME" , roadTabName);
            conf.set("TRACKTABLENAME" , trackTabName);
            conf.set("ROADFAMILY",RoadFamily);
            conf.set("ROADQUALIFIER",RoadQualifier);
            conf.set("TRACKFAMILY",TrackFamily);
            conf.set("TRACKQUALIFIER",TrackQualifier);
            conf.set("FILTER_VALUE" , filter_value);

            Job job = Job.getInstance(conf , "ConstructionMR");
            job.setJarByClass(ConstructionDriver.class);
            FileInputFormat.addInputPath(job , new Path(input));
            FileOutputFormat.setOutputPath(job , new Path(output));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(ConstructionMapper.class);

            //long totalSize = Hdfs.getFileSize(conf, input);
            //long splitSize = totalSize / split;
            //FileInputFormat.setMaxInputSplitSize(job, splitSize);

            if (Hdfs.deleteIfExists(conf, output)) {
                System.out.println("存在此输出路径，已删除！！！");
            }

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(ConstructionReducer.class);
            job.setNumReduceTasks(reducenum);
            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception{
        System.exit(ToolRunner.run(new ConstructionDriver() , args));
    }

    /**
     *
     * @param inputpath      数据输入
     * @param outputpath     结果输出
     * @param hbasehost      zookeeper
     * @param roadtable      hbase roadtable
     * @param tracktable     hbase tracktable
     * @param roadfamily     基础路网  列族
     * @param roadqualifier  基础路网  列
     * @param trackfamily    轨迹      列族
     * @param trackqualifier 轨迹      列
     * @param filter_value   轨迹过滤点阀值
     * @param split          reduce 个数
     * @throws Exception
     */
    public void azkabanRun(String inputpath , String outputpath , String hbasehost , String roadtable , String tracktable , String roadfamily , String roadqualifier , String trackfamily , String trackqualifier , String filter_value , String split) throws Exception{
        ToolRunner.run(new ConstructionDriver() , new  String[]{inputpath , outputpath , hbasehost , roadtable , tracktable , roadfamily , roadqualifier , trackfamily , trackqualifier ,filter_value , split});
    }
}
