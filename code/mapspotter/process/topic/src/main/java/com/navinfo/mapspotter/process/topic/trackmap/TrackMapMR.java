package com.navinfo.mapspotter.process.topic.trackmap;

//import com.navinfo.mapspotter.foundation.io.DataSource;
//import com.navinfo.mapspotter.foundation.io.IOUtil;
//import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.model.TrackMapData;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by gaojian on 2016/3/1.
 */
public class TrackMapMR {
    public static final Logger logger = Logger.getLogger(TrackMapMR.class);

    public static class TrackMapMapper extends TableMapper<Text, Text> {
        private static MercatorUtil mercator;
        private byte[] family;
        private byte[] qualifier;
        private boolean sparse;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            mercator = new MercatorUtil(256, 14);
            family = Bytes.toBytes(context.getConfiguration().get("family"));
            qualifier = Bytes.toBytes(context.getConfiguration().get("qualifier"));
            sparse = Integer.parseInt(context.getConfiguration().get("sparse")) == 1;
        }

        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
            String rowkey = Bytes.toString(value.getRow());
            String tile = StringUtil.reverse(rowkey);

            Integer[][] matrix = MatrixUtil.deserializeMatrix(value.getValue(family, qualifier), sparse);

            int size = 1024;
            int level = 14;
            IntCoordinate pixels = MercatorUtil.TRACKPOINT_MERCATOR.inTile2Pixels(new IntCoordinate(0, 0), tile);
            int originX = pixels.x;
            int originY = pixels.y;

            while (level > 5) {
                Integer[][] nextMatrix = new Integer[size/2][size/2];

                for (int x = 0; x < size; ++x) {
                    for (int y = 0; y < size; ++y) {
                         if (matrix[y][x] != null && matrix[y][x] > 0) {
                             int count = matrix[y][x];
                             String mcode = mercator.pixels2MCode(new IntCoordinate(originX+x, originY+y));
                             context.write(
                                     new Text(mcode + "_" + level),
                                     new Text(String.format("%d,%d,%d", originX+x, originY+y, count))
                             );

                             int x2 = x / 2;
                             int y2 = y / 2;
                             if (nextMatrix[y2][x2] != null) {
                                 nextMatrix[y2][x2] += count;
                             } else {
                                 nextMatrix[y2][x2] = count;
                             }
                         }
                    }
                }

                matrix = nextMatrix;
                level = level - 1;
                size = size / 2;
                originX = originX / 2;
                originY = originY / 2;
            }
        }
    }

    public static class TrackMapReducer extends TableReducer<Text, Text, ImmutableBytesWritable> {
//        private MongoDB mdb = null;
//        private String table = "";
        private MercatorUtil mercator = null;

        private byte[] family;

        private byte[] qualifier;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
//            mdb = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams(
//                    PropertiesUtil.getValue("MongoDB.host"),
//                    Integer.parseInt(PropertiesUtil.getValue("MongoDB.port")),
//                    PropertiesUtil.getValue("MongoDB.db")
//            ));
//            table = PropertiesUtil.getValue("TrackMapTable");
            mercator = new MercatorUtil(256, 14);

            family = context.getConfiguration().get("dest_family").getBytes();

            qualifier = context.getConfiguration().get("dest_qualifier").getBytes();
        }

//        @Override
//        protected void cleanup(Context context) throws IOException, InterruptedException {
//            mdb.close();
//        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
//            TrackMapData data = new TrackMapData();

//            data.tile = key.toString();
//            String[] tileinfos = data.tile.split("_");
            //int tx = Integer.parseInt(tileinfos[0]);
            //int ty = Integer.parseInt(tileinfos[1]);
//            int level = Integer.parseInt(tileinfos[2]);

            BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            for (Text value : values) {
                String str = value.toString();
                String[] contents = str.split(",");
                int x = Integer.parseInt(contents[0]);
                int y = Integer.parseInt(contents[1]);
                int count = Integer.parseInt(contents[2]);
//                Coordinate coord = mercator.pixels2LonLat(new IntCoordinate(x, y), level);
//                data.data.add(new double[]{coord.x, coord.y, count});

                IntCoordinate pixel = mercator.pixelsInTile(new IntCoordinate(x, y));
                image.setRGB(pixel.x, pixel.y, getColor(count));
            }

//            List<TrackMapData> list = new ArrayList<>();
//            list.add(data);
//            mdb.insert(table, list);

//            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
//            map.put("tile", key.toString());
//            map.put("data", ImageUtil.getPNG(image));
//            mdb.insert(table, map);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ImageIO.write(image, "png", out);

            byte[] bytes = out.toByteArray();

            Put put = new Put(key.toString().getBytes());

            put.addColumn(family,qualifier,bytes);

            context.write(new ImmutableBytesWritable(put.getRow()),put);

        }

        public int getColor(int value) {
            if (value > 10000) {
                return 0xFF8B0000;
            } else if (value > 5000) {
                return 0xFFFF0000;
            } else if (value > 2000) {
                return 0xFFFF7F50;
            } else if (value > 1000) {
                return 0xFFFFA500;
            } else if (value > 500) {
                return 0xFFFFD700;
            } else if (value > 200) {
                return 0xFFFFFF00;
            } else if (value > 100) {
                return 0xFFA8D032;
            } else if (value > 50) {
                return 0xFF90EE90;
            } else if (value > 10) {
                return 0xFF7FFFD4;
            } else {
                return 0xFFC8F9F9;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String table = args[0];
        String family = args[1];
        String qualifier = args[2];
        String sparse = args[3];
        String destTabName = args[4];
        String destFamily = args[5];
        String destQualifier = args[6];
        Configuration conf = new Configuration();
        conf.set("family", family);
        conf.set("qualifier", qualifier);
        conf.set("sparse", sparse);
        conf.set("dest_family",destFamily);
        conf.set("dest_qualifier",destQualifier);

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapreduce.map.speculative", false);

        Job job = Job.getInstance(conf, "TrackMapMR");

        job.setJarByClass(TrackMapMR.class);

        Scan scan = new Scan();
        // 2015-10-16新增
//        scan.setCacheBlocks(false);
//        scan.setCaching(10);
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));

        TableMapReduceUtil.initTableMapperJob(table, scan,
                TrackMapMapper.class,
                Text.class, Text.class, job);

        TableMapReduceUtil.initTableReducerJob(destTabName,
                TrackMapReducer.class, job);

//        job.setReducerClass(TrackMapReducer.class);
//        job.setOutputFormatClass(NullOutputFormat.class);
//        job.setOutputKeyClass(NullWritable.class);
//        job.setOutputValueClass(NullWritable.class);
        job.setNumReduceTasks(16);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
