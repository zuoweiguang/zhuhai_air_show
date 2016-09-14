package com.navinfo.mapspotter.process.topic.roaddetect;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;

/**
 * Created by cuiliang on 2016/1/11.
 * 清理指定列的hbase中的数据的MR
 */
public class ClearDataMR {

    public static class MyMapper extends TableMapper<ImmutableBytesWritable, Delete> {
        String column = null;
        String family = null;

        public void setup(Context context) {
            try {
                column = context.getConfiguration().get("column");
                family = context.getConfiguration().get("family");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //        public void map(ImmutableBytesWritable rowkey, Result result,
//                        Context context) throws IOException, InterruptedException {
//            String rowKey = Bytes.toString(result.getRow());
//
//            boolean qualifierExist = false;
//            boolean sourceFamilyExist = false;
//            boolean detectFamilyExist = false;
//
//
//            for (Cell cell : result.rawCells()) {
//                String family = new String(CellUtil.cloneFamily(cell), "UTF-8");
//                String qualifier = new String(CellUtil.cloneQualifier(cell), "UTF-8");
//
//                if(type.equals("all")){
//                    if(family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY)){
//                        sourceFamilyExist = true;
//                    }
//                    if(family.equals(Constants.ROAD_DETECT_DETECT_FAMILY)){
//                        detectFamilyExist = true;
//                    }
//                }
//                else{
//                    if(type.equals(family) && family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY)){
//                        sourceFamilyExist = true;
//                    }
//                    if(type.equals(family) && family.equals(Constants.ROAD_DETECT_DETECT_FAMILY)){
//                        detectFamilyExist = true;
//                    }
//                }
//                if (qualifier.equals(source)) {
//                    qualifierExist = true;
//                }
//
//            }
//            if (qualifierExist) {
//                Delete delete = new Delete(Bytes.toBytes(rowKey));
//                if(sourceFamilyExist){
//                    delete.addColumn(Bytes.toBytes(Constants.ROAD_DETECT_SOURCE_FAMILY), Bytes.toBytes(source));
//                }
//                if(detectFamilyExist){
//                    delete.addColumn(Bytes.toBytes(Constants.ROAD_DETECT_DETECT_FAMILY), Bytes.toBytes(source));
//                }
//
//                context.write(rowkey, delete);
//            }
//        }
//    }
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());
            for (Cell cell : result.rawCells()) {

                String colfamily = new String(CellUtil.cloneFamily(cell), "UTF-8");
                String qualifier = new String(CellUtil.cloneQualifier(cell), "UTF-8");

                if (qualifier.equals(column) && colfamily.equals(family)) {
                    Delete delete = new Delete(Bytes.toBytes(rowKey));
                    delete.addColumn(Bytes.toBytes(family),
                            Bytes.toBytes(column));
                    context.write(rowkey, delete);
                }
            }

        }
    }

    public static void main(String[] args) throws Exception {
        String table_name = args[0];
        String column = args[1];
        String family = args[2];

        Configuration conf = new Configuration();
        conf.set("source", column);
        conf.set("family", family);

        Job job = Job.getInstance(conf, "ClearHBaseColumn");
        job.setJarByClass(ClearDataMR.class);
        Scan scan = new Scan();
        scan.addColumn(family.getBytes(), column.getBytes());

        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, ClearDataMR.MyMapper.class, ImmutableBytesWritable.class,
                Delete.class, job);
        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE,
                table_name);
        job.setOutputKeyClass(ImmutableBytesWritable.class);
        job.setOutputValueClass(Writable.class);
        job.setNumReduceTasks(0);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
