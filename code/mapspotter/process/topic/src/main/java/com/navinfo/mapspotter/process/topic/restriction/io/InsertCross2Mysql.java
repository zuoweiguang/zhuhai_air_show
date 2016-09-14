package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.geojson.Feature;

import java.io.IOException;
import java.util.List;

/**
 * MapReduce 将路口信息存储到Mysql
 * Created by SongHuiXing on 2016/2/18.
 */
public class InsertCross2Mysql {
    enum InsertStatus{
        Success,
        Fail,
    }

    private static final Log s_logger = LogFactory.getLog(InsertCross2HBase.class);

    public static class InsertCrossMapper
            extends Mapper<LongWritable, Text, NullWritable, IntWritable> {

        private RoadRasterSupplier roadRasterSupplier = new RoadRasterSupplier("road_raster", "1606");
        private CrossRasterFactory factory = null;

        private JsonUtil m_jsonUtil = JsonUtil.getInstance();

        private SqlCrossVistor vistor = new SqlCrossVistor("192.168.4.128",
                                                            "navinfo",
                                                            3306,
                                                            "reynold",
                                                            "1qaz");

        @Override
        protected void setup(Context context){
            vistor.prepare();

            factory = new CrossRasterFactory(14, 1024, roadRasterSupplier);
            factory.prepare();
        }

        @Override
        protected void cleanup(Context context){
            vistor.shutdown();
            factory.shutdown();
            factory = null;
        }

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            BaseCrossJsonModel crossJson = BaseCrossJsonModel.readCrossJson(value.toString());

            List<Node> nodes = Node.convert(m_jsonUtil.readCollection(crossJson.getNodes(), Feature.class));
            List<Link> links = Link.convert(m_jsonUtil.readCollection(crossJson.getLinks(), Feature.class));

            try {
                long pid = crossJson.getPID();

                CrossRaster raster = factory.buildRaster(pid, links, nodes);

                if(!vistor.insertCrossIndex(pid, raster.getPageEnvelope()) ||
                    !vistor.insertCrossInfo(crossJson, raster)){
                    s_logger.warn(String.format("Insert cross to MySql failed: %d", pid));
                    context.getCounter(InsertStatus.Fail).increment(1);
                    return;
                }

            } catch (ArrayIndexOutOfBoundsException e){
                s_logger.error(String.format("Invalid Cross: %d", crossJson.getPID()));
                throw e;
            }

            context.getCounter(InsertStatus.Success).increment(1);
        }
    }

    public static class InsertCrossDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(args.length < 1){
                System.err.printf("Usage: %s [generic option] <input path>\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            Job job = getJob(getConf(), args[0]);

            boolean sucess = job.waitForCompletion(true);

            if(sucess){
                createIndex4Position();
            }

            return job.waitForCompletion(true) ? 0 : 1;
        }

        private boolean createIndex4Position(){
            SqlCrossVistor vistor = new SqlCrossVistor("192.168.4.128",
                    "navinfo",
                    3306,
                    "reynold",
                    "1qaz");

            if(!vistor.prepare())
                return false;

            boolean suc = vistor.createSpatialIndex4Info();

            vistor.shutdown();

            return suc;
        }

        public static Job getJob(Configuration cfg, String inputPath) throws IOException {

            Configuration hbaseCfg = CrossInformationVistor.getHBaseConfig();
            cfg.addResource(hbaseCfg);

            cfg.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{inputPath},
                            FileSystem.get(cfg),
                            "Cross",
                            8)));

            Job job = Job.getInstance(cfg, "Insert Cross info to MySql");

            job.setJarByClass(InsertCrossDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));

            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(IntWritable.class);
            job.setMapperClass(InsertCrossMapper.class);

            job.setOutputFormatClass(NullOutputFormat.class);

            job.setNumReduceTasks(0);

            return job;
        }

    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new InsertCrossDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
