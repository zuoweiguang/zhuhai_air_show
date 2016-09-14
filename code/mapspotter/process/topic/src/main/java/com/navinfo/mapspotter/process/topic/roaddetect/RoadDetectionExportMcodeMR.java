package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
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

import java.io.IOException;

/**
 * Created by cuiliang on 2016/2/24.
 */
public class RoadDetectionExportMcodeMR {

    public static class RoadDetectionExportMcodeMapper extends TableMapper<Text, Text> {
        public static final Logger logger = Logger.getLogger(RoadDetectionExportMcodeMapper.class);
        private String family = "";
        private String source = "";

        @Override
        public void setup(Context context) {
            family = context.getConfiguration().get("family");
            source = context.getConfiguration().get("source");
            logger.info("==================family:"+family+";source:"+source+"==================");
        }

        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());

            for (Cell cell : result.rawCells()) {
                String cell_family = new String(CellUtil.cloneFamily(cell));
                String cell_qualifier = new String(CellUtil.cloneQualifier(cell));

                if (cell_family.equals(family) && cell_qualifier.equals(source)) {
                    context.write(new Text(StringUtil.reverse(rowKey)), new Text(""));
                }
            }

        }
    }

    public static class RoadDetectionExportMcodeReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text val : values) {
                context.write(key, val);
            }
        }
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        String table_name = args[0];
        String family = args[1];
        String source = args[2];
        String output = args[3];
        conf.set("family", family);
        conf.set("source", source);
        Job job = Job.getInstance(conf, "NewRoadDetectionExportMR");
        job.setJarByClass(RoadDetectionExportMcodeMR.class);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(false);

        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, RoadDetectionExportMcodeMR.RoadDetectionExportMcodeMapper.class, Text.class,
                Text.class, job);

        job.setReducerClass(RoadDetectionExportMcodeMR.RoadDetectionExportMcodeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileOutputFormat.setOutputPath(job, new Path(output));
        boolean b = job.waitForCompletion(true);
        System.out.println("result flag : " + b);
    }

    public static void azkabanRun(String table_name, String family, String source, String output) throws Exception {
        System.out.println("table_name = " + table_name);
        System.out.println("family = " + family);
        System.out.println("source = " + source);
        System.out.println("output = " + output);
        Configuration conf = new Configuration();
        conf.set("family", family);
        conf.set("source", source);
        Job job = Job.getInstance(conf, "NewRoadDetectionExportMR");
        job.setJarByClass(RoadDetectionExportMcodeMR.class);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(false);

        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, RoadDetectionExportMcodeMR.RoadDetectionExportMcodeMapper.class, Text.class,
                Text.class, job);

        job.setReducerClass(RoadDetectionExportMcodeMR.RoadDetectionExportMcodeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileOutputFormat.setOutputPath(job, new Path(output));
        boolean b = job.waitForCompletion(true);
        System.out.println("result flag : " + b);
    }
}
