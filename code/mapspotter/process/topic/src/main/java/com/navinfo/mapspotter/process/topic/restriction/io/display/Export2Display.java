package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.navinfo.mapspotter.process.topic.restriction.Link;
import com.navinfo.mapspotter.process.topic.restriction.Node;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionConfig;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;
import com.navinfo.mapspotter.process.topic.restriction.restricfind.RestrictionAnalysisResult;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.geojson.Feature;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 输出交限分析结果，以便展示
 * Created by SongHuiXing on 2016/1/18.
 */
public class Export2Display {
    public static class ExportDiffRestricMapper
            extends Mapper<LongWritable, Text, Text, Text> {

        private static double deatil_offset = 0.00002;

        private CrossInformationVistor m_crossGetter;
        private JsonUtil m_jsonUtil = JsonUtil.getInstance();

        private Text outKey = new Text();
        private Text outValue = new Text();

        @Override
        protected void setup(Context context){
            m_crossGetter =
                    new CrossInformationVistor(
                            context.getConfiguration().get(RestrictionConfig.BASEINFO_COLFAMILY),
                            null,
                            null);

            m_crossGetter.prepare();
        }

        @Override
        protected void cleanup(Context context){
            m_crossGetter.shutdown();
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException{

            String hitResultLine = value.toString().trim();
            if(hitResultLine.isEmpty())
                return;

            String[] hitResultArray = hitResultLine.split("\t");
            if(2 != hitResultArray.length)
                return;

            RestrictionAnalysisResult resAnalysisResult =
                    m_jsonUtil.readValue(hitResultArray[1], RestrictionAnalysisResult.class);

            long crosspid = resAnalysisResult.CrossPid;

            RestricCross displayCross = new RestricCross();
            displayCross.setCrossId(crosspid);
            displayCross.setProvinceId(resAnalysisResult.AdminId);

            BaseCrossJsonModel crossBasicInfo = m_crossGetter.getNodeAndLinks(crosspid);

            List<Node> nodes = Node.convert(m_jsonUtil.readCollection(crossBasicInfo.getNodes(), Feature.class));

            Point crossPt = nodes.get(0).getPoint();
            displayCross.setMainPoint(crossPt);

            LngLatAlt coord = crossPt.getCoordinates();
            Coordinate coordpt = new Coordinate(coord.getLongitude(), coord.getLatitude());
            displayCross.setTileCode(MercatorUtil.lonLat2MCode(coordpt, 12), 12);

            displayCross.setTiles(getTileCodes(coordpt, 8, 16));

            List<int[]> linkDirs = m_jsonUtil.readIntMatrix(crossBasicInfo.getLinkDirection());

            int[] inLinkDirs = linkDirs.get(0);     //link作为进入线的方向
            int[] outLinkDirs = linkDirs.get(1);    //link作为退出线的方向

            List<Link> links = Link.convert(m_jsonUtil.readCollection(crossBasicInfo.getLinks(), Feature.class));

            int newcount = 0, delcount = 0;

            //解除
            for(int row=0;row<resAnalysisResult.ReleaseRestrictions.length;row++){
                int[] rowValues = resAnalysisResult.ReleaseRestrictions[row];

                for(int col=0;col < rowValues.length; col++){
                    int v = rowValues[col];
                    if(0 == v)
                        continue;

                    delcount++;

                    int resdir = resAnalysisResult.OriginalResMatrix[row][col];
                    resdir = resdir % 10;

                    int confidence = resAnalysisResult.AnalysisDeleteWeights[row][col];

                    RestricDetail findRestric = buildDetail(links.get(row),
                                                            inLinkDirs[row],
                                                            links.get(col),
                                                            outLinkDirs[col],
                                                            resdir,
                                                            confidence);

                    findRestric.setCrossId(crosspid);
                    findRestric.setProvinceId(resAnalysisResult.AdminId);

                    outKey.set(String.format("_%d", crosspid));
                    outValue.set(m_jsonUtil.write2String(findRestric));
                    context.write(outKey, outValue);
                }
            }
            displayCross.setDeleteCount(delcount);

            //新增
            for(int row=0;row<resAnalysisResult.NewRestrictions.length;row++){
                int[] rowValues = resAnalysisResult.NewRestrictions[row];

                for(int col=0;col < rowValues.length; col++){
                    int v = rowValues[col];
                    if(0 == v)
                        continue;

                    newcount++;

                    int confidence = resAnalysisResult.AnalysisNewWeights[row][col];

                    RestricDetail findRestric = buildDetail(links.get(row),
                                                            inLinkDirs[row],
                                                            links.get(col),
                                                            outLinkDirs[col],
                                                            -1,
                                                            confidence);

                    findRestric.setCrossId(crosspid);
                    findRestric.setProvinceId(resAnalysisResult.AdminId);

                    outKey.set(String.format("_%d", crosspid));
                    outValue.set(m_jsonUtil.write2String(findRestric));
                    context.write(outKey, outValue);
                }
            }
            displayCross.setNewCount(newcount);

            if(delcount > 0) {
                outKey.set(String.format("%d", crosspid));
                outValue.set(m_jsonUtil.write2String(displayCross));
                context.write(outKey, outValue);
            }
        }

        private RestricDetail buildDetail(Link in, int indir, Link out, int outdir,
                                          int res, int confidence){
            RestricDetail findRestric = new RestricDetail();

            com.vividsolutions.jts.geom.LineString inLink = in.getLineString(indir == 2);
            com.vividsolutions.jts.geom.LineString outLink = out.getLineString(outdir == 2);

            if(res == -1){  //新增交限
                findRestric.setIsDelete(false);
                res = SpatialUtil.calculateDirectInfo(inLink,outLink);
            } else {
                findRestric.setIsDelete(true);
            }

            findRestric.setRestricDir(res);

            findRestric.setConfidence(confidence);

            findRestric.setInLinkId(in.getPid());
            findRestric.setInLinkGeo(inLink);

            findRestric.setOutLinkId(out.getPid());
            findRestric.setOutLinkGeo(outLink);

            Coordinate coord = inLink.getEndPoint().getCoordinate();
            Coordinate coord1 = inLink.getPointN(inLink.getNumPoints()-2).getCoordinate();

            //TODO:调压盖
            double theta = SpatialUtil.getAngleWithNorth(coord1, coord);

            Point displayPoint = new Point(coord.x + deatil_offset*Math.cos(theta),
                                           coord.y - deatil_offset*Math.sin(theta));

            findRestric.setDisplayPoint(displayPoint);

            findRestric.setTileCode(MercatorUtil.lonLat2MCode(coord, 14), 14);
            findRestric.setTiles(getTileCodes(coord, 10, 18));

            return findRestric;
        }

        private List<String> getTileCodes(Coordinate pt, int minlevel, int maxlevel){
            ArrayList<String> tiles = new ArrayList<>(maxlevel - minlevel + 1);

            for (int i = minlevel; i <= maxlevel; i++) {
                tiles.add(String.format("%s_%d",
                                        MercatorUtil.lonLat2MCode(pt, i),
                                        i)
                         );
            }

            return tiles;
        }
    }

    public static class ExportDiffPartioner extends Partitioner<Text, Text> {

        @Override
        public int getPartition(Text arg0, Text arg1, int num) {
            String key = arg0.toString();

            if(!key.startsWith("_")){
                Long pid = Long.parseLong(key);

                return pid.hashCode() % 2;

            } else {
                Long pid = Long.parseLong(key.substring(1, key.length()));

                return pid.hashCode() % (num - 2) + 2;
            }
        }
    }

    public static class ExportDiffDisplayReducer extends Reducer<Text, Text, NullWritable, Text>{

        private MultipleOutputs<NullWritable, Text> multipleOutputs;

        @Override
        protected void setup(Context context){
            multipleOutputs = new MultipleOutputs<>(context);
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String skey = key.toString();

            for (Text t : values){
                multipleOutputs.write(NullWritable.get(), t, getFileName(skey));
            }
        }

        private String getFileName(String key){
            if(!key.startsWith("_")){
                return "Cross/cross";

            } else {
                return "Detail/detail";
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            multipleOutputs.close();
        }
    }

    public static class ExportDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if (2 != args.length) {
                System.err.printf("Usage: %s <input> <output> [InfoColFamily]\n", getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            Configuration conf = getConf();

            String infoCol = "Information";
            if(args.length > 2){
                infoCol = args[2];
            }

            Job job = getJob(conf, args[0], args[1], infoCol);

            return job.waitForCompletion(true) ? 0 : 1;
        }

        public static Job getJob(Configuration conf,
                                 String input, String output,
                                 String infoCol) throws IOException {

            conf.set(RestrictionConfig.BASEINFO_COLFAMILY, infoCol);

            Job job = Job.getInstance(conf, "Collect different restrictions");
            job.setJarByClass(ExportDriver.class);

            FileInputFormat.addInputPath(job, new Path(input));
            FileOutputFormat.setOutputPath(job, new Path(output));

            LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(ExportDiffRestricMapper.class);

            job.setPartitionerClass(ExportDiffPartioner.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(ExportDiffDisplayReducer.class);

            job.setNumReduceTasks(10);

            return job;
        }

    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new ExportDriver(), args));
    }
}
