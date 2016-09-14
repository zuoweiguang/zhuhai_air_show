package com.navinfo.mapspotter.process.convert.road.transfer;

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
import org.apache.hadoop.mapreduce.Mapper;

public class ClearDataMR {
    public static void main(String[] args)
            throws Exception {
        String source = args[0];
        String type = args[1];
        String tablename = args[2];

        Configuration conf = new Configuration();
        conf.set("source", source);
        conf.set("type", type);
        conf.set("hbase.zookeeper.quorum","datanode01:2181,datanode02:2181,datanode03:2181");

        Job job = Job.getInstance(conf, "ClearHBaseColumn");
        job.setJarByClass(ClearDataMR.class);
        Scan scan = new Scan();
        scan.addColumn("source".getBytes(), source.getBytes());
        scan.addColumn("detect".getBytes(), source.getBytes());

        TableMapReduceUtil.initTableMapperJob(tablename, scan, MyMapper.class, ImmutableBytesWritable.class, Delete.class, job);

        job.setOutputFormatClass(TableOutputFormat.class);
        job.getConfiguration().set("hbase.mapred.outputtable", tablename);

        job.setOutputKeyClass(ImmutableBytesWritable.class);
        job.setOutputValueClass(Writable.class);
        job.setNumReduceTasks(0);
        System.exit((job.waitForCompletion(true)) ? 0 : 1);
    }

    public static class MyMapper extends TableMapper<ImmutableBytesWritable, Delete> {
        String source;
        String type;

        public MyMapper() {
            this.source = null;
            this.type = null;
        }

        public void setup(Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Delete>.Context context) {
            try {
                this.source = context.getConfiguration().get("source");
                this.type = context.getConfiguration().get("type");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void map(ImmutableBytesWritable rowkey, Result result, Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Delete>.Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());

            boolean qualifierExist = false;
            boolean sourceFamilyExist = false;
            boolean detectFamilyExist = false;

            Cell[] arr$ = result.rawCells();
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; ++i$) {
                Cell cell = arr$[i$];
                String family = new String(CellUtil.cloneFamily(cell), "UTF-8");
                String qualifier = new String(CellUtil.cloneQualifier(cell), "UTF-8");

                if (this.type.equals("all")) {
                    if (family.equals("source"))
                        sourceFamilyExist = true;

                    if (family.equals("detect"))
                        detectFamilyExist = true;
                } else {
                    if ((this.type.equals(family)) && (family.equals("source")))
                        sourceFamilyExist = true;

                    if ((this.type.equals(family)) && (family.equals("detect")))
                        detectFamilyExist = true;
                }

                if (qualifier.equals(this.source))
                    qualifierExist = true;

            }

            if (qualifierExist) {
                Delete delete = new Delete(Bytes.toBytes(rowKey));
                if (sourceFamilyExist)
                    delete.addColumn(Bytes.toBytes("source"), Bytes.toBytes(this.source));

                if (detectFamilyExist) {
                    delete.addColumn(Bytes.toBytes("detect"), Bytes.toBytes(this.source));
                }

                context.write(rowkey, delete);
            }
        }
    }
}