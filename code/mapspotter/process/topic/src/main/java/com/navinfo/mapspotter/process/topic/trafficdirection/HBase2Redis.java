package com.navinfo.mapspotter.process.topic.trafficdirection;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.Redis;
import com.navinfo.mapspotter.foundation.util.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gaojian on 2016/4/9.
 */
public class HBase2Redis {
    private static final Logger logger = Logger.getLogger(HBase2Redis.class);

    public static class HBase2RedisMapper extends TableMapper<Text, Text> {
        private MercatorUtil mercator = null;
        private byte[] family;
        private byte[] qualifier;
        private Set<String> tiles = null;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            ProvinceUtil provinceUtil = new ProvinceUtil();
            provinceUtil.initProvinceMeshes();
            tiles = new HashSet<>();
            tiles.addAll(provinceUtil.provinceTiles("北京市", 14));

            mercator = new MercatorUtil(1024, 14);

            family = context.getConfiguration().get("family").getBytes();

            qualifier = context.getConfiguration().get("qualifier").getBytes();
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
        }

        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
            String tile = StringUtil.reverse(Bytes.toString(key.get()));

            if (!tiles.contains(tile)) return;

            IntCoordinate origin = mercator.inTile2Pixels(0, 0, tile);

            Integer[][] matrix = MatrixUtil.deserializeMatrix(value.getValue(family, qualifier), true);

            for (int y = 0; y < 1024; ++y) {
                for (int x = 0; x < 1024; ++x) {
                    if (matrix[y][x] != null && matrix[y][x] > 0) {
                        String coord = "";//Base64Util.encode(x + origin.x) + "_" + Base64Util.encode(y + origin.y);
                        String pid = "";//Base64Util.encode(matrix[y][x]);
                        context.write(new Text(coord), new Text(pid));
                    }
                }
            }
        }
    }

    public static class HBase2RedisReducer extends Reducer<Text, Text, Text, Text> {
        private Redis redis = null;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            redis = (Redis) DataSource.getDataSource(IOUtil.makeRedisParam(
                    "192.168.4.128", 6379
            ));
            redis.transaction();
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            redis.commit();
            redis.close();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value : values) {
                redis.updateString(key.toString(), value.toString());
                //context.write(key, value);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String table = args[0];
        String family = args[1];
        String qualifier = args[2];
        String outPath = args[3];

        Configuration conf = new Configuration();
        conf.set("family", family);
        conf.set("qualifier", qualifier);

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapreduce.map.speculative", false);

        Job job = Job.getInstance(conf, "HBase2Redis");

        job.setJarByClass(HBase2Redis.class);

        Scan scan = new Scan();
        scan.setCacheBlocks(false);
        scan.setCaching(10);
        scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));

        TableMapReduceUtil.initTableMapperJob(table, scan,
                HBase2RedisMapper.class,
                Text.class, Text.class, job);

        job.setReducerClass(HBase2RedisReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        if (Hdfs.deleteIfExists(conf, outPath)) {
            System.out.println("存在此输出路径，已删除！！！");
        }
        FileOutputFormat.setOutputPath(job, new Path(outPath));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
