package com.navinfo.mapspotter.process.topic.poihang;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;

import com.navinfo.mapspotter.foundation.model.oldPoiHang.BusinessPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.RecoPoi;
import com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes.Poi;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * Created by cuiliang on 2016/2/19.
 */
public class ReadPoiMR {

    public static class MyMapper extends TableMapper<Text, Text> {
        private double min_x = 180, max_x = 0, min_y = 90, max_y = 0;
        protected final Log log = LogFactory.getLog(MyMapper.class);

        @Override
        public void setup(Context context) {
            String a[] = context.getConfiguration().get("boundingBox")
                    .split(";");
            double x1, y1, x2, y2;
            try {
                x1 = Double.valueOf(a[0]);
                y1 = Double.valueOf(a[1]);
                x2 = Double.valueOf(a[2]);
                y2 = Double.valueOf(a[3]);

                if (x1 < min_x)
                    min_x = x1;
                if (y1 < min_y)
                    min_y = y1;
                if (x2 > max_x)
                    max_x = x2;
                if (y2 > max_y)
                    max_y = y2;

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        @Override
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            try {
                BusinessPoi businessPoi = new BusinessPoi();
                RecoPoi recoPoi = new RecoPoi();
                for (KeyValue kv : result.raw()) {
                    if (Bytes.toString(kv.getQualifier()).equals("attributes")) {
                        Poi poi = JSONObject.toJavaObject((JSONObject) JSONObject
                                        .toJSON(Bytes.toString(kv.getValue())),
                                Poi.class);

                        if ((this.max_x > this.min_x)
                                && (this.max_y > this.min_y)) {
                            if (Double
                                    .valueOf(poi.getLocation().getLongitude()) > this.max_x)
                                return;
                            if (Double
                                    .valueOf(poi.getLocation().getLongitude()) < this.min_x)
                                return;
                            if (Double.valueOf(poi.getLocation().getLatitude()) > this.max_y)
                                return;
                            if (Double.valueOf(poi.getLocation().getLatitude()) < this.min_y)
                                return;
                        }
                        businessPoi = Transform.makeBusinessPoi(poi);
                        recoPoi = Transform.makeRecoPoi(poi);
                    }
                }
                context.write(new Text("p" + Constants.MR_MAIN_SEPARATOR + businessPoi.toJson() + Constants.MR_MAIN_SEPARATOR + recoPoi.toJson()), new Text(""));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class MyReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text val : values) {
                context.write(key, val);
            }
        }
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        {

            Configuration conf = new Configuration();
            conf.set("boundingBox",
                    "75.2575103993498;18.22146985119227;134.29825007160898;52.97467907119413");

            Job job = new Job(conf, "rpcfMR");
            job.setJarByClass(ReadPoiMR.class);
            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);

            TableMapReduceUtil.initTableMapperJob("poi", scan, MyMapper.class,
                    Text.class, Text.class, job);
            job.setReducerClass(ReadPoiMR.MyReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setNumReduceTasks(30);
            FileOutputFormat.setOutputPath(job, new Path(
                    "hdfs://namenode01:9000/mapreduce/common/whole/readpoi/output"));
            boolean b = job.waitForCompletion(true);
        }
    }

    public static Job readPoiMRJob(String[] args) throws Exception {
        String sourceBox = args[0];
        String cpDate = args[1];
        String poiBoxOutput = args[2];
        Configuration confReadPoi = new Configuration();
        confReadPoi.set("boundingBox", sourceBox);
        Job jobReadPoi = Job.getInstance(confReadPoi, "4-ReadPoiCFMR_datax_" + cpDate);

        jobReadPoi.setJarByClass(ReadPoiMR.class);
        Scan scanPoi = new Scan();
        scanPoi.setCaching(500);
        scanPoi.setCacheBlocks(false);
        TableMapReduceUtil.initTableMapperJob("poi", scanPoi, ReadPoiMR.MyMapper.class, Text.class, Text.class, jobReadPoi);
        jobReadPoi.setReducerClass(ReadPoiMR.MyReducer.class);
        jobReadPoi.setOutputKeyClass(Text.class);
        jobReadPoi.setOutputValueClass(Text.class);
        jobReadPoi.setNumReduceTasks(30);
        FileOutputFormat.setOutputPath(jobReadPoi, new Path(poiBoxOutput));
        return jobReadPoi;
    }
}