package com.navinfo.mapspotter.process.convert.vectortile;


import com.mongodb.hadoop.MongoInputFormat;

import com.mongodb.hadoop.util.MongoConfigUtil;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.bson.BSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuiliang on 2016/7/5.
 */
public class MongoToHbaseMR {
    private static Logger log = Logger.getLogger(MongoToHbaseMR.class);

    public static class MongoMapper extends Mapper<Object, BSONObject, Text, Text> {

        private int minLevel;
        private int maxLevel;

        private Text outKey = new Text();
        private Text outVal = new Text();

        private HashMap<Integer, MercatorUtil> mercatorUtils = new HashMap<>();

        @Override
        protected void setup(Context context) {
            minLevel = context.getConfiguration().getInt("minLevel", 3);
            maxLevel = context.getConfiguration().getInt("maxLevel", 17);

            for (int level = minLevel; level <= maxLevel; level++) {
                mercatorUtils.put(level, new MercatorUtil(256, level));
            }
        }

        @Override
        public void map(Object key, BSONObject value, Context context)
                throws IOException, InterruptedException {

            int monthEdit = (Integer) value.get("month_edit");
            int dayEdit = (Integer) value.get("day_edit");
            int collect = (Integer) value.get("collect");

            double lon = (Double) value.get("x");
            double lat = (Double) value.get("y");

            Coordinate coordinate = new Coordinate(lon, lat);

            for (int level = minLevel; level <= maxLevel; level++) {
                MercatorUtil util = mercatorUtils.get(level);
                IntCoordinate tilePixel = util.lonLat2InTile(coordinate);
                outKey.set(level + "_" + util.lonLat2MCode(coordinate));
                if (monthEdit == 1) {
                    outVal.set(changeResolution(tilePixel.x, 4096) + "_" + changeResolution(tilePixel.y, 4096) + ",monthEdit");
                    context.write(outKey, outVal);
                }
                if (dayEdit == 1) {
                    outVal.set(changeResolution(tilePixel.x, 4096) + "_" + changeResolution(tilePixel.y, 4096) + ",dayEdit");
                    context.write(outKey, outVal);
                }
                if (collect == 1) {
                    outVal.set(changeResolution(tilePixel.x, 4096) + "_" + changeResolution(tilePixel.y, 4096) + ",collect");
                    context.write(outKey, outVal);
                }

            }
        }

        /**
         * 把256分辨率下的像素坐标转换到目标分辨率的中心
         *
         * @param i
         * @param targetResolution
         * @return
         */
        public static int changeResolution(int i, int targetResolution) {
            int time = targetResolution / 256;

            return i * time + time / 2;
        }
    }

    public static class HbaseReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {

        private GeometryFactory geometryFactory;
        private String family;
        private String qualifier;
        private ImmutableBytesWritable outKey = new ImmutableBytesWritable();

        @Override
        protected void setup(TableReducer<Text, Text, ImmutableBytesWritable>.Context context)
                throws IOException, InterruptedException {
            qualifier = context.getConfiguration().get("qualifier");
            family = context.getConfiguration().get("family");
            geometryFactory = new GeometryFactory();
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            String tileCode = key.toString();

            byte[] rowKey = tileCode.getBytes();

            VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

            for (Text value : values) {
                String val = value.toString();
                String lonlat = val.split(",")[0];
                String editType = val.split(",")[1];

                Integer lon = Integer.parseInt(lonlat.split("_")[0]);
                Integer lat = Integer.parseInt(lonlat.split("_")[1]);

                Coordinate coordinate = new Coordinate(lon, lat);
                Point point = geometryFactory.createPoint(coordinate);
                Map<String, Object> attributes = new HashMap<>();
                if(editType.equals("monthEdit")){
                    vtm.addFeature(WarehouseDataType.LayerType.PoiMonthEditHeatmap.toString(), attributes, point);
                }
                if(editType.equals("dayEdit")){
                    vtm.addFeature(WarehouseDataType.LayerType.PoiDayEditHeatmap.toString(), attributes, point);
                }
                if(editType.equals("collect")){
                    vtm.addFeature(WarehouseDataType.LayerType.PoiCollectHeatmap.toString(), attributes, point);
                }
            }


            if (vtm.encode().length > 0) {
                Put put = new Put(rowKey);
                put.addColumn(family.getBytes(), qualifier.getBytes(), vtm.encode());
                outKey.set(rowKey);
                context.write(outKey, put);
            }
        }
    }

    public static class MongoToHbaseDriver extends Configured implements Tool {
        @Override
        public int run(String[] args) throws Exception {
            String mongoHost = args[0];
            String mongoPost = args[1];
            String mongoDatabase = args[2];
            String mongoCollection = args[3];
            String mongoCondition = args[4];

            String hbaseTable = args[5];
            String hbaseFamily = args[6];
            String hbaesQualifier = args[7];

            Configuration conf = new Configuration();
            conf.set("family", hbaseFamily);
            conf.set("qualifier", hbaesQualifier);

            String mongoUrl = "mongodb://" + mongoHost + ":" + mongoPost + "/" + mongoDatabase + "." + mongoCollection;

            MongoConfigUtil.setInputURI(conf, mongoUrl);
            MongoConfigUtil.setQuery(conf, mongoCondition);
            Job job = Job.getInstance(conf, "MongoToHbase");
            job.setJarByClass(MongoToHbaseMR.class);
            job.setMapperClass(MongoToHbaseMR.MongoMapper.class);
            TableMapReduceUtil.initTableReducerJob(hbaseTable,
                    Search2HeatmapMR.Search2HeatMapReducer.class, job);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setInputFormatClass(MongoInputFormat.class);


            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) {
        try {
            System.exit(ToolRunner.run(new MongoToHbaseDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public class MongoToHbaseDriver extends MongoTool {
//        public MongoToHbaseDriver() throws UnknownHostException {
//            setConf(new Configuration());
//
//            MongoConfigUtil.setInputFormat(getConf(), MongoInputFormat.class);
//            MongoConfigUtil.setOutputFormat(getConf(), TextOutputFormat.class);
//
//
//            MongoConfigUtil.setInputURI(getConf(), "mongodb://192.168.4.128:27017/mapspotter.poi");
//
//            MongoConfigUtil.setMapper(getConf(), MongoMapper.class);
//            MongoConfigUtil.setReducer(getConf(), HbaseReducer.class);
//            MongoConfigUtil.setMapperOutputKey(getConf(), Text.class);
//            MongoConfigUtil.setMapperOutputValue(getConf(), Text.class);
//            MongoConfigUtil.setOutputKey(getConf(), IntWritable.class);
//            MongoConfigUtil.setOutputValue(getConf(), BSONWritable.class);
//
//            new SensorDataGenerator().run();
//        }
//    }
}
