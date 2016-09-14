package com.navinfo.mapspotter.process.topic.poistat;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.model.PoiHang;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.analysis.poistat.AreaAnalysis;
import com.navinfo.mapspotter.process.analysis.poistat.BlockInfo;
import com.navinfo.mapspotter.process.analysis.poistat.BlocksAnalysis;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaojian on 2016/3/17.
 */
public class HeatmapMR {
    private static final Logger logger = Logger.getLogger(HeatmapMR.class);

    public static class HeatmapData {
        public String id = "";
        public List<double[]> data = new ArrayList<>();
    }

    public static class HeatmapMapper extends Mapper<LongWritable, Text, Text, Text> {
        private BlocksAnalysis blocksAnalysis = null;
        private int level;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            //String strSql = PropertiesUtil.getValue("AreaCount.adminsql");
            String blockfile = context.getConfiguration().get("adminfile");
            blocksAnalysis = new BlocksAnalysis();
            //blocksAnalysis.Initialize(PropertiesUtil.getValue("AreaCount.host") , PropertiesUtil.getValue("AreaCount.db"),
            //        PropertiesUtil.getValue("AreaCount.user") , PropertiesUtil.getValue("AreaCount.password") ,
            //        PropertiesUtil.getValue("AreaCount.port"));
            //blocksAnalysis.PrepareRtree(strSql);
            blocksAnalysis.prepareRTree_Json(blockfile , 1);
            level = context.getConfiguration().getInt("level", 0);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {

        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            PoiHang poiHang = PoiHang.parse(value.toString());
            if (poiHang == null) return;

            double lon = poiHang.getoLon();
            double lat = poiHang.getoLat();
            int count = poiHang.getCount();
            Geometry geometry = GeoUtil.createPoint(lon , lat);
            Coordinate coordinate = geometry.getCoordinate();
            List<BlockInfo> rlist = blocksAnalysis.Contains(coordinate , geometry);
            if (rlist.isEmpty() || rlist.size() == 0){
                return;
            }
            String areaId = rlist.get(0).getBlockid();

            areaId = areaId.substring(0, 2) + "0000";

            IntCoordinate pixelCoord = MercatorUtil.getDefaultInstance().lonLat2Pixels(
                    new Coordinate(lon, lat), level
            );

            String result = pixelCoord.x + "," + pixelCoord.y + ":" + count;

            context.write(new Text(areaId), new Text(result));
        }
    }

    public static class HeatmapReducer extends Reducer<Text, Text, Text, Text> {
        private MongoDB mdb = null;
        private String table = "";
        private int level;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            String mongohost = context.getConfiguration().get("mongohost");
            String mongoport = context.getConfiguration().get("mongoport");
            String mongodb = context.getConfiguration().get("mongodb");
            mdb = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams(
                    mongohost,
                    Integer.parseInt(mongoport),
                    mongodb
            ));
            table = context.getConfiguration().get("table");
            level = context.getConfiguration().getInt("level", 0);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            mdb.close();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Map<String, Integer> map = new HashMap<>();

            for (Text value : values) {
                String[] splits = value.toString().split(":");
                String coord = splits[0];
                int count = Integer.parseInt(splits[1]);

                Integer sum = map.get(coord);
                if (sum == null) {
                    sum = 0;
                }

                map.put(coord, sum + count);
            }

            HeatmapData data = new HeatmapData();
            data.id = key.toString();

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String[] splits = entry.getKey().split(",");
                int px = Integer.parseInt(splits[0]);
                int py = Integer.parseInt(splits[1]);
                Coordinate coord = MercatorUtil.getDefaultInstance().pixels2LonLat(
                        new IntCoordinate(px, py), level
                );
                double[] point = {coord.y, coord.x, (double) entry.getValue()};
                data.data.add(point);
            }

            List<HeatmapData> list = new ArrayList<>();
            list.add(data);
            mdb.insert(table, list);
        }
    }
    public static class HeadmapDriver extends Configured implements Tool{
        @Override
        public int run(String[] args) throws Exception{
            String inputPath = args[0];
            String mghost = args[1];
            String mgport = args[2];
            String mgdbname = args[3];
            String tablename = args[4];
            int level     = Integer.parseInt(args[5]);
            String adminfile = args[6];
            long splitcount = Long.parseLong(args[7]);

            Configuration conf = new Configuration();
            conf.set("table", tablename);
            conf.set("mongohost" , mghost);
            conf.set("mongoport" , mgport);
            conf.set("mongodb" , mgdbname);
            conf.setInt("level", level);
            conf.set("adminfile" , adminfile);

            logger.info("inputPath : " + inputPath + " adminfile : " + adminfile);
            logger.info("mongohost : " + mghost + " mongoport : " + mgport + "mongodb" + mgdbname + " tablename : " + tablename);
            logger.info("splitcount : " + splitcount  + " level " + level);

            Job job = Job.getInstance(conf, "PoiHeatmapMR");
            job.setJarByClass(HeatmapMR.class);
            job.setMapperClass(HeatmapMapper.class);
            job.setReducerClass(HeatmapReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setOutputFormatClass(NullOutputFormat.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            // 计算分割文件大小，控制map个数
            long totalSize = Hdfs.getFileSize(conf, inputPath);
            long splitSize = totalSize / splitcount;
            FileInputFormat.setMaxInputSplitSize(job, splitSize);

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }
    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new HeadmapDriver() , args));
    }

    /**
     * azkaban调用接口
     * @param inputfile  搜索日志下挂后数据
     * @param mongohost  mongo host
     * @param mongoport  mongo port
     * @param mongodb    mongo db
     * @param tablename  mongoDB 热力图表名
     * @param level      墨卡托等级
     * @param adminfile  行政区划数据
     * @param splitcount mapper个数
     * @throws Exception
     */
    public void azkabanRun(String inputfile ,String mongohost , String mongoport , String mongodb , String tablename , String level , String adminfile , String splitcount) throws Exception{
        ToolRunner.run(new HeadmapDriver() , new String[]{inputfile , mongohost , mongoport , mongodb , tablename ,level , adminfile , splitcount});
    }
}
