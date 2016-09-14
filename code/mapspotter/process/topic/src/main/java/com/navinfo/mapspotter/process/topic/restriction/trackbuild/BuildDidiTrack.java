package com.navinfo.mapspotter.process.topic.restriction.trackbuild;

import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.model.DidiCarTrack;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Arrays;

/**
 * 根据嘀嘀全量数据创建轨迹
 * Created by SongHuiXing on 2016/1/12.
 */
public class BuildDidiTrack extends BuildTrack {

    @Override
    public Job getJob(Configuration cfg, int minutes,
                      String[] inputPaths, String outputPath,
                      String prefix, int reducecount) throws Exception {
        Job job = super.getJob(cfg, minutes, inputPaths, outputPath, prefix, reducecount);

        job.setMapperClass(BuildDidiTrackMapper.class);
        job.setReducerClass(BuildDidiTrackReducer.class);

        return job;
    }

    /**
     * Mapper
     */
    public static class BuildDidiTrackMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private int trackseconds = 600;

        @Override
        protected void setup(Context context){
            int tracktime = context.getConfiguration().getInt(RestrictionConfig.TRACK_TIME, 10);
            trackseconds = tracktime * 60;
        }

        @Override
        public void map(LongWritable lineIndex, Text lineStr, Context context)
                throws IOException, InterruptedException {

            String[] fields = lineStr.toString().split(",");

            if(!DidiCarTrack.isValid(fields)){
                context.getCounter(BuildCounter.InvalidData).increment(1);
                return;
            }

            String trackID = fields[0].trim();

            String time = fields[fields.length-1].trim();
            long timeStamp = Long.parseLong(time);

            long startTimeSpanPos = timeStamp / trackseconds;

            String mapkey = trackID + startTimeSpanPos;

            context.write(new Text(mapkey), lineStr);
        }
    }

    /**
     * Reducer
     */
    public static class BuildDidiTrackReducer
            extends Reducer<Text, Text, Text, Text> {

        private Text outvalue = new Text();

        @Override
        public void reduce(Text mapKey, Iterable<Text> mapValues, Context context)
                throws IOException, InterruptedException {

            CarTrack carTrack = null;

            for(Text tracklog : mapValues){
                String[] fields = tracklog.toString().split(",");

                int fieldCount = fields.length;
                String trackID = fields[0].trim();

                if(null == carTrack)
                    carTrack = new DidiCarTrack(trackID);

                int code = carTrack.pushPoint(Arrays.copyOfRange(fields, 1, fieldCount));

                if(-1 == code){
                    context.getCounter(BuildCounter.IgnoreData).increment(1);
                } else if(1 == code){
                    outvalue.set(JsonUtil.getInstance().write2String(carTrack));
                    context.write(mapKey, outvalue);

                    carTrack = new DidiCarTrack(trackID);
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
        System.exit(ToolRunner.run(new BuildDidiTrack(), args));
    }
}
