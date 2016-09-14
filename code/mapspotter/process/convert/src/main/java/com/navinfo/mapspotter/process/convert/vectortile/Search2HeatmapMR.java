package com.navinfo.mapspotter.process.convert.vectortile;

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
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cuiliang on 2016/6/24.
 */
public class Search2HeatmapMR {

    private static Logger log = Logger.getLogger(Search2HeatmapMR.class);

    public static class Search2HeatMapMapper extends Mapper<LongWritable, Text, Text, Text> {

        private HashMap<Integer, MercatorUtil> mercatorUtils = new HashMap<>();

        private Text outKey = new Text();
        private Text info = new Text();

        private int minLevel;
        private int maxLevel;

        @Override
        protected void setup(Context context) {
            minLevel = context.getConfiguration().getInt("minLevel" ,3);
            maxLevel = context.getConfiguration().getInt("maxLevel" ,17);

            for(int level = minLevel ; level <= maxLevel ; level++){
                mercatorUtils.put(level, new MercatorUtil(256, level));
            }
        }

        @Override
        protected void cleanup(Context context){
            mercatorUtils.clear();
        }

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            try{
                String v = value.toString();
                String[] segments = v.split("\t");

                double lon = Double.parseDouble(segments[12]);
                double lat = Double.parseDouble(segments[13]);

                int searchTime = Integer.parseInt(segments[14]);

                Coordinate coordinate = new Coordinate(lon, lat);

                for(int level = minLevel ; level <= maxLevel ; level++){
                    MercatorUtil util = mercatorUtils.get(level);

                    outKey.set(level + "_" + util.lonLat2MCode(coordinate));

                    IntCoordinate tilePixel = util.lonLat2InTile(coordinate);
                    info.set(changeResolution(tilePixel.x, 4096) + "_" +
                            changeResolution(tilePixel.y, 4096) + "," + searchTime);

                    context.write(outKey, info);
                }
            }
            catch(Exception e){
                e.printStackTrace();
                System.out.println(value.toString());
            }
        }

        /**
         * 把256分辨率下的像素坐标转换到目标分辨率的中心
         * @param i
         * @param targetResolution
         * @return
         */
        public static int changeResolution(int i, int targetResolution) {
            int time = targetResolution / 256;

            return i * time + time / 2;
        }
    }

    public static class Search2HeatMapReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {

        private GeometryFactory geometryFactory;
        private String family;
        private String version;
        private ImmutableBytesWritable outKey = new ImmutableBytesWritable();

        @Override
        protected void setup(TableReducer<Text, Text, ImmutableBytesWritable>.Context context)
                throws IOException, InterruptedException {
            version = context.getConfiguration().get("version");
            family = context.getConfiguration().get("family");
            geometryFactory = new GeometryFactory();
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String tileCode = key.toString();

            byte[] rowKey = tileCode.getBytes();
            VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);


            for (Text value : values) {
                String val = value.toString();
                String lonlat = val.split(",")[0];
                int searchTime = Integer.parseInt(val.split(",")[1]);

                Integer lon = Integer.parseInt(lonlat.split("_")[0]);
                Integer lat = Integer.parseInt(lonlat.split("_")[1]);

                for(int i = 0 ; i < searchTime ; i++){
                    Coordinate coordinate = new Coordinate(lon, lat);
                    Point point = geometryFactory.createPoint(coordinate);
                    Map<String, Object> attributes = new HashMap<>();
                    vtm.addFeature(WarehouseDataType.LayerType.SogouSearch.toString(),
                            attributes, point);
                }
            }

            if(vtm.encode().length > 0){
                Put put = new Put(rowKey);
                put.addColumn(family.getBytes(), version.getBytes(), vtm.encode());
                outKey.set(rowKey);
                context.write(outKey, put);
            }
        }
    }


    public static class Search2HeatMapClusterReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {

        private GeometryFactory geometryFactory;
        private String family;
        private String version;
        private ImmutableBytesWritable outKey = new ImmutableBytesWritable();

        @Override
        protected void setup(TableReducer<Text, Text, ImmutableBytesWritable>.Context context)
                throws IOException, InterruptedException {
            version = context.getConfiguration().get("version");
            family = context.getConfiguration().get("family");
            geometryFactory = new GeometryFactory();
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String tileCode = key.toString();

            byte[] rowKey = tileCode.getBytes();

            int zLevel = Integer.parseInt(tileCode.split("_")[0]);

            Map<String, Integer> count = new HashMap();
            for (Text value : values) {
                String val = value.toString();
                String lonlat = val.split(",")[0];
                int searchTime = Integer.parseInt(val.split(",")[1]);
                Integer i = count.get(lonlat);
                if(i == null){
                    count.put(lonlat, searchTime);
                }
                else{
                    count.put(lonlat, searchTime + i);
                }
            }

            VectorTileEncoder vtm = new VectorTileEncoder(4096, 16, false);

            Iterator iter = count.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String lonlat = (String)entry.getKey();
                Integer searchTime = (Integer)entry.getValue();

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("searchTime", searchTime);

                Integer lon = Integer.parseInt(lonlat.split("_")[0]);
                Integer lat = Integer.parseInt(lonlat.split("_")[1]);

                Coordinate coordinate = new Coordinate(lon, lat);

                Point point = geometryFactory.createPoint(coordinate);

                vtm.addFeature(WarehouseDataType.LayerType.SogouSearch.toString(),
                        attributes, point);
            }
            if(vtm.encode().length > 0){
                Put put = new Put(rowKey);
                put.addColumn(family.getBytes(), version.getBytes(), vtm.encode());
                outKey.set(rowKey);
                context.write(outKey, put);
            }
        }
    }

    public static class Search2HeatmapDriver extends Configured implements Tool {
        private static Logger logger = Logger.getLogger(Search2HeatmapDriver.class);
        @Override
        public int run(String[] args) throws Exception {

            if (args == null || args.length < 7) {
                logger.info("arguments is wrong. ");
                return 0;
            }

            String table = args[0].trim();
            String family = args[1].trim();
            String version = args[2].trim();

            int minLevel = Integer.parseInt(args[3].trim());
            int maxLevel = Integer.parseInt(args[4].trim());

            // reduce个数
            int reduceNum = Integer.parseInt(args[5].trim());

            String inputPath = args[6].trim();

            Configuration conf = new Configuration();
            conf.set("table", table);
            conf.set("family", family);

            conf.setInt("minLevel", minLevel);
            conf.setInt("maxLevel", maxLevel);
            conf.set("version", version);

            Job job = Job.getInstance(conf, "Search2HeatMapMR_" + version);
            job.setJarByClass(Search2HeatmapMR.class);
            job.setMapperClass(Search2HeatmapMR.Search2HeatMapMapper.class);

            TableMapReduceUtil.initTableReducerJob(table,
                    Search2HeatmapMR.Search2HeatMapReducer.class, job);

            FileInputFormat.addInputPath(job, new Path(inputPath));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setNumReduceTasks(reduceNum);

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new Search2HeatmapDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
