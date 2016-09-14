package com.navinfo.mapspotter.process.topic.poistat;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.model.AdminBlock;
import com.navinfo.mapspotter.foundation.model.PoiHang;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.analysis.poistat.AreaAnalysis;
import com.navinfo.mapspotter.process.analysis.poistat.BlockInfo;
import com.navinfo.mapspotter.process.analysis.poistat.BlocksAnalysis;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 搜索日志POI统计
 * Created by gaojian on 2016/1/25.
 */
public class PoiStatMR {
    private static final Logger logger = Logger.getLogger(PoiStatMR.class);

    public static class PoiStatMapper extends Mapper<LongWritable, Text, Text, Text> {
        private Set<String> meshes = null;
        private BlocksAnalysis blocksAnalysis = null;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            //String strSql = PropertiesUtil.getValue("AreaCount.sql");
            blocksAnalysis = new BlocksAnalysis();
            //blocksAnalysis.Initialize(PropertiesUtil.getValue("AreaCount.host") , PropertiesUtil.getValue("AreaCount.db"),
            //        PropertiesUtil.getValue("AreaCount.user") , PropertiesUtil.getValue("AreaCount.password") ,
            //        PropertiesUtil.getValue("AreaCount.port"));
            //blocksAnalysis.PrepareRtree(strSql);
            String blockfile = context.getConfiguration().get("blockfile");
            blocksAnalysis.prepareRTree_Json(blockfile , 1);
            ProvinceUtil provinceUtil = new ProvinceUtil();
            String meshlist = context.getConfiguration().get("meshlist");
            provinceUtil.initProvinceMeshes(meshlist , 1);
            meshes = new HashSet<>();
            meshes.addAll(provinceUtil.getProvinceMeshes("北京市"));
            meshes.addAll(provinceUtil.getProvinceMeshes("江苏省"));
            meshes.addAll(provinceUtil.getProvinceMeshes("青海省"));
            meshes.addAll(provinceUtil.getProvinceMeshes("宁夏回族自治区"));
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            //areaAnalysis.destroy();
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            PoiHang poiHang = PoiHang.parse(value.toString());
            if (poiHang == null) {
                logger.info(value.toString());
                return;
            }

            String mesh = MeshUtil.coordinate2Mesh(poiHang.getoLon(), poiHang.getoLat());
            if (!meshes.contains(mesh)) return;
            Geometry geometry = GeoUtil.createPoint(poiHang.getoLon(),poiHang.getoLat());
            Coordinate coordinate = geometry.getCoordinate();

            List<BlockInfo> rlist = blocksAnalysis.Contains(coordinate , geometry);
            if (rlist.isEmpty() || rlist.size() == 0){
                return;
            }

            String areaId = rlist.get(0).getBlockid();
            String output = "";
            if (StringUtil.isEmpty(poiHang.getPid())) {
                output = "0," + poiHang.getCount();
            } else {
                output = poiHang.getCount() + ",0";
            }

            context.write(new Text(areaId), new Text(output));
        }
    }

    public static class PoiStatReducer extends Reducer<Text, Text, NullWritable, Text> {
        private BlocksAnalysis blocksAnalysis = null;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);

            // 初始化区域分析类
            //String strSql = PropertiesUtil.getValue("AreaCount.sql");
            blocksAnalysis = new BlocksAnalysis();
            //blocksAnalysis.Initialize(PropertiesUtil.getValue("AreaCount.host") , PropertiesUtil.getValue("AreaCount.db"),
            //        PropertiesUtil.getValue("AreaCount.user") , PropertiesUtil.getValue("AreaCount.password") ,
            //        PropertiesUtil.getValue("AreaCount.port"));
            //blocksAnalysis.PrepareBlockInfoMap(strSql);
            String blockfile = context.getConfiguration().get("blockfile");
            blocksAnalysis.prepareMap_Json(blockfile, 1);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String areaId = key.toString();
            String result = "\"" + areaId + "\",";
            // 区域代码不为空，则给省市赋值
            BlockInfo adminBlock = blocksAnalysis.GetBlockInfo(areaId);
            if (adminBlock == null) {
                logger.error("adminBlock not found:" + areaId);
                return;
            }
            result += "\"" + adminBlock.getProvince() + "\",";
            result += "\"" + adminBlock.getCity() + "\",";
            result += "\"" + adminBlock.getCounty() + "\",";
            result += "\"" + adminBlock.getArea() + "\",";

            int count1 = 0;
            int count2 = 0;
            for (Text value : values) {
                String[] counts = value.toString().split(",");
                count1 += Integer.parseInt(counts[0]);
                count2 += Integer.parseInt(counts[1]);
            }
            result += count1 + "," + count2;

            context.write(NullWritable.get(), new Text(result));
        }
    }

    public static class PoiStatDriver extends Configured implements Tool{
        @Override
        public int run(String[] args) throws Exception{
            String inputPath = args[0];
            String outputPath = args[1];
            String blockfile = args[2];
            String meshlist = args[3];
            long splitCount = Long.parseLong(args[4]);

            logger.info("inputPath : " + inputPath + " outputPath : " + outputPath);
            logger.info("blockfile : " + blockfile + " meshlist : " + meshlist);
            logger.info("splitcount : " + args[4]);

            Configuration conf = new Configuration();
            conf.set("blockfile" , blockfile);
            conf.set("meshlist" , meshlist);

            Job job = Job.getInstance(conf, "PoiStatMR");
            job.setJarByClass(PoiStatMR.class);
            job.setMapperClass(PoiStatMapper.class);
            job.setReducerClass(PoiStatReducer.class);
            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setNumReduceTasks(1);

            FileInputFormat.addInputPath(job, new Path(inputPath));
            // 计算分割文件大小，控制map个数
            long totalSize = Hdfs.getFileSize(conf, inputPath);
            long splitSize = totalSize / splitCount;
            FileInputFormat.setMaxInputSplitSize(job, splitSize);

            if (Hdfs.deleteIfExists(conf, outputPath)) {
                System.out.println("存在此输出路径，已删除！！！");
            }
            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new PoiStatDriver() , args));
    }

    /**
     * azkaban调度接口
     * @param inputPath  统计下挂后POI数据
     * @param outputPath 统计输出路径
     * @param blockfile  统计block数据
     * @param meshlist   分省图幅列表
     * @param splitcount Mapper个数
     * @throws Exception
     */
    public void azkabanRun(String inputPath , String outputPath , String blockfile , String meshlist , String splitcount) throws Exception{
        ToolRunner.run(new PoiStatDriver() , new String[]{inputPath , outputPath , blockfile,meshlist,splitcount});
    }
}
