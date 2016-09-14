package com.navinfo.mapspotter.process.topic.restriction.trackbuild;

import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.model.SogouCarTrack;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Arrays;

/**
 * 构建搜狗轨迹
 * Created by SongHuiXing on 2016/1/24.
 */
public class BuildSogouTrack extends BuildTrack {

    @Override
    public Job getJob(Configuration cfg, int minutes,
                      String[] inputPaths, String outputPath,
                      String prefix, int reducecount) throws Exception {
        Job job = super.getJob(cfg, minutes, inputPaths, outputPath, prefix, reducecount);

        job.setMapperClass(BuildSogouTrackMapper.class);
        job.setReducerClass(BuildSogouTrackReducer.class);

        return job;
    }

    /**
     * Mapper
     */
    public static class BuildSogouTrackMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private int trackseconds = 600;

        private Text outKey = new Text();

        private Logger log = Logger.getLogger(BuildSogouTrackMapper.class);

        @Override
        protected void setup(Context context){
            int tracktime = context.getConfiguration().getInt(RestrictionConfig.TRACK_TIME, 10);
            trackseconds = tracktime * 60;

            log.info(String.format("Build track per %d minutes.", tracktime));
        }

        @Override
        public void map(LongWritable lineIndex, Text lineStr, Context context)
                throws IOException, InterruptedException {

            String[] fields = lineStr.toString().split("\t");

            long timeStamp = SogouCarTrack.isValid(fields);
            if(timeStamp < 0){
                context.getCounter(BuildCounter.InvalidData).increment(1);
                return;
            }

            String trackID = fields[fields.length-1].trim();

            long startTimeSpanPos = timeStamp / trackseconds;

            String mapkey = trackID + startTimeSpanPos;
            outKey.set(mapkey);

            context.write(outKey, lineStr);
        }
    }

    /**
     * Reducer
     */
    public static class BuildSogouTrackReducer
            extends Reducer<Text, Text, Text, Text> {

        private Text outvalue = new Text();

        @Override
        public void reduce(Text mapKey, Iterable<Text> mapValues, Context context)
                throws IOException, InterruptedException {

            CarTrack carTrack = null;

            for(Text tracklog : mapValues){
                String[] fields = tracklog.toString().split("\t");

                try {
                    int fieldCount = fields.length;
                    String trackID = fields[fieldCount - 1].trim();

                    if (null == carTrack)
                        carTrack = new SogouCarTrack(trackID);

                    String[] pointAttrs = Arrays.copyOfRange(fields, 0, fieldCount - 1);
                    int code = carTrack.pushPoint(pointAttrs);

                    if (-1 == code) {
                        context.getCounter(BuildCounter.IgnoreData).increment(1);
                    } else if (1 == code) {
                        outvalue.set(JsonUtil.getInstance().write2String(carTrack));
                        context.write(mapKey, outvalue);

                        carTrack = new SogouCarTrack(trackID);
                        carTrack.pushPoint(pointAttrs);
                    }
                }catch (Exception e){
                    carTrack = null;
                }
            }

            if(null == carTrack)
                return;

            outvalue.set(JsonUtil.getInstance().write2String(carTrack));
            context.write(mapKey, outvalue);
        }

    }

    /**
     * 构建log数据的轨迹
     * @param args 输入数据路径，输出数据路径
     * @throws Exception
     */
    public static void main( String[] args )
            throws Exception {
        System.exit(ToolRunner.run(new BuildSogouTrack(), args));
    }
}
