package com.navinfo.mapspotter.process.storage.vectortile;

import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;
import com.navinfo.mapspotter.process.convert.vectortile.MgConverter;
import com.navinfo.mapspotter.process.convert.vectortile.PgConverter;
import com.vividsolutions.jts.geom.Envelope;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Created by SongHuiXing on 6/6 0006.
 */
public class BuildTrafficPbfMR {
    protected final static String PBFTableName = "Pbf_Table";
    protected final static String PBFColfamily = "Pbf_Colfamily";
    protected final static String PBFColumn = "Pbf_Column";
    protected final static String PBFMinLevel = "Mini_Level";
    protected final static String PBFMaxLevel = "Max_Level";

    public static class BuildPbfMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put>{

        private ImmutableBytesWritable pbfTable = null;
        private String pbfTableName = null;
        private String pbfColfamilyName = null;
        private String pbfColumnName = null;
        private int minlevel = 3;
        private int maxlevel = 16;

        private PostGISDatabase pgDb;
        private MongoDB mgDb;

        private PgConverter pgConvertor;
        private MgConverter mgConverter;

        private ProtoBufStorage storage = null;

        private WarehouseDataType.SourceType targetType = null;

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            pbfTableName = cfg.get(PBFTableName, "Traffic_Vectortile");
            pbfTable = new ImmutableBytesWritable(Bytes.toBytes(pbfTableName));

            pbfColfamilyName = cfg.get(PBFColfamily, "Vectortile");
            pbfColumnName = cfg.get(PBFColumn, "traffic");

            minlevel = cfg.getInt(PBFMinLevel, 3);
            maxlevel = cfg.getInt(PBFMaxLevel, 16);

            storage = new ProtoBufStorage("Master.Hadoop:2181",
                                        pbfTableName,
                                        pbfColfamilyName);

            targetType = initTargetTypes(pbfColumnName);

            pgDb = (PostGISDatabase) DataSource.getDataSource(
                            IOUtil.makePostGISParam("192.168.4.104",
                                    5440,
                                    "baotou_demo",
                                    "postgres",
                                    "navinfo1!pg"));

            pgConvertor = new PgConverter(pgDb);

            mgDb = (MongoDB) DataSource.getDataSource(
                            IOUtil.makeMongoDBParams("192.168.4.128",
                                                        27017,
                                                        "warehouse"));

            mgConverter = new MgConverter(mgDb);
        }

        private WarehouseDataType.SourceType initTargetTypes(String target){
            if(target.equals("road")){
                return WarehouseDataType.SourceType.Road;
            } else if(target.equals("admin")){
                return WarehouseDataType.SourceType.Admin;
            } else if(target.equals("background")){
                return WarehouseDataType.SourceType.Background;
            } else if(target.equals("dig")){
                return WarehouseDataType.SourceType.Dig;
            } else if(target.equals("block")){
                return WarehouseDataType.SourceType.Block;
            } else if (target.equals("traffic")){
                return WarehouseDataType.SourceType.Traffic;
            }

            return WarehouseDataType.SourceType.Road;
        }

        @Override
        protected void cleanup(Context context){
            try {
                pgDb.close();
                mgDb.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String tileCode = value.toString();

            String[] params = tileCode.split("_");

            int z = Integer.parseInt(params[0]);
            if(z > maxlevel || z < minlevel)
                return;

            int x = Integer.parseInt(params[1]);
            int y = Integer.parseInt(params[2]);

            Envelope bound = MercatorUtil.mercatorBound(z, x, y);
            if(bound.getMaxX() < 60 || bound.getMinX() > 145 || bound.getMinY() > 56 )
                return;

            byte[] protobuf = null;
            if(targetType != WarehouseDataType.SourceType.Dig){
                protobuf = pgConvertor.getProtobuf(z, x, y, targetType);
            } else {
                protobuf = mgConverter.getProtobuf(z, x, y, targetType);
            }

            if(null == protobuf || protobuf.length <=2)
                return;

            Put pt = storage.writeProtobuf2Put(z, x, y, protobuf, pbfColumnName);

            context.write(pbfTable, pt);
        }
    }

    public static class BuildPbfDriver extends Configured implements Tool{

        @Override
        public int run(String[] args) throws Exception {
            if(args.length < 1){
                System.err.printf("Usage: %s <tilelistfile> <target> [minlevel] [maxlevel] [mapcount]" +
                                "[tablename] [colfamily] \n",
                                getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            String target = args[1];

            int minlevel = 3;
            if(args.length > 2){
                minlevel = Integer.parseInt(args[2]);
            }

            int maxlevel = 16;
            if(args.length > 3){
                maxlevel = Integer.parseInt(args[3]);
            }

            int mapcount = 200;
            if(args.length > 4){
                mapcount = Integer.parseInt(args[4]);
            }

            String tablename = "Traffic_Vectortile";
            if(args.length > 5){
                tablename = args[5];
            }

            String colfa = "Vectortile";
            if(args.length > 6){
                colfa = args[6];
            }

            Job job = getJob(getConf(), args[0],
                            minlevel, maxlevel, mapcount,
                            tablename, colfa, target);

            return job.waitForCompletion(true) ? 0 : 1;
        }

        public static Job getJob(Configuration cfg, String tilelistfile,
                                 int min, int max, int mapcount,
                                 String tablename, String colFamily, String column) throws IOException {

            cfg.set(PBFTableName, tablename);
            cfg.set(TableOutputFormat.OUTPUT_TABLE, tablename);

            cfg.set(PBFColfamily, colFamily);
            cfg.set(PBFColumn, column);

            cfg.setInt(PBFMinLevel, min);
            cfg.setInt(PBFMaxLevel, max);

            //hbase
            cfg.set("hbase.zookeeper.quorum", "Master.Hadoop:2181,Slave3.Hadoop");
            cfg.set("hbase.master", "Slave3.Hadoop");

            cfg.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{tilelistfile},
                            FileSystem.get(cfg),
                            "tile",
                            mapcount)));

            Job job = Job.getInstance(cfg, "Build Taffic ProtoBuf to HBase");

            job.setJarByClass(BuildPbfDriver.class);

            FileInputFormat.addInputPath(job, new Path(tilelistfile));

            job.setMapOutputKeyClass(ImmutableBytesWritable.class);
            job.setMapOutputValueClass(Put.class);
            job.setMapperClass(BuildPbfMapper.class);

            job.setOutputFormatClass(TableOutputFormat.class);

            job.setNumReduceTasks(0);

            return job;
        }
    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new BuildPbfDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
