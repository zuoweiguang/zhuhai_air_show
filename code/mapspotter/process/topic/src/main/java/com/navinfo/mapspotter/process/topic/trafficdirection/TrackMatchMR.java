package com.navinfo.mapspotter.process.topic.trafficdirection;

import com.navinfo.mapspotter.foundation.algorithm.Raster;
import com.navinfo.mapspotter.foundation.algorithm.string.SimpleCountCluster;
import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.model.CarTrackPoint;
import com.navinfo.mapspotter.foundation.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

//import java.io.BufferedReader;
//import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaojian on 2016/4/7.
 */
public class TrackMatchMR {
    private static final Logger logger = Logger.getLogger(TrackMatchMR.class);

    public static class TrackMatchMapper extends Mapper<LongWritable, Text, Text, Text> {
        private static JsonUtil jsonUtil = JsonUtil.getInstance();
        private static MercatorUtil mercatorUtil = new MercatorUtil(1024, 14);

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split("\t");
            CarTrack track = jsonUtil.readValue(line[1], CarTrack.class);

            String segment = "";
            String tile = "";
            String carid = track.getCarID();
            for (CarTrackPoint point : track.getCarTrack()) {
                String tile2 = mercatorUtil.pixels2MCode(new IntCoordinate(
                        point.getLongitude(), point.getLatitude()
                ));
                if (!tile.equals(tile2)) {
                    write(tile , segment, context);
                    tile = tile2;
                    segment = "";
                }
                IntCoordinate coord = mercatorUtil.pixelsInTile(new IntCoordinate(point.getLongitude(), point.getLatitude()));
                segment += coord.x + " " + coord.y + " ";
            }

            write(tile , segment, context);
        }

        protected void write(String tile, String segment, Context context) throws IOException, InterruptedException {
            segment = segment.trim();
            if (segment.indexOf(' ') != segment.lastIndexOf(' ')) {
                context.write(new Text(tile), new Text(segment));
            }
        }

    }

    public static class TrackMatchReducer extends Reducer<Text, Text, Text, Text> {
        private Redis redis = null;
        private Hbase hbase = null;
        private String family = null;
        private String qualifier = null;
        //private Table hTable = null;
        //private String testfamily = "track";
        //private String testqualifier = "data";
        private int filterval = 0;
        private int bInter = 0;
        private String zookeeperHost = null;
        //private Map<String, Integer> map = null;

//        private void loadMap(String path) {
//            map = new HashMap<>();
//            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
//                String line = reader.readLine();
//                while (line != null) {
//                    String[] splits = line.split("\t");
//                    if (splits.length == 2) {
//                        map.put(splits[0], Integer.parseInt(splits[1]));
//                    }
//                }
//            } catch (Exception e) {
//                logger.error(e);
//            }
//        }

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            zookeeperHost = context.getConfiguration().get("zookeeperhost");
            hbase = (Hbase) DataSource.getDataSource(IOUtil.makeHBaseParam(zookeeperHost));
            hbase.openTable(context.getConfiguration().get("tablename"));
            family = context.getConfiguration().get("family");
            qualifier = context.getConfiguration().get("qualifier");
            //loadMap(context.getConfiguration().get("mapfile"));
            redis = (Redis) DataSource.getDataSource(IOUtil.makeRedisParam(
                    "192.168.4.128", 6379
            ));

            //hTable = hbase.getTable("direct_track_test");

            filterval = Integer.parseInt(context.getConfiguration().get("filterval"));
            bInter = Integer.parseInt(context.getConfiguration().get("bInter"));
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            hbase.close();
            redis.close();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String skey = key.toString();
            byte[] data = hbase.query(StringUtil.reverse(skey), family, qualifier);
            if (data == null) {
                logger.error(skey);
                return;
            }
            //Integer[][] trackmatrix = new Integer[1024][1024];

            Map<String, Integer> map = new HashMap<>();
            Integer[][] matrix = MatrixUtil.deserializeMatrix(data, true);
            SimpleCountCluster cluster = new SimpleCountCluster(255);
            for (Text value : values) {
                String coordinates = value.toString();
                List<int[]> coords = null;
                if (bInter == 0) {
                    coords = getPoint(coordinates);
                }else {
                    coords = getInterPoint(coordinates);
                }
                //String[] coords = coordinates.split(" ");
                //for (int i = 0; i < coords.length; i += 2) {
                for (int i = 0 ; i < coords.size() ; i++){
                    int x = coords.get(i)[0];//Integer.parseInt(coords[i]);
                    int y = coords.get(i)[1];//Integer.parseInt(coords[i + 1]);
                    if (x < 0 || x > 1023 || y < 0 || y > 1023){
                        continue;
                    }
                    Integer linkpid = matrix[y][x];
                    if (linkpid == null) {
                        linkpid = 0;
                    }
                    cluster.insertHit(linkpid);
                    //trackmatrix[y][x] = (trackmatrix[y][x] == null ? 0 : trackmatrix[y][x]) + 1;
                }
                //if (skey.equals("13489_6205")){
                //String strlog = cluster.get2DRunningCode(0);
                //logger.info(skey + "," + carid + "----" + strlog);}
                List<Integer> linkpids = cluster.getRunningCode(filterval);
                if (linkpids != null) {
                    int prelink = 0;
                    for (Integer linkpid : linkpids) {
                        if (linkpid == 0) {
                            continue;
                        }
                        if (prelink == 0) {
                            prelink = linkpid;
                            continue;
                        }
                        String traffic = prelink + "-" + linkpid;
                        Integer count = map.get(traffic);
                        if (count == null) {
                            count = 0;
                        }
                        map.put(traffic, count + 1);
                        prelink = linkpid; //// add
                    }
                }
            }

            //byte[] wdata = MatrixUtil.serializeSparseMatrix(trackmatrix , true);
            //hbase.insertData(hTable , StringUtil.reverse(key.toString()) , testfamily , testqualifier , wdata);

            Map<String, int[]> linkmap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String r = redis.queryString(entry.getKey());
                if (r == null || r.equals("nil")) {
                    continue;
                }
                String[] links = entry.getKey().split("-");
                int[] v = linkmap.get(links[0]);
                if (v == null) {
                    v = new int[]{0, 0};
                }
                if (r.charAt(0) == '1') {
                    v[0] = v[0] + entry.getValue();
                } else {
                    v[1] = v[1] + entry.getValue();
                }
                linkmap.put(links[0], v);
                v = linkmap.get(links[1]);
                if (v == null) {
                    v = new int[]{0, 0};
                }
                if (r.charAt(1) == '1') {
                    v[0] = v[0] + entry.getValue();
                } else {
                    v[1] = v[1] + entry.getValue();
                }
                linkmap.put(links[1], v);
            }

            for (Map.Entry<String, int[]> entry : linkmap.entrySet()) {
                String outvalue = entry.getValue()[0] + "\t" + entry.getValue()[1];
                context.write(new Text(entry.getKey()), new Text(outvalue));
            }
        }

        /**
         * 获取轨迹点
         * @param linevalue
         * @return
         */
        public List<int[]> getPoint(String linevalue){
            if (linevalue == null || linevalue.isEmpty()){
                return null;
            }

            List<int[]> listret = new ArrayList<>();
            String[] coordinates = linevalue.split(" ");
            for (int i = 0 ; i < coordinates.length ; i+= 2){
                int x = Integer.parseInt(coordinates[i]);
                int y = Integer.parseInt(coordinates[i+1]);
                listret.add(new int[]{x , y});
            }

            return listret;
        }

        /**
         * 获取内插后的轨迹点
         * @param linevalue
         * @return
         */
        public List<int[]> getInterPoint(String linevalue){
            if (linevalue == null || linevalue.isEmpty()){
                return null;
            }
            List<int[]> listret = new ArrayList<>();

            String[] coordinates = linevalue.split(" ");
            for (int i = 0 ; i < coordinates.length - 2 ; i+=2){
                int x1 = Integer.parseInt(coordinates[i]);
                int y1 = Integer.parseInt(coordinates[i+1]);
                int x2 = Integer.parseInt(coordinates[i+2]);
                int y2 = Integer.parseInt(coordinates[i+3]);
                List<int[]> vRet =  Raster.getAdvanceBresenhamline(x1 , y1 , x2 , y2);
                listret.addAll(vRet);
            }

            if (coordinates.length == 2){
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);

                listret.add(new int[]{x, y});
            }

            return listret;
        }
    }
    public static void main(String[] args) throws Exception {
        String inputPath = args[0];
        String outPath = args[1];
        int split = Integer.parseInt(args[2]);
        String zookeeperhost = args[3];
        String tablename = args[4];
        String family = args[5];
        String qualifier = args[6];
        String filterval = args[7];
        String binter = args[8];
        //String mapfile = args[6];

        Configuration conf = new Configuration();
        conf.set("zookeeperhost" , zookeeperhost);
        conf.set("tablename", tablename);
        conf.set("family", family);
        conf.set("qualifier", qualifier);
        conf.set("filterval" , filterval);
        conf.set("bInter" , binter);
        //conf.set("mapfile", mapfile);

        Job job = Job.getInstance(conf, "TrackMatchMR");
        job.setJarByClass(TrackMatchMR.class);
        job.setMapperClass(TrackMatchMapper.class);
        job.setReducerClass(TrackMatchReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(split);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        // 计算分割文件大小，控制map个数
//        long totalSize = Hdfs.getFileSize(conf, inputPath);
//        long splitSize = totalSize / split;
//        FileInputFormat.setMaxInputSplitSize(job, splitSize);

        if (Hdfs.deleteIfExists(conf, outPath)) {
            System.out.println("存在此输出路径，已删除！！！");
        }
        FileOutputFormat.setOutputPath(job, new Path(outPath));

        job.waitForCompletion(true);
    }
}
