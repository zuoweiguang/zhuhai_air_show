package com.navinfo.mapspotter.process.topic.roaddetect;

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
 * Created by cuiliang on 2016/1/31.
 */
public class TestRoadDetectionExportMR {
    public static class RoadDetectionExportMapper extends TableMapper<Text, Text> {

        private String source = "";

        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
        }

        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());

            for (Cell cell : result.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));

                if (family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && qualifier.equals(source)) {
                    context.write(new Text(StringUtil.reverse(rowKey)), new Text(""));
                }
            }

        }
    }

    public static class RoadDetectionExportReducer extends Reducer<Text, Text, Text, Text> {
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
        String source = args[0];
        String output = args[1];
        conf.set("source", source);
        Job job = Job.getInstance(conf, "NewRoadDetectionExportMR");
        job.setJarByClass(TestRoadDetectionExportMR.class);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(false);

        TableMapReduceUtil.initTableMapperJob(Constants.ROAD_DETECT_TABLE,
                scan, TestRoadDetectionExportMR.RoadDetectionExportMapper.class, Text.class,
                Text.class, job);

        job.setReducerClass(TestRoadDetectionExportMR.RoadDetectionExportReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileOutputFormat.setOutputPath(job, new Path(output));
        boolean b = job.waitForCompletion(true);
        System.out.println("result flag : " + b);
    }
}
