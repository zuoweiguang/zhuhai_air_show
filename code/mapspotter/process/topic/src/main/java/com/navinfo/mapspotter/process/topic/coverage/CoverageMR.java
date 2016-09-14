package com.navinfo.mapspotter.process.topic.coverage;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huanghai on 2016/3/2.
 */
public class CoverageMR extends Configured implements Tool {
    private static final Logger logger = Logger.getLogger(CoverageMR.class);

    /**
     * azkaban调度入口方法
     *
     * @param tableName          hbase表名
     * @param roadFamily         底图列族名
     * @param roadQualifie       底图列名
     * @param trajectoryFamily   轨迹列族名
     * @param trajectoryQualifie 轨迹列名
     * @param gpsThreholdNum     gps点门限值
     * @param ratioThrehold      link被覆盖百分比
     * @param outputPath         被覆盖link输出hdfs路径
     * @param zookeeperHost      zookeeper
     * @throws Exception
     */
    public void azkabanRun(String tableName, String roadFamily, String roadQualifie, String trajectoryFamily, String trajectoryQualifie, String gpsThreholdNum, String ratioThrehold, String outputPath, String zookeeperHost) throws Exception {
        int res = ToolRunner.run(new Configuration(), new CoverageMR(), new String[]{tableName, roadFamily, roadQualifie, trajectoryFamily, trajectoryQualifie, gpsThreholdNum, ratioThrehold, outputPath, zookeeperHost});
        System.exit(res);
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new CoverageMR(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String tableName = args[0];
        String roadFamily = args[1];
        String roadQualifie = args[2];
        String trajectoryFamily = args[3];
        String trajectoryQualifie = args[4];

        int gpsThreholdNum = Integer.parseInt(args[5]);
        double ratioThrehold = Double.parseDouble(args[6]);

        String outputPath = args[7];

        String zookeeperHost = args[8];


        logger.info("tableName : " + tableName + " roadFamily : " + roadFamily + " roadQualifie : " + roadQualifie + " trajectoryFamily : " + trajectoryFamily + " trajectoryQualifie : " + trajectoryQualifie);
        logger.info("gpsThreholdNum : " + gpsThreholdNum + " ratioThrehold : " + ratioThrehold + " zookeeperHost : " + zookeeperHost);

        Configuration conf = getConf();
        conf.set("roadFamily", roadFamily);
        conf.set("roadQualifie", roadQualifie);
        conf.set("trajectoryFamily", trajectoryFamily);
        conf.set("trajectoryQualifie", trajectoryQualifie);
        conf.set("gpsThreholdNum", String.valueOf(gpsThreholdNum));
        conf.set("ratioThrehold", String.valueOf(ratioThrehold));
        conf.set("hbase.zookeeper.quorum", zookeeperHost);

        Job job = Job.getInstance(conf, "CoverageMR");
        job.setJarByClass(CoverageMR.class);

        Scan scan = new Scan();
        scan.setCacheBlocks(false);


        scan.addColumn(roadFamily.getBytes(), roadQualifie.getBytes());
        scan.addColumn(trajectoryFamily.getBytes(), trajectoryQualifie.getBytes());

        TableMapReduceUtil.initTableMapperJob(tableName, scan,
                CoverageMap.class, IntWritable.class, NullWritable.class, job);
        job.setReducerClass(CoverageReduce.class);

        job.setOutputFormatClass(TextOutputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setNumReduceTasks(1);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class CoverageReduce extends Reducer<IntWritable, NullWritable, IntWritable, NullWritable> {

        @Override
        protected void reduce(IntWritable key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }
    }


    public static class CoverageMap extends TableMapper<IntWritable, NullWritable> {
        // 列族列
        String roadFamily;
        String roadQualifie;
        String trajectoryFamily;
        String trajectoryQualifie;

        // 过滤阀值
        int gpsThreholdNum;
        double ratioThrehold;

        IntWritable pidKey = new IntWritable();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration config = context.getConfiguration();
            roadFamily = config.get("roadFamily");
            roadQualifie = config.get("roadQualifie");
            trajectoryFamily = config.get("trajectoryFamily");
            trajectoryQualifie = config.get("trajectoryQualifie");
            gpsThreholdNum = Integer.parseInt(config.get("gpsThreholdNum"));
            ratioThrehold = Double.parseDouble(config.get("ratioThrehold"));
        }

        @Override
        protected void map(ImmutableBytesWritable key, Result result, Context context) throws IOException, InterruptedException {
            byte[] roadByte = null;
            byte[] trajectoryByte = null;
            // 获取轨迹和路网的矩阵数组
            for (Cell cell : result.rawCells()) {
                if (Bytes.toString(cell.getFamilyArray(),
                        cell.getFamilyOffset(), cell.getFamilyLength()).equals(
                        trajectoryFamily)
                        && Bytes.toString(cell.getQualifierArray(),
                        cell.getQualifierOffset(),
                        cell.getQualifierLength()).equals(
                        trajectoryQualifie)) {
                    trajectoryByte = Bytes.copy(cell.getValueArray(),
                            cell.getValueOffset(), cell.getValueLength());
                } else if (Bytes.toString(cell.getFamilyArray(),
                        cell.getFamilyOffset(), cell.getFamilyLength()).equals(
                        roadFamily)
                        && Bytes.toString(cell.getQualifierArray(),
                        cell.getQualifierOffset(),
                        cell.getQualifierLength()).equals(
                        roadQualifie)) {
                    roadByte = Bytes.copy(cell.getValueArray(),
                            cell.getValueOffset(), cell.getValueLength());
                }
            }
            // 对轨迹和路网的矩阵进行对比分析
            if (roadByte != null && trajectoryByte != null) {
                // 对稀疏矩阵进行转化
                // 路网矩阵
                SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
                int[][] roadPparse = serializeUtil.deserialize(roadByte);
                DoubleMatrix roadMatrix = new DoubleMatrix(roadPparse[0], roadPparse[1], roadPparse[2]);
                int[][] roadArray = roadMatrix.toIntArray2();
                // 轨迹矩阵
                int[][] trajectorySparse = serializeUtil.deserialize(trajectoryByte);
                DoubleMatrix trajectoryMatrix = new DoubleMatrix(trajectorySparse[0], trajectorySparse[1], trajectorySparse[2]);
                int[][] trajectoryArray = trajectoryMatrix.toIntArray2();
                Map<String, Integer> linkMap = new HashMap<String, Integer>();
                Map<String, Integer> tmpLinkMap = new HashMap<String, Integer>();
                for (int i = 0; i < roadArray.length; i++) {
                    for (int j = 0; j < roadArray[i].length; j++) {
                        // 阀值过滤
                        int gpsNum = trajectoryArray[i][j];
                        int pid = roadArray[i][j];
                        if (pid != 0) {
                            Integer roadLinkSum = linkMap.get(String.valueOf(pid));
                            if (roadLinkSum != null) {
                                linkMap.put(String.valueOf(pid), roadLinkSum + 1);
                            } else {
                                linkMap.put(String.valueOf(pid), 1);
                            }
                        }
                        if (gpsNum >= gpsThreholdNum && pid != 0) {
                            Integer tmpLinkSum = tmpLinkMap.get(String.valueOf(pid));
                            if (tmpLinkSum != null) {
                                tmpLinkMap.put(String.valueOf(pid), tmpLinkSum + 1);
                            } else {
                                tmpLinkMap.put(String.valueOf(pid), 1);
                            }
                        }
                    }
                }
                // 计算link中占据超过阀值个数的格子的link
                for (Map.Entry<String, Integer> entry : tmpLinkMap.entrySet()) {
                    String pid = entry.getKey();
                    Integer entryValue = entry.getValue();
                    if (entryValue / (double) linkMap.get(pid) >= ratioThrehold) {
                        pidKey.set(Integer.parseInt(pid));
                        context.write(pidKey, NullWritable.get());
                    }
                }
            }
        }
    }
}
