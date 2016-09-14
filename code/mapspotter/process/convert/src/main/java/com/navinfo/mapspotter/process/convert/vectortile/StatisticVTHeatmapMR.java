package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.model.SogouCarTrack;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建Vectortile精度的轨迹热力数据
 * Created by SongHuiXing on 6/17 0017.
 */
public class StatisticVTHeatmapMR {
    private static Logger log = Logger.getLogger(StatisticVTHeatmapMR.class);

    private static final String HEATMAP_LEVEL = "heatmap_level";

    enum BuildCounter{
        InvalidData,
        TileCount,
    }

    public static class VTHeatmapMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private MercatorUtil mercatorUtil;

        private Text outKey = new Text();
        private Text info = new Text();

        @Override
        protected void setup(Context context){
            int level = context.getConfiguration().getInt(HEATMAP_LEVEL, 16);
            mercatorUtil = new MercatorUtil(4096, level);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String[] fields = value.toString().split("\t");

            long timeStamp = SogouCarTrack.isValid(fields);
            if(timeStamp < 0){
                context.getCounter(BuildCounter.InvalidData).increment(1);
                return;
            }

            double lon = Double.parseDouble(fields[1]);
            double lat = Double.parseDouble(fields[2]);

            Coordinate coordinate = new Coordinate(lon, lat);

            outKey.set(mercatorUtil.lonLat2MCode(coordinate));

            IntCoordinate tilePixel = mercatorUtil.lonLat2InTile(coordinate);
            info.set(tilePixel.x + "_" + tilePixel.y);

            context.write(outKey, info);
        }
    }

    public static class VTHeatmapReducer
            extends Reducer<Text, Text, Text, Text> {

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private int level;

        private Text outValue = new Text();
        private Text outkey = new Text();

        private StringBuilder builder = new StringBuilder();

        @Override
        protected void setup(Context context){
            level = context.getConfiguration().getInt(HEATMAP_LEVEL, 16);
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values,
                              Context context)
                throws IOException, InterruptedException{

            HashMap<String, Long> counter = new HashMap<>();

            for (Text v : values){
                String pixelCoord = v.toString();

                Long count = counter.get(pixelCoord);
                if(null != count){
                    counter.put(pixelCoord, ++count);
                } else {
                    counter.put(pixelCoord, 1L);
                }
            }

            outkey.set(level + "_" + key.toString());

            //默认估计给10个点大小左右空间
            builder.delete(0, builder.length());
            builder.setLength(2+16*10);
            builder.append('[');

            for (Map.Entry<String, Long> heatPoint : counter.entrySet()){
                String[] xy = heatPoint.getKey().split("_");

                builder.append('[');
                builder.append(xy[0]);
                builder.append(',');
                builder.append(xy[1]);
                builder.append(',');
                builder.append(heatPoint.getValue());
                builder.append(']');

                builder.append(',');
            }

            builder.deleteCharAt(builder.length()-1);
            builder.append(']');

            outValue.set(builder.toString());

            context.write(outkey, outValue);
        }
    }

    public static class StatisticDriver
            extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(2 > args.length){
                System.err.printf("Usage: %s <input> <output> [level] [reducecount]\n",
                        getClass().getSimpleName());

                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            int level = 16;
            if(args.length > 2){
                level = Integer.parseInt(args[2]);
            }

            int reducecount = 60;
            if(args.length > 3){
                reducecount = Integer.parseInt(args[3]);
            }

            log.info("Statistic Vector tile heatmap from:" + args[0] +
                    " to:" + args[1] + " with level is:" + level);

            Job job = getJob(conf, args[0], args[1], level, reducecount);

            int res = job.waitForCompletion(true) ? 0 : 1;

            return res;
        }

        public static Job getJob(Configuration conf,
                                 String inputPath,
                                 String outputPath,
                                 int level,
                                 int reducecount)
                throws Exception{

            conf.setInt(HEATMAP_LEVEL, level);

            Job job = Job.getInstance(conf, String.format("Statistic heatmap with level is %b", level));

            job.setJarByClass(StatisticDriver.class);

            FileSystem fileSystem = FileSystem.get(conf);
            ArrayList<Path> files = new ArrayList<>();
            Hdfs.addInputPath(new Path(inputPath), fileSystem, files, "part");

            for (Path f : files){
                FileInputFormat.addInputPath(job, f);
            }

            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(VTHeatmapMapper.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(VTHeatmapReducer.class);

            job.setNumReduceTasks(reducecount);

            return job;
        }
    }

    public static void main( String[] args )
            throws Exception {
        System.exit(ToolRunner.run(new StatisticDriver(), args));
    }
}
