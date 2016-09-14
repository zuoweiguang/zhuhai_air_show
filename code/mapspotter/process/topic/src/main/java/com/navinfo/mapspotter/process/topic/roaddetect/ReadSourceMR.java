package com.navinfo.mapspotter.process.topic.roaddetect;


import com.navinfo.mapspotter.foundation.io.*;
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
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cuiliang on 2016/1/7.
 */
public class ReadSourceMR {
    private static final Logger logger = Logger.getLogger(ReadSourceMR.class);
    private static final int level = 12;
    private static MercatorUtil mkt = new MercatorUtil(1024, level);


    public static class ReadSourceMapper extends Mapper<LongWritable, Text, Text, Text> {

        private String source = "";
        private static DateFormat DateFormater;
        private BlocksAnalysis analysis;

        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
            DateFormater = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
            analysis = new BlocksAnalysis();
            try {
                analysis.prepareMap_Json("" ,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String v = value.toString();

            if (source.equals(Constants.SOURCE_FLOAT)) {
                String[] track = v.split("\\|");
                for (int i = 1; i < track.length; i++) {
                    String content = track[i];
                    String lon = content.split(",")[0];
                    String lat = content.split(",")[1];
                    String rowKey = mkt.lonLat2MCode(new Coordinate(
                            Double.parseDouble(lon),
                            Double.parseDouble(lat)
                    ));
                    context.write(new Text(rowKey), new Text(lon + "," + lat));
                }
            } else if (source.equals(Constants.SOURCE_SOGOU)) {
                String[] track = v.split("\t");
                String time = track[0];
                String lon = track[1];
                String lat = track[2];
                String heading = track[3];
                String speed = track[4];
                String precision = track[5];
                boolean isDeal = true;

                try {
                    float iHeading = Float.parseFloat(heading);
                    if (iHeading < 0 || iHeading > 360) {
                        isDeal = false;
                    }

                } catch (Exception e) {
                    isDeal = false;
                }

                try {
                    float iSpeed = Float.parseFloat(speed);
                    if (iSpeed * 3.6 >= 200) {
                        isDeal = false;
                    }
                } catch (Exception e) {
                    isDeal = false;
                }

                try {
                    float iPrecision = Float.parseFloat(precision);
                    if (iPrecision < 0 || iPrecision > 1000) {
                        isDeal = false;
                    }
                } catch (Exception e) {
                    isDeal = false;
                }

                try{
                    Date dateTime = DateFormater.parse(time.trim());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateTime);
                    int ptYear = calendar.get(Calendar.YEAR);
                    if(ptYear == 1970)
                        isDeal = false;
                    calendar.setTime(new Date());

                    if(ptYear > calendar.get(Calendar.YEAR))
                        isDeal = false;
                }catch(ParseException e){
                    isDeal = false;
                }


                if (isDeal) {
                    String rowKey = MercatorUtil.lonLat2MCode(new Coordinate(Double.parseDouble(lon), Double.parseDouble(lat)), 12);
                    context.write(new Text(rowKey), new Text(lon + "," + lat));
                }
            } else {
                String lon = v.split(",")[1];
                String lat = v.split(",")[2];
                String rowKey = mkt.lonLat2MCode(new Coordinate(
                        Double.parseDouble(lon),
                        Double.parseDouble(lat)
                ));
                context.write(new Text(rowKey), new Text(lon + "," + lat));
            }
        }

    }

    public static class ReadSourceReducer extends
            TableReducer<Text, Text, ImmutableBytesWritable> {
        private String source = "";
        Connection connection = null;
        private int isIncrement = 1;
        private String familyName = "";

        @Override
        protected void setup(
                TableReducer<Text, Text, ImmutableBytesWritable>.Context context)
                throws IOException, InterruptedException {
            Configuration conf = HBaseConfiguration.create();
            connection = ConnectionFactory.createConnection(conf);

            isIncrement = Integer.parseInt(context.getConfiguration().get(
                    "isIncrement"));
            source = context.getConfiguration().get("source");
            familyName = context.getConfiguration().get("familyName");
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            byte[] keyByte = StringUtil.reverse(key.toString()).getBytes();

            Table table = connection.getTable(TableName
                    .valueOf(Constants.ROAD_DETECT_TABLE));
            Get get = new Get(keyByte);
            Result result = table.get(get);
            byte[] value = result.getValue(Constants.ROAD_DETECT_SOURCE_FAMILY.getBytes(),
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
                TableReducer<Text, Text, ImmutableBytesWritable>.Context context)
                throws IOException, InterruptedException {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static String[] getPathArray(FileSystem fs, String root, String startDate, String endDate, String prefix) throws IOException {
        Path path = new Path(root);
        List<Path> list = new ArrayList();
        getPathList(fs, path, list, startDate, endDate, prefix);
        int size = list.size();
        Path[] pathArray = list.toArray(new Path[size]);
        String[] array = new String[size];
        for(int i = 0 ; i < pathArray.length ; i++){
            array[i] = pathArray[i].toString();
        }
        return array;
    }


    public static void getPathList(FileSystem fs, Path path, List<Path> pathArray, String startDate, String endDate, String prefix) throws IOException {
        if (fs.isDirectory(path)) {
            String pathName = path.getName();
            if (pathName.startsWith("gps-track-")) {
                String week = pathName.replace("gps-track-", "");
                String[] weekArray = week.split("-");
                if (weekArray.length == 2) {
                    if (Integer.parseInt(weekArray[0]) >= Integer.parseInt(startDate)
                            && Integer.parseInt(weekArray[0]) <= Integer.parseInt(endDate)
                            && Integer.parseInt(weekArray[1]) >= Integer.parseInt(startDate)
                            && Integer.parseInt(weekArray[1]) <= Integer.parseInt(endDate)) {
                        Hdfs.addInputPath(path, fs, pathArray, prefix);
                    }
                }

            } else {
                FileStatus[] listStatus = fs.listStatus(path);
                for (FileStatus file : listStatus) {
                    getPathList(fs, file.getPath(), pathArray, startDate, endDate, prefix);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 9) {
            logger.info("arguments is wrong. ");
            return;
        }
        //源头 didi  sogou  baidu ...
        String source = args[0].trim();
        // map个数
        int mapNum = Integer.parseInt(args[1].trim());
        // reduce个数
        int reduceNum = Integer.parseInt(args[2].trim());
        // 是否增量
        String isIncrement = args[3].trim();
        // 文件前缀
        String prefix = args[4].trim();
        // 根目录
        String root = args[5].trim();
        // 起始日期
        String startDate = args[6].trim();
        // 结束日期
        String endDate = args[7].trim();

        String tableName = args[8].trim();
        String familyName = args[9].trim();

        Configuration conf = new Configuration();
        conf.set("source", source);
        conf.set("familyName", familyName);
        conf.set("isIncrement", isIncrement);
        FileSystem fs = FileSystem.get(conf);

        String[] input = getPathArray(fs, root, startDate, endDate, prefix);

        //String[] input = Arrays.copyOfRange(args, 5, args.length);
        conf.set("mapred.max.split.size", Long.toString(Hdfs.CalMapReduceSplitSize(input, fs, prefix, mapNum)));

        Job job = Job.getInstance(conf, "ReadSourceMR");
        job.setJarByClass(ReadSourceMR.class);
        job.setMapperClass(ReadSourceMR.ReadSourceMapper.class);
        TableMapReduceUtil.initTableReducerJob(tableName,
                ReadSourceMR.ReadSourceReducer.class, job);
        for (String inputPath : input) {
            FileInputFormat.addInputPath(job, new Path(inputPath));
        }
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(reduceNum);
        //job.setInputFormatClass(CombineTextInputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


    public static void azkabanRun(String source, int mapNum, int reduceNum, String isIncrement,
                                  String prefix, String root, String startDate, String endDate,
                                  String tableName, String familyName) throws Exception {
        Configuration conf = new Configuration();
        conf.set("source", source);
        conf.set("familyName", familyName);
        conf.set("isIncrement", isIncrement);
        FileSystem fs = FileSystem.get(conf);

        String[] input = getPathArray(fs, root, startDate, endDate, prefix);
        //String[] input = Arrays.copyOfRange(args, 5, args.length);
        conf.set("mapred.max.split.size", Long.toString(Hdfs.CalMapReduceSplitSize(input, fs, prefix, mapNum)));

        Job job = Job.getInstance(conf, "ReadSourceMR");
        job.setJarByClass(ReadSourceMR.class);
        job.setMapperClass(ReadSourceMR.ReadSourceMapper.class);
        TableMapReduceUtil.initTableReducerJob(tableName,
                ReadSourceMR.ReadSourceReducer.class, job);
        for (String inputPath : input) {
            FileInputFormat.addInputPath(job, new Path(inputPath));
        }
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setNumReduceTasks(reduceNum);
        //job.setInputFormatClass(CombineTextInputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
