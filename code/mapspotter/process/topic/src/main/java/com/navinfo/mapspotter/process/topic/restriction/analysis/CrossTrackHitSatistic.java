package com.navinfo.mapspotter.process.topic.restriction.analysis;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.CrossRaster;
import com.navinfo.mapspotter.process.topic.restriction.Link;
import com.navinfo.mapspotter.process.topic.restriction.PageMatrix;
import com.navinfo.mapspotter.process.topic.restriction.RestricConfig;
import com.navinfo.mapspotter.process.topic.restriction.io.BaseCrossJsonModel;
import com.navinfo.mapspotter.process.topic.restriction.io.CrossInformationVistor;
import com.navinfo.mapspotter.process.topic.restriction.trackhit.HitTrackInCross;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.geojson.Feature;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * 统计路口的打点情况
 * Created by SongHuiXing on 2016/2/17.
 */
public class CrossTrackHitSatistic {
    private static String s_interConfName = "IsInter";
    private static final String BASEINFO_COLFAMILY = "Restriction.BaseInfocolfamily";
    private static final String RASTER_COLFAMILY = "Restriction.Rastercolfamily";
    private static String s_meshlistFile = "MESHLIST";

    public static class CrossHitStaticReducer extends Reducer<Text, Text, NullWritable, Text> {

        private CrossInformationVistor vistor;

        private JsonUtil jsonUtil = JsonUtil.getInstance();

        private Text outValue = new Text();

        private HashMap<String, String> mesh_province = null;

        @Override
        protected void setup(Context context) {
            Configuration cfg = context.getConfiguration();

            String meshfile = cfg.get(s_meshlistFile, "");

            if(!meshfile.isEmpty()){
                try {
                    FileSystem fs = FileSystem.get(cfg);

                    mesh_province = readMeshInfo(meshfile, fs);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            vistor = new CrossInformationVistor(cfg.get(BASEINFO_COLFAMILY),
                                                cfg.get(RASTER_COLFAMILY),
                                                null);

            vistor.prepare();
        }

        private HashMap<String, String> readMeshInfo(String filepath, FileSystem fs){
            HashMap<String, String> meshinfo = new HashMap<>();

            try {
                FSDataInputStream in = fs.open(new Path(filepath));

                InputStreamReader input = new InputStreamReader(in);

                BufferedReader reader = new BufferedReader(input);

                String lineTxt = null;
                while (null != (lineTxt = reader.readLine())){
                    String[] infos = lineTxt.split(",");

                    if(4 != infos.length)
                        continue;

                    String meshid = infos[0];
                    meshid = meshid.substring(meshid.indexOf('\"')+1, meshid.length()-1);

                    String province = infos[2];
                    province = province.substring(province.indexOf('\"')+1, province.length()-1);

                    meshinfo.put(meshid, province);
                }

                reader.close();
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return meshinfo;
        }

        @Override
        protected void cleanup(Context context) {
            vistor.shutdown();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            StringBuilder sb = new StringBuilder(key.toString());
            String crosspid = sb.reverse().toString();

            BaseCrossJsonModel crossJsonModel = vistor.getCrossInfomation(crosspid);

            if(!mesh_province.containsKey(crossJsonModel.getMesh()))
                return;

            CrossRaster cross = vistor.getCrossesRaster(crosspid);
            if(null == cross)
                return;

            CrossHitInfomation hitInfomation = new CrossHitInfomation();

            hitInfomation.mesh = crossJsonModel.getMesh();
            hitInfomation.province = mesh_province.get(hitInfomation.mesh);

            hitInfomation.pid = cross.getPid();

            CrossHitInfomation.Point2d ptll = new CrossHitInfomation.Point2d();
            ptll.longitude = cross.getPageEnvelope()[0];
            ptll.latitude = cross.getPageEnvelope()[1];
            hitInfomation.envelope.put("ptll", ptll);

            CrossHitInfomation.Point2d ptur = new CrossHitInfomation.Point2d();
            ptur.longitude = cross.getPageEnvelope()[2];
            ptur.latitude = cross.getPageEnvelope()[3];
            hitInfomation.envelope.put("ptur", ptur);

            double[] linkenv = jsonUtil.readDoubleArray(crossJsonModel.getLinkenvelope());
            CrossHitInfomation.Point2d ptll1 = new CrossHitInfomation.Point2d();
            ptll1.longitude = linkenv[0];
            ptll1.latitude = linkenv[1];
            hitInfomation.links.put("ptll", ptll1);

            CrossHitInfomation.Point2d ptur1 = new CrossHitInfomation.Point2d();
            ptur1.longitude = linkenv[2];
            ptur1.latitude = linkenv[3];
            hitInfomation.links.put("ptur", ptur1);

            List<Link> links = Link.convert(jsonUtil.readCollection(crossJsonModel.getLinks(), Feature.class));
            for (int i = 0; i < links.size(); i++) {
                hitInfomation.link_inx_pid.put(i, links.get(i).getPid());
            }

            DoubleMatrix raster = cross.getMatrix();

            DoubleMatrix cross_pixels = new DoubleMatrix(raster.eq(255));
            hitInfomation.cross_pixels = cross_pixels.toIntArray2();

            hitInfomation.restrictions = vistor.getOriginalRestrictionMatrix(cross.getPid());

            hitInfomation.tileInfo = cross.getCornerTilePos();

            Configuration conf = context.getConfiguration();
            boolean isInter = conf.getBoolean(s_interConfName, true);

            PageMatrix page = new PageMatrix(cross.getPageEnvelope(), RestricConfig.MercatorTileLevel);

            NullWritable outKey = NullWritable.get();

            try {
                for (Text v : values) {
                    int[] track;
                    String[] infos = v.toString().split("#");
                    try {
                        track = jsonUtil.readIntArray(infos[2]);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    TrackHitInfo hit;
                    if(!isInter){
                        hit = page.getTrackHittedPoints(track, raster.toIntArray2());
                    }else{
                        hit = page.getTrackHittedCoords(track, raster.toIntArray2());
                    }
                    hit.trackId = infos[0];

                    hitInfomation.traj.add(hit);
                }

                outValue.set(jsonUtil.write2String(hitInfomation));
                context.write(outKey, outValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class CrossHitStaticDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if (3 > args.length) {
                System.err.printf("Usage: %s <input> <output> <intergration> " +
                                    "[InfoColFamily] [RasterColFamily] [meshlist]\n",
                                    getClass().getSimpleName());

                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            Configuration conf = getConf();

            boolean isInter = Boolean.parseBoolean(args[2]);
            conf.setBoolean(s_interConfName, isInter);

            if(args.length > 3){
                conf.set(BASEINFO_COLFAMILY, args[3]);
            } else {
                conf.set(BASEINFO_COLFAMILY, "Information");
            }

            if(args.length > 4){
                conf.set(RASTER_COLFAMILY, args[4]);
            } else {
                conf.set(RASTER_COLFAMILY, "Analysis");
            }

            String meshlist_file = "";
            if(args.length > 5){
                meshlist_file = args[5];
            }
            conf.set(s_meshlistFile, meshlist_file);

            Job job = Job.getInstance(conf,
                    String.format("Statistic Cross Trackhit with inter is %b", isInter));

            job.setJarByClass(getClass());

            FileInputFormat.addInputPath(job, new Path(args[0]));
            FileOutputFormat.setOutputPath(job, new Path(args[1]));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(HitTrackInCross.HitTrackMapper.class);

            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            job.setReducerClass(CrossHitStaticReducer.class);

            job.setNumReduceTasks(6);

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new CrossHitStaticDriver(), args));
    }
}
