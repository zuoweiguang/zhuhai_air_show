package com.navinfo.mapspotter.process.topic.trackmap;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.process.analysis.poistat.AreaCountReducer;
import com.navinfo.mapspotter.process.analysis.poistat.AreaCountResult;
import com.navinfo.mapspotter.process.analysis.poistat.AreaCountTrackMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by gaojian on 2016/3/25.
 */
public class TrackStatMR {
    public static final Logger logger = Logger.getLogger(TrackStatMR.class);

    public static class OutputMapper extends Mapper<NullWritable, Text, NullWritable, Text> {
        @Override
        protected void map(NullWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            AreaCountResult result = AreaCountResult.parse(value.toString());

            StringBuilder sb = new StringBuilder();
            sb.append("\"" + result.getCity() + "\",");
            sb.append("\"" + result.getTown() + "\",");
            sb.append("\"" + result.getAreaId() + "\",");
            sb.append(result.getCount());

            context.write(NullWritable.get(), new Text(sb.toString()));
        }
    }

    public static class TrackStatDriver extends Configured implements Tool {
        @Override
        public int run(String[] args) throws Exception{
            String table = args[0];
            String family = args[1];
            String qualifier = args[2];
            String outputPath = args[3];
            String blockfile = args[4];
            String meshlist  = args[5];
            String zookeeperHost = args[6];

            logger.info("zookeeperHost : " + zookeeperHost + " table : " + table + " family : " + family + " qualifier : " + qualifier);
            logger.info("outputPath : " + outputPath + " blockfile : " + blockfile + " meshlist" + meshlist);

            Configuration conf = new Configuration();
            conf.set("hbase.zookeeper.quorum" , zookeeperHost);
            conf.set("family", family);
            conf.set("qualifier", qualifier);
            conf.set("blockfile" , blockfile);
            conf.set("meshlist" , meshlist);

            // 2015-10-16新增
            conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
            conf.setBoolean("mapreduce.map.speculative", false);

            Job job = Job.getInstance(conf, "TrackStatMR");

            job.setJarByClass(TrackStatMR.class);

            Scan scan = new Scan();
            // 2015-10-16新增
            scan.setCacheBlocks(false);
            scan.setCaching(10);
            scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));

            TableMapReduceUtil.initTableMapperJob(table, scan,
                    AreaCountTrackMapper.class,
                    Text.class, IntWritable.class, job);

            Configuration reducerConf = new Configuration();
            ChainReducer.setReducer(job, AreaCountReducer.class, Text.class, IntWritable.class, NullWritable.class, Text.class, reducerConf);

            //Configuration outputMapperConf = new Configuration();
            //ChainReducer.addMapper(job, OutputMapper.class, NullWritable.class, Text.class, NullWritable.class, Text.class, outputMapperConf);

            job.setNumReduceTasks(1);

            if (Hdfs.deleteIfExists(conf, outputPath)) {
                System.out.println("存在此输出路径，已删除！！！");
            }
            FileOutputFormat.setOutputPath(job, new Path(outputPath));

            return job.waitForCompletion(true) ? 0 : 1;
        }
    }
    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new TrackStatDriver() , args));
    }

    /**
     * azkaban 调度接口
     * @param table        HBase 轨迹数据表名
     * @param family       轨迹数据列族名
     * @param qualifier    轨迹数据列
     * @param outputPath   输出数据目录
     * @param blockfile    block数据
     * @param meshlist     分省图幅列表
     * @throws Exception
     */
    public void azkabanRun(String table , String family , String qualifier , String outputPath , String blockfile , String meshlist , String zookeeperHost) throws Exception {
        ToolRunner.run(new TrackStatDriver() , new String[]{table , family , qualifier , outputPath , blockfile , meshlist , zookeeperHost});
    }
}
