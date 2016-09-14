package com.navinfo.mapspotter.process.topic.coverage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.process.analysis.poistat.BlockInfo;
import com.navinfo.mapspotter.process.analysis.poistat.BlocksAnalysis;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huanghai on 2016/3/3.
 * <p/>
 * <p/>
 * create PROCEDURE MS_BEIJINGCOUNT (PARA1 in VARCHAR2,PARA2 out VARCHAR2) is
 * BEGIN
 * SELECT max(t1.mapid) into PARA2
 * FROM ms_4city_blocks t1
 * WHERE SDO_ANYINTERACT(t1.Geometry,SDO_GEOMETRY(PARA1,8307))='TRUE';
 * END MS_BEIJINGCOUNT;
 */
public class BlockAreaCoverageMR extends Configured implements Tool {
    private static final Logger logger = Logger.getLogger(BlockAreaCoverageMR.class);

    public void azkabanRun(String coverageLinkPath, String inputPath, String outputPath, String blockfilePath) throws Exception {
        int res = ToolRunner.run(new Configuration(), new BlockAreaCoverageMR(), new String[]{coverageLinkPath, inputPath, outputPath, blockfilePath});
        System.exit(res);
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new BlockAreaCoverageMR(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String coverageLinkPath = args[0];
        String inputPath = args[1];
        String outputPath = args[2];
        String blockfile = args[3];
        logger.info("inputPath : " + inputPath + " outputPath : " + outputPath + " coverageLinkPath : " + coverageLinkPath + " blockfile : " + blockfile);

        Configuration conf = getConf();
        conf.set("coverageLinkPath", coverageLinkPath);
        conf.set("blockfile", blockfile);

        Job job = Job.getInstance(conf, "BlockAreaCoverageMR");
        job.setJarByClass(BlockAreaCoverageMR.class);

        job.setMapperClass(MileageCoverageMapper.class);
        job.setReducerClass(MileageCoverageReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);

        job.setInputFormatClass(TextInputFormat.class);

        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        return job.waitForCompletion(true) ? 0 : 1;
    }


    /**
     * 计算link覆盖率和link里程覆盖率
     */
    public static class MileageCoverageMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        Text keyText = new Text();
        DoubleWritable outVal = new DoubleWritable();
        Map<Integer, Boolean> pidMap = new HashMap<Integer, Boolean>();
        BlocksAnalysis blocksAnalysis = new BlocksAnalysis();


        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String coverageLinkPath = conf.get("coverageLinkPath");
            FileSystem fileSystem = FileSystem.get(conf);
            InputStream fsDataInputStream = fileSystem.open(new Path(coverageLinkPath));
            BufferedReader bReader = new BufferedReader(new InputStreamReader(fsDataInputStream));
            String pid;
            while ((pid = bReader.readLine()) != null) {
                pidMap.put(Integer.parseInt(pid), true);
            }
            // 获取areamap
            String blockfile = context.getConfiguration().get("blockfile");
            logger.info("blockfile : " + blockfile);
            blocksAnalysis.prepareRTree_Json(blockfile, 1);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            JSONObject jsonObject = JSON.parseObject(value.toString());
            JSONObject properties = jsonObject.getJSONObject("properties");
            int pid = properties.getIntValue("link_pid");
            int functionclass = properties.getIntValue("functionclass");
            String regionId = properties.getString("r_region");
            JSONArray jsonArray = jsonObject.getJSONObject("geometry").getJSONArray("coordinates");

            // 选取link的起始点
            JSONArray coord = (JSONArray) jsonArray.get(0);
            List<BlockInfo> blockInfos = blocksAnalysis.Contains(new Coordinate(coord.getDoubleValue(0), coord.getDoubleValue(1)), GeoUtil.createPoint(coord.getDoubleValue(0), coord.getDoubleValue(1)));

            double length = properties.getDouble("length");
            Boolean aBoolean = pidMap.get(pid);

            if (blockInfos != null && blockInfos.size() >= 1) {
                String mapId = blockInfos.get(0).getArea();
                // 被覆盖link
                if (aBoolean != null && aBoolean) {
                    // 按区域
                    keyText.set("COV|" + mapId);
                    outVal.set(length);
                    context.write(keyText, outVal);
                    // 道路等级
                    keyText.set("COV|" + mapId + "|" + functionclass);
                    outVal.set(length);
                    context.write(keyText, outVal);

                    // link数目
                    keyText.set("COVN|" + mapId);
                    outVal.set(1);
                    context.write(keyText, outVal);

                    keyText.set("COVFCN|" + mapId + "|" + functionclass);
                    outVal.set(1);
                    context.write(keyText, outVal);
                }
                // 所有link
                keyText.set("ALL|" + mapId);
                outVal.set(length);
                context.write(keyText, outVal);
                // 道路等级
                keyText.set("ALL|" + mapId + "|" + functionclass);
                outVal.set(length);
                context.write(keyText, outVal);
                // link数目
                keyText.set("ALLN|" + mapId);
                outVal.set(1);
                context.write(keyText, outVal);

                keyText.set("ALLFCN|" + mapId + "|" + functionclass);
                outVal.set(1);
                context.write(keyText, outVal);
            }
        }
    }


    public static class MileageCoverageReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        DoubleWritable outVal = new DoubleWritable();

        @Override
        protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double total = 0;
            for (DoubleWritable value : values) {
                total += value.get();
            }
            outVal.set(total);
            context.write(key, outVal);
        }
    }

}
