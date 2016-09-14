package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangJin1207 on 2016/1/27.
 */
public class ConstructionAnalysisMR {

    private static final Logger logger = Logger.getLogger(ConstructionAnalysisMR.class);
    enum AnalysisCounter{
        InvalidData,
        IgnoreData,
    }
    public static class ConstructionAnalysisMapper extends Mapper<LongWritable , Text , Text , Text> {

        private double dRatio = 0;
        private int minLinkPN = 0;
        private int filter_value = 0;
        private ConstrctionHBaseInfo constrctionHBaseInfo = new ConstrctionHBaseInfo();

        @Override
        protected void setup(Context context) throws IOException , InterruptedIOException{
            String HBaseHost = context.getConfiguration().get("HBASEHOST");
            String roadTableName = context.getConfiguration().get("ROADTABLENAME");
            String trackTableName = context.getConfiguration().get("TRACKTABLENAME");
            String RoadFamily = context.getConfiguration().get("ROADFAMILY");
            String RoadQualifier = context.getConfiguration().get("ROADQUALIFIER");
            String TrackFamily = context.getConfiguration().get("TRACKFAMILY");
            String TrackQualifier = context.getConfiguration().get("TRACKQUALIFIER");
            String filtervalue = context.getConfiguration().get("FILTER_VALUE");
            String Ratio = context.getConfiguration().get("RATIO");
            String MinLinkPN = context.getConfiguration().get("MINLINKPN");
            dRatio = Double.parseDouble(Ratio);
            minLinkPN = Integer.parseInt(MinLinkPN);
            filter_value = Integer.parseInt(filtervalue);
            constrctionHBaseInfo.PrepareHBase(HBaseHost , roadTableName ,trackTableName , RoadFamily , RoadQualifier , TrackFamily , TrackQualifier);
        }
        @Override
        public void map(LongWritable key , Text value , Context context) throws IOException , InterruptedException {
            String strVal = value.toString();
            if (strVal == null || strVal.isEmpty()){
                return;
            }
            ConstructionRoadModle cmodle = ConstructionRoadModle.prase(strVal);
            if (cmodle == null){
                context.getCounter(AnalysisCounter.InvalidData).increment(1);
                return;
            }

            String outmcode = null;
            List<String> mcodes = new ArrayList<>();
            for (String mcode : cmodle.getMcodslist()){
                StringBuilder stringBuilder = new StringBuilder(mcode);
                mcodes.add(stringBuilder.reverse().toString());
                outmcode = mcode;
            }
            int pid = Integer.parseInt(cmodle.getLinkpid());

            RoadAnalysisInfo roadinfo = constrctionHBaseInfo.GetInfo(mcodes , pid , filter_value);
            String stroutmcode = outmcode + "_12";
            String strR = roadinfo.toOutput() + "\t" + stroutmcode;
            //String strRatio = String.format("%.3f",roadinfo.getdHitRatio());
            //if ((roadinfo.getLinkOverArea() <= minLinkPN && roadinfo.getdHitRatio() == 1) ||
            //        (roadinfo.getLinkOverArea() > minLinkPN && roadinfo.getdHitRatio() > dRatio)){
            //疑是开通
            //    strR = "" + strRatio + "\t" + 1 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\t" + smcodes;
            //}else if (roadinfo.getLinkOverArea() == 0){
            //基础路网数据中没有
            //    strR = "" + strRatio + "\t" + -1 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\t" + smcodes;
            //}else{
            //还处于施工中
            //    strR = "" + strRatio + "\t" + 0 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\t" + smcodes;
            //}
            context.write(new Text(cmodle.getLinkpid()) , new Text(strR));
        }
    }

    public static class ConstructionAnalysisReduce extends Reducer<Text , Text , Text , Text>{

        @Override
        protected void reduce(Text key , Iterable<Text> values, Context context) throws IOException , InterruptedException{
            for (Text val : values){
            context.write(key , val);
            }
        }
    }

    public static class ConstructionAnalysisDirver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception{
            String input = args[0];
            String output = args[1];
            String HBaseHost = args[2];
            String roadTabName = args[3];
            String trackTableName = args[4];
            String RoadFamily = args[5];
            String RoadQualifier = args[6];
            String TrackFamily = args[7];
            String TrackQualifier = args[8];
            String filter_value = args[9];
            int split = Integer.parseInt(args[10]);

            logger.info("inputpath : " + input + " outputpath : " + output + " filtervalue : " + filter_value + " spilt : "  + split);
            logger.info("zookeeperhost : " + HBaseHost + " roadtable : " + roadTabName + " roadfamily : " + RoadFamily + " roadqualifier : " + RoadQualifier + " tracktable : " + trackTableName + " trackfamily : " + TrackFamily + " trackqualifier : " + TrackQualifier);

            String Ratio = "0.666667";
            String MinLinkPN = "10";

            Configuration conf = getConf();
            conf.set("HBASEHOST" , HBaseHost);
            conf.set("ROADTABLENAME" , roadTabName);
            conf.set("TRACKTABLENAME" , trackTableName);
            conf.set("ROADFAMILY",RoadFamily);
            conf.set("ROADQUALIFIER",RoadQualifier);
            conf.set("TRACKFAMILY",TrackFamily);
            conf.set("TRACKQUALIFIER",TrackQualifier);
            conf.set("FILTER_VALUE" , filter_value);
            conf.set("RATIO",Ratio);
            conf.set("MINLINKPN" , MinLinkPN);

            Job job = Job.getInstance(conf , "ConstructionAnalysis");
            job.setJarByClass(ConstructionAnalysisDirver.class);
            FileInputFormat.addInputPath(job , new Path(input));
            FileOutputFormat.setOutputPath(job , new Path(output));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(ConstructionAnalysisMapper.class);

            long totalSize = Hdfs.getFileSize(conf, input);
            long splitSize = totalSize / split;
            FileInputFormat.setMaxInputSplitSize(job, splitSize);

            if (Hdfs.deleteIfExists(conf, output)) {
                System.out.println("存在此输出路径，已删除！！！");
            }

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(ConstructionAnalysisReduce.class);
            job.setNumReduceTasks(1);
            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception{
        logger.info(args.toString());
        System.exit(ToolRunner.run(new ConstructionAnalysisDirver() , args));
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
     * @param split          mapper 个数
     * @throws Exception
     */
    public void azkabanRun(String inputpath , String outputpath , String hbasehost , String roadtable , String tracktable , String roadfamily , String roadqualifier , String trackfamily , String trackqualifier , String filter_value , String split) throws Exception{
        ToolRunner.run(new ConstructionAnalysisDirver() , new  String[]{inputpath , outputpath , hbasehost , roadtable , tracktable , roadfamily , roadqualifier , trackfamily , trackqualifier ,filter_value , split});
    }
}
