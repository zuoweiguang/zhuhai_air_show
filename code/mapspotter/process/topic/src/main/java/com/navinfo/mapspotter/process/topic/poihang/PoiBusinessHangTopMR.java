package com.navinfo.mapspotter.process.topic.poihang;

import java.util.ArrayList;
import java.util.List;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
/**
 * Created by cuiliang on 2016/2/20.
 */
public class PoiBusinessHangTopMR {



    private static List<String> split(String s, String t) {
        List<String> list = new ArrayList<String>();
        if (s == null || s.length() == 0)
            return list;
        while (s.length() > 0) {
            int post = s.indexOf(t);
            if (post == -1) {
                list.add(s);
                break;
            } else {
                list.add(s.substring(0, s.indexOf(t)));
                s = s.substring(s.indexOf(t) + t.length(), s.length());
            }
        }
        return list;
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        String cpname = args[0];
        String cpdate = args[1];
        Configuration conf = new Configuration();
        Job job = new Job(conf,PoiBusinessHangTopMR.class.getName());
        job.setJarByClass(PoiBusinessHangTopMR.class);
        job.setMapperClass(PoiBusinessHangTopMR.MyMapper.class);
        job.setReducerClass(PoiBusinessHangTopMR.MyReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path("hdfs://Master.Hadoop:9000/mapreduce/workspace/" + cpname + "/" + cpdate + "/businessHang/output"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://Master.Hadoop:9000/mapreduce/workspace/" + cpname + "/" + cpdate + "/businessHang/outputTop"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

    public static class MyMapper extends Mapper<LongWritable, Text, Text, Text>{

        protected void map(LongWritable key, Text value, Context context) throws java.io.IOException ,InterruptedException {
            List<String> s = split(value.toString(), Constants.MR_MAIN_SEPARATOR);
            String uuid = "";
            if(s.get(0).equals("1b")){
                uuid = s.get(2);
                context.write(new Text(uuid), value);
            }
            else{
                uuid = s.get(2);
                context.write(new Text(uuid), value);
            }

        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text,Text> {

        protected void reduce(Text key, java.lang.Iterable<Text> values, Context context) throws java.io.IOException ,InterruptedException {
            double similarity = 0;
            String valueTemp = "";
            for(Text value : values){
                List<String> s = split(value.toString(), Constants.MR_MAIN_SEPARATOR);
                if(Double.valueOf(s.get(s.size() - 1)) >= similarity){
                    similarity = Double.valueOf(s.get(s.size() - 1));
                    String tab = "\t";

                    valueTemp = s.get(0).substring(0,1) + tab + s.get(3);
                }
            }

            if(valueTemp != null && !valueTemp.equals("")){
                context.write(new Text(valueTemp), new Text(""));
            }
        }

    }

    public static Job poiBusinessHangTopMRJob(String[] args) throws Exception{
        String cpDate = args[0];
        String busenessOutput = args[1];
        String busenessOutput1 = args[2];
        Configuration confBusinessHangTop = new Configuration();
        Job jobBusinessHangTop = new Job(confBusinessHangTop,"5.1-PoiBusinessHangTopMR_datax_"+cpDate);
        jobBusinessHangTop.setJarByClass(PoiBusinessHangTopMR.class);
        jobBusinessHangTop.setMapperClass(PoiBusinessHangTopMR.MyMapper.class);
        jobBusinessHangTop.setReducerClass(PoiBusinessHangTopMR.MyReducer.class);
        jobBusinessHangTop.setMapOutputKeyClass(Text.class);
        jobBusinessHangTop.setMapOutputValueClass(Text.class);
        FileInputFormat.addInputPath(jobBusinessHangTop, new Path(busenessOutput));
        FileOutputFormat.setOutputPath(jobBusinessHangTop, new Path(busenessOutput1));
        return jobBusinessHangTop;
    }


}
