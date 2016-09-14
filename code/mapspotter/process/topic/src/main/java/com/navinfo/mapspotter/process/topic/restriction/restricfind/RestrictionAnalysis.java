package com.navinfo.mapspotter.process.topic.restriction.restricfind;

import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.process.topic.restriction.CrossRaster;
import com.navinfo.mapspotter.process.topic.restriction.Link;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;


/**
 * 交限分析MR
 * Created by SongHuiXing on 2016/1/15.
 */
public class RestrictionAnalysis {

    enum DataCounter {
        TooLessOfTrack,
        ChangedCross,
        NewRestriction,
        DeletedRestriction,
    }

    public static class RestrictionAnalysisMapper
            extends Mapper<LongWritable, Text, LongWritable, Text> {

        private LongWritable outkey = new LongWritable();
        private Text outvalue = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String hitResultLine = value.toString().trim();
            if(hitResultLine.isEmpty())
                return;

            String[] hitResultArray = hitResultLine.split("\t");
            if(2 != hitResultArray.length)
                return;

            long crossPid = Long.parseLong(hitResultArray[0]);
            outkey.set(crossPid);

            String[] trackLine = hitResultArray[1].split("#");

            int ptcount = Integer.parseInt(trackLine[1]);
            String simplePath = trackLine[2];
            outvalue.set(ptcount + "#" + simplePath);

            context.write(outkey, outvalue);
        }

    }

    public static class RestrictionAnalysisReducer
            extends Reducer<LongWritable, Text, LongWritable, Text> {

        private CrossInformationVistor m_crossGetter;

        private OracleDatabase oraSource = null;
        private PreparedStatement stmt = null;

        private static final String queryMeshSql = "SELECT ADMINCODE, PROVINCE FROM ni_meshlist_for_dn " +
                                                    "WHERE MESH=?";

        private JsonUtil m_jackUtil = JsonUtil.getInstance();

        private Text outvalue = new Text();

        private int minTrackCount = 0;

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            minTrackCount = cfg.getInt(RestrictionConfig.MINTRACK_COUNT, 50);
            String infoCol = cfg.get(RestrictionConfig.BASEINFO_COLFAMILY, "Information");
            String rasterCol = cfg.get(RestrictionConfig.RASTER_COLFAMILY, "Analysis");

            m_crossGetter = new CrossInformationVistor(infoCol, rasterCol, null);
            m_crossGetter.prepare();

            oraSource = (OracleDatabase)DataSource.getDataSource(
                                                    IOUtil.makeOracleParams("192.168.4.166",
                                                    1521,
                                                    "orcl",
                                                    "gdb_16sum2",
                                                    "zaq1"));

            try {
                stmt = oraSource.prepare(queryMeshSql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void cleanup(Context context){
            m_crossGetter.shutdown();

            if(null != oraSource) {
                oraSource.close();
            }

            if(null != stmt){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException{

            long crosspid = key.get();

            RestrictionAnalysisResult result = new RestrictionAnalysisResult();
            result.CrossPid = crosspid;

            result.OriginalResMatrix = m_crossGetter.getOriginalRestrictionMatrix(crosspid);

            if(null == result.OriginalResMatrix)
                return;

            CrossRestrictionAnalyzer resAnalyzer =
                    new CrossRestrictionAnalyzer(crosspid,
                                                result.OriginalResMatrix);

            long trackCounter = 0;
            for(Text track : values){
                trackCounter++;

                String[] trackInfo = track.toString().split("#");

                int ptCount = Integer.parseInt(trackInfo[0]);
                String simpleTrack = trackInfo[1];

                resAnalyzer.insertTrack(simpleTrack, ptCount);
            }

            result.TotalTrackCount = trackCounter;
            result.PerfectTrackCount = resAnalyzer.getRecordTrackCount();

            if(result.PerfectTrackCount < minTrackCount){
                context.getCounter(DataCounter.TooLessOfTrack).increment(1);
                return;
            }

            result.MeshId = m_crossGetter.getCrossMeshID(crosspid);

            Map.Entry<Integer, String> admininfo = getProvince4Mesh(result.MeshId);
            if(null != admininfo){
                result.AdminId = admininfo.getKey();
                result.AdminName = admininfo.getValue();
            }

            result.AnalysisResMatrix = resAnalyzer.getAnalyzeRestriction().toIntArray2();

            CrossRaster raster = m_crossGetter.getCrossesRaster(crosspid);
            if(null == raster)
                return;

            DoubleMatrix bufMx = raster.getMatrix();

            result.AnalysisDeleteWeights =
                    resAnalyzer.getDeleteWeight(bufMx).toIntArray2();

            result.AnalysisNewWeights = resAnalyzer.getNewWeight(bufMx).toIntArray2();

            ArrayList<BaseCrossJsonModel.TurnFromChild> turnFromChild =
                                        m_crossGetter.getChildrenTurns(crosspid);
            DoubleMatrix turnInChildMx = DoubleMatrix.zeros(result.OriginalResMatrix.length,
                                                            result.OriginalResMatrix.length);
            for (BaseCrossJsonModel.TurnFromChild turn : turnFromChild){
                turnInChildMx.put(turn.inLinkIndex, turn.outLinkIndex, 1);
            }

            DoubleMatrix newMx = resAnalyzer.getNewRestrictions(turnInChildMx);
            result.NewRestrictions = newMx.toIntArray2();

            int realHas = newMx.findIndices().length;
            context.getCounter(DataCounter.NewRestriction).increment(realHas);

            DoubleMatrix theoryTurnMx = DoubleMatrix.zeros(result.OriginalResMatrix.length,
                                                            result.OriginalResMatrix.length);
            long[] theoryTurn = m_crossGetter.getTheoryForbiddenTurn4Child(crosspid);
            if(null != theoryTurn){
                List<Link> links = m_crossGetter.getLinks(crosspid);
                int inIndex = getIndex(links, theoryTurn[0]);
                int outIndex = getIndex(links, theoryTurn[1]);
                if(inIndex >= 0 && outIndex >= 0){
                    theoryTurnMx.put(inIndex, outIndex, 1);
                }
            }
            DoubleMatrix releaseMx = resAnalyzer.getReleasedRestrictions(theoryTurnMx);
            result.ReleaseRestrictions = releaseMx.toIntArray2();
            int originalHas = releaseMx.findIndices().length;
            context.getCounter(DataCounter.DeletedRestriction).increment(originalHas);

            context.getCounter(DataCounter.ChangedCross).increment(realHas + originalHas);

            outvalue.set(m_jackUtil.write2String(result));
            context.write(key, outvalue);
        }

        private Map.Entry<Integer, String> getProvince4Mesh(int meshid) {
            Map.Entry<Integer, String> adminInfo = null;

            if(null == stmt)
                return adminInfo;

            SqlCursor cursor = null;
            try{
                cursor = oraSource.query(stmt, meshid);

                while (cursor.next()) {
                    int adminid = cursor.getInteger(1);
                    String adminname = cursor.getString(2);

                    adminInfo = new AbstractMap.SimpleEntry<>(adminid, adminname);
                }

            } catch (SQLException sqlE){
                sqlE.printStackTrace();
            } finally {
                if(null != cursor){
                    try {
                        cursor.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            return adminInfo;
        }

        private static int getIndex(List<Link> links, long pid){
            for (int i = 0; i < links.size(); i++) {
                if(links.get(i).getPid() == pid)
                    return i;
            }

            return -1;
        }
    }

    public static class RestrictionAnalysisDriver
            extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(2 > args.length){
                System.err.printf("Usage: %s <input> <output> [min_track_count] " +
                                "[BaseInfoColFamily] [RasterColFamily]\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            Configuration conf = getConf();

            int minTrackCount = 50;
            if(args.length > 2) {
                minTrackCount = Integer.parseInt(args[2]);
            }

            String infoCol = "Information";
            if(args.length > 3){
                infoCol = args[3];
            }

            String rasterCol = "Analysis";
            if(args.length > 4){
                rasterCol = args[4];
            }

            System.out.println(String.format("Run analysis from %s to %s with min:%d", args[0], args[1], minTrackCount));

            Job job = getJob(conf, args[0], args[1], infoCol, rasterCol, minTrackCount);

            return job.waitForCompletion(true) ? 0 : 1;
        }

        public static Job getJob(Configuration conf,
                                 String inputPath,
                                 String outputPath,
                                 String infoColFamily,
                                 String rasterColFamily,
                                 int minTrackCount) throws Exception {
            conf.setInt(RestrictionConfig.MINTRACK_COUNT, minTrackCount);
            conf.set(RestrictionConfig.BASEINFO_COLFAMILY, infoColFamily);
            conf.set(RestrictionConfig.RASTER_COLFAMILY, rasterColFamily);

            Job job = Job.getInstance(conf, "Analysis cross restrictions");
            job.setJarByClass(RestrictionAnalysisDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            job.setMapOutputKeyClass(LongWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(RestrictionAnalysisMapper.class);

            job.setOutputKeyClass(LongWritable.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(RestrictionAnalysisReducer.class);

            job.setNumReduceTasks(20);

            return job;
        }

    }

    public static void main( String[] args ) throws Exception{
        System.exit(ToolRunner.run(new RestrictionAnalysisDriver(), args));
    }
}
