package com.navinfo.mapspotter.process.convert.road.transfer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
//import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
//import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.process.convert.road.PropertiesUtil;

/**
 * Created by huanghai on 2016/1/19.
 */
public class BasicRoadTransferHFileMR extends Configured implements Tool {
    private static final Logger logger = Logger.getLogger(BasicRoadTransferMR.class);

    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(new Configuration(), new BasicRoadTransferHFileMR(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        String inputPath = args[0];

        String matrix = PropertiesUtil.getValue("matrix");
        String zoom = PropertiesUtil.getValue("zoom");
        //String tableName = PropertiesUtil.getValue("table_name");
        String thresholdOne = PropertiesUtil.getValue("threshold_one");
        String thresholdTwo = PropertiesUtil.getValue("threshold_two");
        String zookeeperHost = PropertiesUtil.getValue("zookeeper_host");
        logger.info("thresholdOne : " + thresholdOne + " thresholdTwo : " + thresholdTwo + " zookeeperHost : " + zookeeperHost);
        //logger.info("inputPath : " + inputPath + " matrix : " + matrix + " zoom : " + zoom + " tableName : " + tableName);

        Configuration conf = getConf();
        conf.set("matrixInt", matrix);
        conf.set("tableFamily", args[1]);
        conf.set("tableQualifier", args[2]);
        conf.set("zoom", zoom);
        conf.set("thresholdOne", thresholdOne);
        conf.set("thresholdTwo", thresholdTwo);
        //conf.set("hbase.zookeeper.quorum", zookeeperHost);

        Job job = Job.getInstance(conf, "BasicRoadTransferHFileMR");
        job.setJarByClass(BasicRoadTransferHFileMR.class);

        job.setMapperClass(BasicRoadHFileMapper.class);

        job.setReducerClass(BasicRoadTransferHFileReducer.class);
        job.setOutputKeyClass(ImmutableBytesWritable.class);
        job.setOutputValueClass(KeyValue.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setNumReduceTasks(Integer.parseInt(args[3]));
        job.setOutputFormatClass(HFileOutputFormat2.class);


        FileInputFormat.addInputPath(job, new Path(inputPath));

        // Auto configure partitioner and reducer
        HFileOutputFormat2.setOutputPath(job, new Path(args[4]));

        return job.waitForCompletion(true) ? 0 : 1;
    }


    public static class BasicRoadTransferHFileReducer extends Reducer<Text, Text, ImmutableBytesWritable, KeyValue> {
        private int matrixInt;
        public static byte[] HBASE_FAMILY_DATA_BYTE;
        public static byte[] HBASE_QUALIFIE_ROAD_BYTE;
        private Map<Integer, String> map = new HashMap<Integer, String>();
        private Map<String, Integer> map2 = new HashMap<String, Integer>();


        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            matrixInt = Integer.parseInt(conf.get("matrixInt"));
            String tableFamily = conf.get("tableFamily");
            String tableQualifier = conf.get("tableQualifier");
            HBASE_FAMILY_DATA_BYTE = tableFamily.getBytes();
            HBASE_QUALIFIE_ROAD_BYTE = tableQualifier.getBytes();
            logger.info(" matrixInt : " + matrixInt + " tableFamily : " + tableFamily + " tableQualifier : " + tableQualifier);

            for (int i = 0; i <= 1024; i++) {
                BigInteger bi = new BigInteger(String.valueOf(i));
                String str = bi.toString(36);
                map.put(i, str);

            }
            Iterator<Entry<Integer, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, String> en = it.next();
                map2.put(en.getValue(), en.getKey());
            }
        }


        @Override
        protected void reduce(Text key, Iterable<Text> vals, Context context)
                throws IOException, InterruptedException {
            // TODO Auto-generated method stub
            String[][] arrString = new String[matrixInt][matrixInt];
            for (Text val : vals) {
                String valStr = val.toString();
                String splits[] = valStr.split("\\+");
                String pid = splits[0];
                String[] strs = splits[1].split("\\|"); // x,y,pid_0
                for (String str : strs) {
                    if (StringUtils.isEmpty(str)) {
                        continue;
                    }
                    String[] strings = str.split(",");

                    int x = new BigInteger(strings[1], 36).intValue();// 纬度
                    int y = new BigInteger(strings[0], 36).intValue();// 经度

                    // 矩阵中x存纬度，y存经度
                    String pidDistance = strings[2];
                    String strVal = arrString[x][y];
                    int distanceNew = Integer.parseInt(pidDistance);
                    if (strVal != null) {
                        int distance = Integer.parseInt(strVal.split("_")[1]);

                        if (distanceNew < distance) {
                            arrString[x][y] = pid + "_" + distanceNew;
                        }
                    } else {
                        arrString[x][y] = pid + "_" + distanceNew;
                    }
                }
            }
            int[][] pidArr = new int[matrixInt][matrixInt];
            for (int i = 0; i < arrString.length; i++) {
                for (int j = 0; j < arrString[i].length; j++) {
                    String pidDistance = arrString[i][j];
                    if (pidDistance != null) {
//                       BigInteger bInt = new BigInteger(pidDistance.split("_")[0], 36);
                        pidArr[i][j] = Integer.parseInt(pidDistance.split("_")[0]);
                    }
                }
            }
            // 存入hbase
            SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
            DoubleMatrix dMatrix = new DoubleMatrix(pidArr);
            DoubleMatrix.SparseMatrix sparseMatrix = dMatrix.toSparse();
            int[][] intMx = new int[][]{sparseMatrix.data, sparseMatrix.indices, sparseMatrix.indptr};


            KeyValue kv = new KeyValue(key.getBytes(), HBASE_FAMILY_DATA_BYTE, HBASE_QUALIFIE_ROAD_BYTE, serializeUtil.serialize(intMx));

            context.write(new ImmutableBytesWritable(key.getBytes()), kv);
        }
    }

}