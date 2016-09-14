package com.navinfo.mapspotter.process.topic.roaddetect;


import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.analysis.poistat.BlocksAnalysis;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuiliang on 2016/1/7.
 */
public class ReadDidiSourceMR {
    private static final Logger logger = Logger.getLogger(ReadDidiSourceMR.class);
    private static final int level = 12;
    private static MercatorUtil mkt = new MercatorUtil(1024, level);


    public static class ReadDidiSourceMapper extends Mapper<LongWritable, Text, Text, Text> {

        private String source = "";
        private boolean isTaxi = false;

        private BlocksAnalysis analysis;

        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
            isTaxi = context.getConfiguration().getBoolean("isTaxi", false);
            analysis = new BlocksAnalysis();
            try {
                analysis.prepareMap_Json("", 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            try {
                String v = value.toString();

                String lon = v.split(",")[1];
                String lat = v.split(",")[2];
                String rowKey = mkt.lonLat2MCode(new Coordinate(
                        Double.parseDouble(lon),
                        Double.parseDouble(lat)
                ));
                String userId = v.split(",")[0];
                long timestamp = Long.parseLong(v.split(",")[3]);
                double speed = Double.parseDouble(v.split(",")[4]) * 3.6;
                double direction = Double.parseDouble(v.split(",")[5]);
                String carType = v.split(",")[9].trim();
                if (speed < 0 || speed >= 250) {
                    return;
                }
                if (direction < 0 || direction > 360) {
                    return;
                }
                if (Long.parseLong(userId) <= 0) {
                    return;
                }
                if (timestamp <= 0) {
                    return;
                }

                if (isTaxi) {
                    if (!carType.endsWith("DD_TAXI")) {
                        return;
                    }
                }

                context.write(new Text(rowKey), new Text(lon + "," + lat));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static class ReadDidiSourceReducer extends
            TableReducer<Text, Text, ImmutableBytesWritable> {
        private String source = "";
        Connection connection = null;
        private String familyName = "";
        private String tableName = "";
        private int isIncrement = 1;

        @Override
        protected void setup(
                Context context)
                throws IOException, InterruptedException {
            Configuration conf = HBaseConfiguration.create();
            connection = ConnectionFactory.createConnection(conf);
//            isIncrement = Integer.parseInt(context.getConfiguration().get(
//                    "isIncrement"));
            source = context.getConfiguration().get("source");
            familyName = context.getConfiguration().get("familyName");
            tableName = context.getConfiguration().get("tableName");
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            byte[] keyByte = StringUtil.reverse(key.toString()).getBytes();

            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(keyByte);
            Result result = table.get(get);
            byte[] value = result.getValue(familyName.getBytes(),
                    source.getBytes());
            Integer bitmap[][];

            if (isIncrement == 1) {
                if (value != null && value.length != 0) {
                    bitmap = MatrixUtil.deserializeMatrix(value, true);
                } else {
                    bitmap = new Integer[1024][1024];
                }
            } else {
                bitmap = new Integer[1024][1024];
            }

            for (Text val : values) {
                String xy = val.toString();
                double lon = Double.parseDouble(xy.split(",")[0]);
                double lat = Double.parseDouble(xy.split(",")[1]);
                IntCoordinate _pixels = mkt.lonLat2Pixels(new Coordinate(lon, lat));
                IntCoordinate _m = mkt.pixelsInTile(_pixels);

                int x = _m.x;
                int y = _m.y;
                if ((x >= 0 && x < 1024) && (y >= 0 && y < 1024)) {
                    if (null == bitmap[y][x])
                        bitmap[y][x] = 0;
                    bitmap[y][x] = bitmap[y][x] + 1;
                }
            }

            byte[] byteArray = MatrixUtil.serializeSparseMatrix(bitmap, true);
            Put put = new Put(keyByte);
            put.addColumn(familyName.getBytes(),
                    source.getBytes(), byteArray);
            context.write(new ImmutableBytesWritable(keyByte), put);

        }

        @Override
        protected void cleanup(
                Context context)
                throws IOException, InterruptedException {
            if (connection != null) {
                connection.close();
            }
        }
    }

//    /*
//     * 自定义Partitioner类
//     */
//    public static class MyPartitioner extends Partitioner<Text, Text> {
//        @Override
//        public int getPartition(Text key, Text value, int numPartitions) {
//            String rowkey = StringUtil.reverse(key.toString());
//            if("3230_1679".equals(rowkey)){
//                return 0;
//            }else if("3231_1679".equals(rowkey)){
//                return 1;
//            }else if("3232_1679".equals(rowkey)){
//                return 2;
//            }else if("3233_1679".equals(rowkey)){
//                return 3;
//            }else if("3230_1680".equals(rowkey)){
//                return 4;
//            }else if("3231_1680".equals(rowkey)){
//                return 5;
//            }else if("3232_1680".equals(rowkey)){
//                return 6;
//            }else if("3233_1680".equals(rowkey)){
//                return 7;
//            }else if("3230_1681".equals(rowkey)){
//                return 8;
//            }else if("3231_1681".equals(rowkey)){
//                return 9;
//            }else if("3232_1681".equals(rowkey)){
//                return 10;
//            }else if("3233_1681".equals(rowkey)){
//                return 11;
//            }else if("3230_1682".equals(rowkey)){
//                return 12;
//            }else if("3231_1682".equals(rowkey)){
//                return 13;
//            }else if("3232_1682".equals(rowkey)){
//                return 14;
//            }else{
//                return 15;
//            }
//        }
//    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 7) {
            logger.info("arguments is wrong. ");
            return;
        }
        String source = args[0].trim();
        String tableName = args[1].trim();
        String familyName = args[2].trim();
        int reduceNum = Integer.parseInt(args[3].trim());
        Configuration conf = new Configuration();
        conf.set("tableName", tableName);
        conf.set("source", source);
        conf.set("familyName", familyName);
        String root = args[4].trim();
        String seDate = args[5];
        String[] seDateArray = seDate.split("-");
        String sDate = seDateArray[0];
        String eDate = seDateArray[1];

        String isTaxi = args[6];
        conf.setBoolean("isTaxi", isTaxi.equals("1"));


        Job job = Job.getInstance(conf, "ReadDidiSourceMR_" + seDate);

        FileSystem fs = FileSystem.get(conf);
        List<Path> pathList = new ArrayList();
        Path inputPaths = new Path(root);
        addInputFile(inputPaths, fs, pathList, "", sDate, eDate);

        for (Path path : pathList) {
            FileInputFormat.addInputPath(job, path);
        }
//        job.setPartitionerClass(MyPartitioner.class);
        job.setJarByClass(ReadDidiSourceMR.class);
        job.setMapperClass(ReadDidiSourceMR.ReadDidiSourceMapper.class);
        TableMapReduceUtil.initTableReducerJob(tableName,
                ReadDidiSourceMR.ReadDidiSourceReducer.class, job);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setNumReduceTasks(reduceNum);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static void addInputFile(Path inputPath, FileSystem fs,
                                    List<Path> pathList, String prefix, String start, String end) throws IOException {
        if (fs.isDirectory(inputPath)) {
            FileStatus[] listStatus = fs.listStatus(inputPath);
            for (FileStatus file : listStatus) {
                if (file.isDirectory()) {
                    if (file.getPath().getName().startsWith(prefix)) {
                        String substr = file.getPath().getName()
                                .replace(prefix, "");
                        if (Integer.parseInt(substr) >= Integer.parseInt(start)
                                && Integer.parseInt(substr) <= Integer
                                .parseInt(end)) {
                            System.out.println("Add input path : " + file.getPath());
                            pathList.add(file.getPath());
                        }
                    }
                }
            }
        }
    }
}
