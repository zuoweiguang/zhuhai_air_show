package com.navinfo.mapspotter.process.topic.restriction.trackhit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.navinfo.mapspotter.foundation.algorithm.rtree.EnvlopeIndentifiedObject;
import com.navinfo.mapspotter.foundation.algorithm.rtree.SimplePointRTree;
import com.navinfo.mapspotter.foundation.algorithm.string.SimpleCountCluster;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.model.CarTrack;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.topic.restriction.*;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossPosition;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 将轨迹在路口内打点
 * Created by SongHuiXing on 2016/1/12.
 */
public class HitTrackInCross {

    enum HitCounter{
        Cannot_Read_Json,
        Cannot_Find_Cross,
        Empty_Path,
        Beijing_Data,
    }

    private static Logger log = Logger.getLogger(HitTrackInCross.class);

    public static class HitTrackMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private SimplePointRTree crossPageEnvelopeIndex = null;

        private JsonUtil m_jsonUtil = null;

        private MercatorUtil mercatorUtil = new MercatorUtil(1024, RestricConfig.MercatorTileLevel);

        private Text crossKey = new Text();

        private Text info = new Text();

        public HitTrackMapper(){
            m_jsonUtil = JsonUtil.getInstance();
        }

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            String indexColFamily = cfg.get(RestrictionConfig.INDEX_COLFAMILY, "Page1");
            String rasterColFamily = cfg.get(RestrictionConfig.RASTER_COLFAMILY, "Analysis");

            CrossInformationVistor crossGetter = new CrossInformationVistor("Information",
                                                                            rasterColFamily,
                                                                            indexColFamily);

            crossGetter.prepare();

            crossPageEnvelopeIndex = crossGetter.buildMemIndex();

            crossGetter.shutdown();
        }

        @Override
        protected void cleanup(Context context){
            crossPageEnvelopeIndex = null;
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String trackStr = value.toString().trim();

            if(trackStr.isEmpty())
                return;

            int jsonPos = trackStr.indexOf("{");

            String jsonS = trackStr.substring(jsonPos);

            CarTrack track;
            try {
                track = m_jsonUtil.readValue(jsonS, CarTrack.class);
            } catch (JsonParseException|JsonMappingException e) {
                e.printStackTrace();
                return;
            }

            TrackCrossInformation crossinfo = getCarTrackRelatedCross(track, context);

            if(null == crossinfo) {
                return;
            }

            for(Map.Entry<Long, int[]> cross : crossinfo.getCoordsInCross().entrySet()){
                StringBuilder sb = new StringBuilder(CrossInformationVistor.getPidString(cross.getKey()));
                crossKey.set(sb.reverse().toString());
                info.set(track.getCarID()+"#"+m_jsonUtil.write2String(cross.getValue()));
                context.write(crossKey, info);
            }
        }

        /***
         * 获取轨迹对应的路口信息
         * @param track
         * @return 路口详细信息
         */
        public TrackCrossInformation getCarTrackRelatedCross(CarTrack track,
                                                             Context context) {

            TrackCrossInformation relatedCrossInfo = new TrackCrossInformation();

            relatedCrossInfo.TrackId = track.getCarID();

            double[] trackEnv = track.getEnvelope();
            if(trackEnv[0] > 115.5 && trackEnv[2] < 117 && trackEnv[1] > 39.48 && trackEnv[3] < 40.5){
                context.getCounter(HitCounter.Beijing_Data).increment(1);
            }

            Map<Long, double[]> crosses = new HashMap<>();

            Map<Long, EnvlopeIndentifiedObject> related = crossPageEnvelopeIndex.find(trackEnv);
            for (EnvlopeIndentifiedObject obj : related.values()){
                CrossPosition position = (CrossPosition)obj;
                crosses.put(position.CrossPid, position.getEnvelope());
            }

//                Map<Long, double[]> crosses = vistor.searchCrosses(trackEnv);

            if(null == crosses || 0 == crosses.size()){
                Counter counter = context.getCounter(HitCounter.Cannot_Find_Cross);
                counter.increment(1);

//                    if(counter.getValue() < 100) {
//                        log.info(String.format("Can not find related cross: %f, %f, %f, %f",
//                                trackEnv[0], trackEnv[1], trackEnv[2], trackEnv[3]));
//                    }
                return null;
            }

            //Envelope和轨迹的Envelope和相交的路口
            Map<Long, int[]> crossesInPixel = new HashMap<>();

            Iterator<Map.Entry<Long, double[]>> iter = crosses.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<Long, double[]> cross = iter.next();
                if(SpatialUtil.isEnvelopeIntesect(trackEnv, cross.getValue())){
                    //相交的路口Envelope换算为像素坐标
                    int[] pixelbounds = mercatorUtil.lonLat2Pixels(cross.getValue());
                    crossesInPixel.put(cross.getKey(),
                            new int[]{pixelbounds[0], pixelbounds[3], pixelbounds[2], pixelbounds[1]});
                }
            }

            crosses.clear();

            Map<Long, int[]> trackCoordsInCross = fliterTrackByCross(crossesInPixel, track.getTrackCoordinates());
            relatedCrossInfo.setCoordsInCross(trackCoordsInCross);

            return relatedCrossInfo;
        }

        /**
         * 使用路口过滤轨迹点，使得每个路口只保留路口Buffer内的轨迹点
         * @param crosses 路口pid及路口Buffer范围
         * @param trackCoords 轨迹点
         * @return
         */
        private static Map<Long, int[]> fliterTrackByCross(Map<Long, int[]> crosses,
                                                                int[] trackCoords){

            Map<Long, int[]> hittedCross = new HashMap<>();
            Map<Long, Integer> hitCrossRecord = new HashMap<>();

            int ptCount = trackCoords.length/2;

            Iterator<Map.Entry<Long, int[]>> iter;

            //正向查找起点
            for(int i=0;i<ptCount;i++){
                if(crosses.size() == 0)
                    break;

                iter = crosses.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<Long, int[]> cross = iter.next();
                    if(SpatialUtil.isPointInEnvelope(trackCoords[2*i], trackCoords[2*i+1], cross.getValue())){
                        hittedCross.put(cross.getKey(), cross.getValue());
                        hitCrossRecord.put(cross.getKey(), i);
                        iter.remove();
                    }
                }
            }

            Map<Long, int[]> result = new HashMap<>();

            //反向查找确定终点
            for(int j=ptCount-1;j>=0;j--){
                if(0 == hittedCross.size())
                    break;

                iter = hittedCross.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<Long, int[]> cross = iter.next();
                    if(SpatialUtil.isPointInEnvelope(trackCoords[2*j], trackCoords[2*j+1], cross.getValue())){
                        int startPos = hitCrossRecord.get(cross.getKey());
                        int ct = 2*(j - startPos + 1);
                        int[] coords = new int[ct];
                        System.arraycopy(trackCoords, 2*startPos, coords, 0, ct);
                        result.put(cross.getKey(), coords);

                        iter.remove();
                    }
                }
            }

            return result;
        }
    }

    public static class HitTrackReducer
            extends Reducer<Text, Text, LongWritable, Text> {

        private CrossInformationVistor m_crossGetter;

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private Text outValue = new Text();
        private LongWritable outkey = new LongWritable();

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            String indexColFamily = cfg.get(RestrictionConfig.INDEX_COLFAMILY, "Page1");
            String rasterColFamily = cfg.get(RestrictionConfig.RASTER_COLFAMILY, "Analysis");

            m_crossGetter = new CrossInformationVistor("Information",
                                                        rasterColFamily,
                                                        indexColFamily);

            m_crossGetter.prepare();
        }

        @Override
        protected void cleanup(Context context){
            m_crossGetter.shutdown();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values,
                              Context context)
                throws IOException, InterruptedException{

            StringBuilder sb = new StringBuilder(key.toString());
            String pidString = sb.reverse().toString();

            CrossRaster cross = m_crossGetter.getCrossesRaster(pidString);

            if(null == cross){
                return;
            }

            Configuration conf = context.getConfiguration();
            boolean isInter = conf.getBoolean(RestrictionConfig.NEED_INTERPOLATION, true);

            double[] pageEnvelope = cross.getPageEnvelope();

            PageMatrix page = new PageMatrix(pageEnvelope, RestricConfig.MercatorTileLevel);
            int[][] rasterBuffer = cross.getDenseRaster();

            outkey.set(cross.getPid());
            for(Text v : values){
                int[] track;
                String[] infos = v.toString().split("#");
                String trackid = infos[0];
                try {
                    track = jsonUtil.readIntArray(infos[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                int ptCount = track.length / 2;

                outValue.set(trackid + "#" + ptCount + "#" +
                            hitTrackInCross(rasterBuffer, page, isInter, track));

                context.write(outkey, outValue);
            }
        }

        public String hitTrackInCross(int[][] buffer, PageMatrix page,
                                      boolean isInter, int[] track)
                throws IOException, InterruptedException {

            String sequenceJson = "";

            int pagerowcount = buffer.length;
            if(0 == pagerowcount)
                return sequenceJson;

            int pagecolcount = buffer[0].length;
            if(0 == pagecolcount)
                return sequenceJson;

            if(isInter){
                sequenceJson = page.getTrackInRasterSequence(track,
                                                            buffer);
            } else{
                sequenceJson = page.getTrackInRasterPoints(track,
                                                            buffer);
            }

            return sequenceJson;
        }
    }

    public static class SimpleHitMapper
            extends Mapper<LongWritable, Text, LongWritable, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String[] trackhitsLine = value.toString().split("#");

            String trackid = trackhitsLine[0];
            int ptcount = Integer.parseInt(trackhitsLine[1]);

            String trackhitVals = trackhitsLine[2];
            short[] trackhits = JsonUtil.getInstance().readShortArray(trackhitVals);
            if(0 == trackhits.length)
                return;

            SimpleCountCluster encodeUtil = new SimpleCountCluster(255);
            for(short hit : trackhits){
                encodeUtil.insertHit(hit);
            }

            String simpleTrack = encodeUtil.getSimpleTrack();
            if(simpleTrack.isEmpty()) {
                context.getCounter(HitCounter.Empty_Path).increment(1);
                return;
            }

            context.write(key, new Text(trackid + "#" + ptcount + "#" + simpleTrack));
        }
    }

    public static class HitTrackDriver
            extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(3 > args.length){
                System.err.printf("Usage: %s <input> <output> <intergration> " +
                                "[indexColFamily] [rastercolfamily] [mapcount] [reducecount] \n",
                                getClass().getSimpleName());

                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            boolean isInter = Boolean.parseBoolean(args[2]);

            String indexCol = "Page1";
            if(args.length > 3){
                indexCol = args[3];
            }

            String rasterCol = "Analysis";
            if(args.length > 4){
                rasterCol = args[4];
            }

            int mapcount = 30;
            if(args.length > 5){
                mapcount = Integer.parseInt(args[5]);
            }

            int reducecount = 60;
            if(args.length > 6){
                reducecount = Integer.parseInt(args[6]);
            }

            log.info("Track hit in cross from:" + args[0] +
                    " to:" + args[1] + " with interpolation is:" + isInter +
                    "use index from:" + indexCol +
                    " use raster from:" + rasterCol +
                    " and mapcount is:" + mapcount +
                    " reducecount is:" + reducecount);

            Job job = getJob(conf, isInter, args[0], args[1],
                            indexCol, rasterCol, mapcount, reducecount);

            int res = job.waitForCompletion(true) ? 0 : 1;

            Counters counters = job.getCounters();
            Counter counter2=counters.findCounter(HitCounter.Cannot_Find_Cross);
            log.info(String.format("There are %d records could not find related cross",
                    counter2.getValue()));

            return res;
        }

        public static Job getJob(Configuration conf, boolean isInter,
                                 String inputPath, String outputPath,
                                 String indexColFamily,
                                 String rasterColFamily,
                                 int mapcount, int reducecount)
                throws Exception{

            conf.setBoolean(RestrictionConfig.NEED_INTERPOLATION, isInter);
            conf.set(RestrictionConfig.INDEX_COLFAMILY, indexColFamily);
            conf.set(RestrictionConfig.RASTER_COLFAMILY, rasterColFamily);

            long splitSize = Hdfs.CalMapReduceSplitSize(new String[]{inputPath},
                                                        FileSystem.get(conf),
                                                        "part", mapcount);

            conf.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(splitSize));

            Job job = Job.getInstance(conf,String.format("Hit track in crosses with inter is %b", isInter));

            job.setJarByClass(HitTrackDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(HitTrackMapper.class);

            ChainReducer.setReducer(job, HitTrackReducer.class,
                                    Text.class, Text.class,
                                    LongWritable.class, Text.class,
                                    conf);

            ChainReducer.addMapper(job, SimpleHitMapper.class,
                                    LongWritable.class, Text.class,
                                    LongWritable.class, Text.class,
                                    conf);

            job.setNumReduceTasks(reducecount);

            return job;
        }
    }

    public void azkabanRun(Properties props) throws Exception {
        String[] args = new String[7];

        args[0] = props.getProperty(RestrictionConfig.TRACKDATA_PATH);
        args[1] = props.getProperty(RestrictionConfig.HITDATA_PATH);
        args[2] = props.getProperty(RestrictionConfig.NEED_INTERPOLATION, "true");
        args[3] = props.getProperty(RestrictionConfig.INDEX_COLFAMILY, "Page1");
        args[4] = props.getProperty(RestrictionConfig.RASTER_COLFAMILY, "Analysis");
        args[5] = props.getProperty(RestrictionConfig.HIT_MAPCOUNT, "50");
        args[6] = props.getProperty(RestrictionConfig.HIT_REDUCECOUNT, "60");

        System.exit(ToolRunner.run(new HitTrackDriver(), args));
    }

    public static void main( String[] args )
            throws Exception {
        System.exit(ToolRunner.run(new HitTrackDriver(), args));
    }
}
