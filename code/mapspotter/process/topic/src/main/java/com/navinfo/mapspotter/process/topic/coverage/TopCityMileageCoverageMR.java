package com.navinfo.mapspotter.process.topic.coverage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

import javax.management.StringValueExp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huanghai on 2016/3/3.
 */
public class TopCityMileageCoverageMR extends Configured implements Tool {
    private static final Logger logger = Logger.getLogger(TopCityMileageCoverageMR.class);

    /**
     * azkaban调度入口方法
     *
     * @param coverageLinkPath 被覆盖linkpid的hdfs路径
     * @param inputPath        母库JSON数据hdfs路径
     * @param outputPath       输出覆盖率的hdfs路径
     * @param mysqlHost        mysql地址
     * @param mysqlPort        mysql端口
     * @param mysqlDatabase    mysql数据库名称
     * @param mysqlUsername    mysql用户名
     * @param mysqlPWD         mysql密码
     * @throws Exception
     */
    public void azkabanRun(String coverageLinkPath, String inputPath, String outputPath, String mysqlHost, String mysqlPort, String mysqlDatabase, String mysqlUsername, String mysqlPWD) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TopCityMileageCoverageMR(), new String[]{coverageLinkPath, inputPath, outputPath, mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPWD});
        System.exit(res);
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TopCityMileageCoverageMR(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String coverageLinkPath = args[0];
        String inputPath = args[1];
        String outputPath = args[2];
        logger.info("inputPath : " + inputPath + " outputPath : " + outputPath + " coverageLinkPath : " + coverageLinkPath);

        String host = args[3];
        int port = Integer.parseInt(args[4]);
        String database = args[5];
        String username = args[6];
        String passWord = args[7];

        Configuration conf = getConf();
        conf.set("coverageLinkPath", coverageLinkPath);
        conf.set("host", host);
        conf.set("port", String.valueOf(port));
        conf.set("database", database);
        conf.set("username", username);
        conf.set("passWord", passWord);

        Job job = Job.getInstance(conf, "MileageCoverageMR");
        job.setJarByClass(TopCityMileageCoverageMR.class);

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
        Map<String, String> adadminName = null;


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
            // 查询admin城市信息
            QueryAdAdminName adminName = new QueryAdAdminName();
            try {
                String host = conf.get("host");
                int port = Integer.parseInt(conf.get("port"));
                String database = conf.get("database");
                String username = conf.get("username");
                String passWord = conf.get("passWord");
                adadminName = adminName.queryAdadminName(host, port, database, username, passWord);
            } catch (SQLException e) {
                logger.error("queryAdadminName() SQLException : " + e);
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            JSONObject jsonObject = JSON.parseObject(value.toString());
            JSONObject properties = jsonObject.getJSONObject("properties");
            int pid = properties.getIntValue("link_pid");
            int functionclass = properties.getIntValue("functionclass");
            String regionId = properties.getString("r_region");

            double length = properties.getDouble("length");
            Boolean aBoolean = pidMap.get(pid);
            String proCity = adadminName.get(regionId);

            if (proCity != null) {
                // 被覆盖link
                if (aBoolean != null && aBoolean) {
                    // 按区域
                    keyText.set("COV|" + proCity);
                    outVal.set(length);
                    context.write(keyText, outVal);
                    // 道路等级
                    keyText.set("COV|" + proCity + "|" + functionclass);
                    outVal.set(length);
                    context.write(keyText, outVal);

                    // link数目
                    keyText.set("COVN|" + proCity);
                    outVal.set(1);
                    context.write(keyText, outVal);

                    keyText.set("COVFCN|" + proCity + "|" + functionclass);
                    outVal.set(1);
                    context.write(keyText, outVal);
                }
                // 所有link
                keyText.set("ALL|" + proCity);
                outVal.set(length);
                context.write(keyText, outVal);
                // 道路等级
                keyText.set("ALL|" + proCity + "|" + functionclass);
                outVal.set(length);
                context.write(keyText, outVal);
                // link数目
                keyText.set("ALLN|" + proCity);
                outVal.set(1);
                context.write(keyText, outVal);

                keyText.set("ALLFCN|" + proCity + "|" + functionclass);
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
