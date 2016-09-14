package com.navinfo.mapspotter.process.topic.traffic;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.PostGISDatabase;
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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 将cntf转换为link
 * Created by SongHuiXing on 7/26 0026.
 */
public class CNTFConvert {
    enum InsertStatus{
        Success,
        Fail,
    }

    private static final Log s_logger = LogFactory.getLog(CNTFConvert.class);

    public static class ConvertCntfMapper
            extends Mapper<LongWritable, Text, NullWritable, IntWritable> {

        private PostGISDatabase pgDb = null;

        private PreparedStatement insertStmt = null;
        private static final String insert_traffic =
                "insert into traffic_link(pid, functionclass, direct, status, traveltime, timestamp, geom) " +
                "select pid, functionclass, ?, ?, ?, ?, geom from road where pid=?";

        @Override
        protected void setup(Context context){
            pgDb = (PostGISDatabase) DataSource.getDataSource(
                            IOUtil.makePostGISParam("192.168.4.104",
                                                    5440,
                                                    "navinfo",
                                                    "postgres",
                                                    "navinfo1!pg"));

            if(null != pgDb){
                try {
                    insertStmt = pgDb.prepare(insert_traffic);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(null == pgDb || insertStmt == null){
                s_logger.error("Can not connect to PostGIS or create prepared statment.");
            }
        }

        @Override
        protected void cleanup(Context context){
            if(null != pgDb){
                pgDb.close();
                pgDb = null;
            }
        }

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if(null == pgDb || insertStmt == null)
                return;

            String[] cnFields = value.toString().split(",");

            if(cnFields.length != 29)
                return;

            int pid = Integer.parseInt(cnFields[6]);

            int dirFlag = Integer.parseInt(cnFields[2]);

            long timestamp = Long.parseLong(cnFields[10]);

            //int isPrecident = Integer.parseInt(cnFields[20]);
            int status = Integer.parseInt(cnFields[24]);
            int traveltime = Integer.parseInt(cnFields[25]);

            try {
                if(0 == pgDb.excute(insertStmt, dirFlag, status, traveltime, timestamp, pid)){
                    context.getCounter(InsertStatus.Success).increment(1);
                }
            } catch (SQLException e) {
                s_logger.error(e.getMessage());
                context.getCounter(InsertStatus.Fail).increment(1);
            }
        }
    }

    public static class ConvertCntDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(args.length != 1){
                System.err.printf("Usage: %s [generic option] <input path>\n",
                        getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            Job job = getJob(getConf(), args[0]);

            boolean sucess = job.waitForCompletion(true);

            if(sucess){
                createIndex();
            }

            return sucess ? 0 : 1;
        }

        private boolean createIndex(){
            PostGISDatabase pgDb = (PostGISDatabase) DataSource.getDataSource(
                                        IOUtil.makePostGISParam("192.168.4.104",
                                                                5440,
                                                                "navinfo",
                                                                "postgres",
                                                                "navinfo1!pg"));

            if(null == pgDb)
                return false;

            pgDb.execute("create index IDX_TRAFFICLINK_GEOM ON traffic_link USING gist(geom)");
            pgDb.execute("create index IDX_TRAFFICLINK_FC on traffic_link (functionclass)");
            pgDb.execute("create index IDX_TRAFFICLINK_DIR on traffic_link (direct)");
            pgDb.execute("create index IDX_TRAFFICLINK_TIME on traffic_link (timestamp)");
            pgDb.execute("create index IDX_TRAFFICLINK_PID on traffic_link (pid)");

            pgDb.close();

            return true;
        }

        public static Job getJob(Configuration cfg, String inputPath) throws IOException {

            cfg.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{inputPath},
                            FileSystem.get(cfg), "1", 10)));

            Job job = Job.getInstance(cfg, "Insert traffic data to PG");

            job.setJarByClass(ConvertCntDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));

            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(IntWritable.class);
            job.setMapperClass(ConvertCntfMapper.class);

            job.setOutputFormatClass(NullOutputFormat.class);

            job.setNumReduceTasks(0);

            return job;
        }

    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new ConvertCntDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
