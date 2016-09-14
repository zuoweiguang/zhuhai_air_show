package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.topic.restriction.*;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.geojson.Feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 根据从母库转出的路口基本信息文档及道路14级地图，构建路口栅格，并将路口信息及索引存储到HBase
 * Created by SongHuiXing on 2016/1/27.
 */
public class InsertCross2HBase {
    private static final Log s_logger = LogFactory.getLog(InsertCross2HBase.class);

    enum ConvertCounter{
        TotalCount,
        InvalidCount,
    }

    public enum InsertTarget{
        RASTER,
        ALL,
    }

    public static class InsertCrossMapper
                extends Mapper<LongWritable, Text, Text, Text> {

        private CrossRasterFactory factory = new CrossRasterFactory(14, 1024, null);

        private JsonUtil m_jsonUtil = JsonUtil.getInstance();

        private Text outKey = new Text();

        private int crosstime = 3;
        private int pagetime = 9;

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            crosstime = cfg.getInt(RestrictionConfig.CROSS_EXPANDTIME, 3);
            pagetime = cfg.getInt(RestrictionConfig.PAGE_EXPANDTIME, 9);
        }

        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            BaseCrossJsonModel crossJson = BaseCrossJsonModel.readCrossJson(value.toString());

            List<Node> nodes = Node.convert(m_jsonUtil.readCollection(crossJson.getNodes(), Feature.class));
            List<Link> links = Link.convert(m_jsonUtil.readCollection(crossJson.getLinks(), Feature.class));

            try {
                List<double[]> crossAreas = CrossRasterFactory.getCrossAndPageArea(nodes, crosstime, pagetime);

                double[] pageenv = crossAreas.get(2);

                Coordinate lb = new Coordinate(pageenv[0], pageenv[1]);
                Coordinate lt = new Coordinate(pageenv[0], pageenv[3]);
                Coordinate rt = new Coordinate(pageenv[2], pageenv[3]);
                Coordinate rb = new Coordinate(pageenv[2], pageenv[1]);

                ArrayList<Map.Entry<String, IntCoordinate>> tileInfo = factory.getTileInfo(lb, lt, rt, rb);

                outKey.set(tileInfo.get(0).getKey());
                context.write(outKey, value);

            } catch (ArrayIndexOutOfBoundsException e){
                s_logger.error(String.format("Invalid Cross: %d", crossJson.getPID()));
                throw e;
            }
        }
    }

    public static class InsertCrossReducer
            extends Reducer<Text, Text, ImmutableBytesWritable, Put>{

        private RoadRasterSupplier roadRasterSupplier;
        private CrossRasterFactory factory = null;

        private JsonUtil m_jsonUtil = JsonUtil.getInstance();

        private ImmutableBytesWritable infoTable = null;

        private ImmutableBytesWritable indexTable = null;

        private int crosstime = 3;
        private int pagetime = 9;
        private InsertTarget target = InsertTarget.ALL;

        private CrossInformationVistor crossInfoVistor;

        @Override
        protected void setup(Context context){
            Configuration cfg = context.getConfiguration();

            crosstime = cfg.getInt(RestrictionConfig.CROSS_EXPANDTIME, 3);
            pagetime = cfg.getInt(RestrictionConfig.PAGE_EXPANDTIME, 9);
            target = cfg.getEnum(RestrictionConfig.INSERTHBASE_DATATYPE, InsertTarget.ALL);

            crossInfoVistor = new CrossInformationVistor(cfg.get(RestrictionConfig.BASEINFO_COLFAMILY),
                                                        cfg.get(RestrictionConfig.RASTER_COLFAMILY),
                                                        cfg.get(RestrictionConfig.INDEX_COLFAMILY));

            infoTable = new ImmutableBytesWritable(Bytes.toBytes(CrossInformationVistor.s_InfoTableName));
            indexTable = new ImmutableBytesWritable(Bytes.toBytes(CrossInformationVistor.s_IndexTableName));

            String tablename = cfg.get(RestrictionConfig.ROAD_RASTER_TABLE, "road_raster");
            String colFamily = cfg.get(RestrictionConfig.ROAD_RASTER_COLFAMILY, "1607");
            roadRasterSupplier = new RoadRasterSupplier(tablename, colFamily);

            factory = new CrossRasterFactory(14, 1024, roadRasterSupplier, crosstime, pagetime);
            factory.prepare();
        }

        @Override
        protected void cleanup(Context context){
            factory.shutdown();
            factory = null;
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String tileCode = key.toString();
            roadRasterSupplier.setMainRoadRaster(tileCode);

            for (Text v : values){
                BaseCrossJsonModel crossJson = BaseCrossJsonModel.readCrossJson(v.toString());

                context.getCounter(ConvertCounter.TotalCount).increment(1);

                insertCross2Puts(context, crossJson);
            }
        }

        private void insertCross2Puts(Context context, BaseCrossJsonModel crossJsonModel) throws IOException, InterruptedException {
            List<Node> nodes = Node.convert(m_jsonUtil.readCollection(crossJsonModel.getNodes(), Feature.class));
            List<Link> links = Link.convert(m_jsonUtil.readCollection(crossJsonModel.getLinks(), Feature.class));

            try {
                CrossRaster raster = factory.buildRaster(crossJsonModel.getPID(), links, nodes);

                //插入信息表中的栅格信息
                Put rasterPut = crossInfoVistor.convertCrossRaster2Put(raster);
                context.write(infoTable, rasterPut);

                if(target.equals(InsertTarget.ALL)) {
                    //插入信息表中的基础信息
                    Put infoPut = crossInfoVistor.convertCrossJson2Put(crossJsonModel);
                    context.write(infoTable, infoPut);

                    //插入索引表
                    Put indexPut = crossInfoVistor.convertCrossIndexJson2Put(raster);
                    context.write(indexTable, indexPut);
                }

            } catch (Exception e){
                s_logger.error(String.format("Invalid Cross: %d", crossJsonModel.getPID()));
                context.getCounter(ConvertCounter.InvalidCount).increment(1);
            }
        }
    }

    public static class InsertCrossDriver extends Configured implements Tool {

        @Override
        public int run(String[] args) throws Exception {
            if(args.length < 1){
                System.err.printf("Usage: %s <inputpath> [infoColFamily] [rasterColfamily] " +
                                "[indexColFamily] [InsertType] [crosstime] " +
                                "[pagetime] [mapcount] [reducecount] [roadraster] [roadrastercolfa] \n",
                                getClass().getSimpleName());
                ToolRunner.printGenericCommandUsage(System.err);
            }

            String infoCol = "Information";
            if(args.length > 1){
                infoCol = args[1];
            }

            String rasterCol = "Analysis";
            if(args.length > 2){
                rasterCol = args[2];
            }

            String indexCol = "Page1";
            if(args.length > 3){
                indexCol = args[3];
            }

            InsertTarget type = InsertTarget.ALL;
            if(args.length > 4){
                type = InsertTarget.valueOf(args[4]);
            }

            int crosstime = 2;
            if(args.length > 5){
                crosstime = Integer.parseInt(args[5]);
            }

            int pagetime = 12;
            if(args.length > 6){
                pagetime = Integer.parseInt(args[6]);
            }

            int mapcount = 10;
            if(args.length > 7){
                mapcount = Integer.parseInt(args[7]);
            }

            int reducecount = 100;
            if(args.length > 8){
                reducecount = Integer.parseInt(args[8]);
            }

            String roadraster = "road_raster";
            if(args.length > 9){
                roadraster = args[9];
            }

            String roadrasterColFa = "1607";
            if(args.length > 10){
                roadrasterColFa = args[10];
            }

            Configuration cfg = getConf();
            cfg.set(RestrictionConfig.ROAD_RASTER_TABLE, roadraster);
            cfg.set(RestrictionConfig.ROAD_RASTER_COLFAMILY, roadrasterColFa);

            Job job = getJob(cfg, args[0],
                            infoCol, rasterCol, indexCol,
                            type, crosstime, pagetime,
                            mapcount, reducecount);

            return job.waitForCompletion(true) ? 0 : 1;
        }

        public static Job getJob(Configuration cfg, String inputPath,
                                 String infoCol, String rasterCol,
                                 String indexCol, InsertTarget type,
                                 int crosstime, int pagetime,
                                 int mapCount, int reducecount) throws IOException {
            Configuration hbaseCfg = CrossInformationVistor.getHBaseConfig();
            cfg.addResource(hbaseCfg);

            cfg.set("mapreduce.input.fileinputformat.split.maxsize",
                    Long.toString(Hdfs.CalMapReduceSplitSize(new String[]{inputPath},
                            FileSystem.get(cfg),
                            "part",
                            mapCount)));

            cfg.set(RestrictionConfig.BASEINFO_COLFAMILY, infoCol);
            cfg.set(RestrictionConfig.RASTER_COLFAMILY, rasterCol);
            cfg.set(RestrictionConfig.INDEX_COLFAMILY, indexCol);

            cfg.setInt(RestrictionConfig.CROSS_EXPANDTIME, crosstime);
            cfg.setInt(RestrictionConfig.PAGE_EXPANDTIME, pagetime);
            cfg.setEnum(RestrictionConfig.INSERTHBASE_DATATYPE, type);

            Job job = Job.getInstance(cfg, "Build Cross Raster to HBase");

            job.setJarByClass(InsertCrossDriver.class);

            FileInputFormat.addInputPath(job, new Path(inputPath));

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(Text.class);
            job.setMapperClass(InsertCrossMapper.class);

            job.setOutputKeyClass(ImmutableBytesWritable.class);
            job.setOutputValueClass(Put.class);
            job.setReducerClass(InsertCrossReducer.class);

            job.setOutputFormatClass(MultiTableOutputFormat.class);

            job.setNumReduceTasks(reducecount);

            return job;
        }
    }

    public void azkabanRun(Properties props) throws Exception {
        String[] args = new String[11];

        args[0] = props.getProperty(RestrictionConfig.CLEANCROSS_DATAPATH);

        args[1] = props.getProperty(RestrictionConfig.BASEINFO_COLFAMILY);
        args[2] = props.getProperty(RestrictionConfig.RASTER_COLFAMILY);
        args[3] = props.getProperty(RestrictionConfig.INDEX_COLFAMILY);

        args[4] = props.getProperty(RestrictionConfig.INSERTHBASE_DATATYPE);

        args[5] = props.getProperty(RestrictionConfig.CROSS_EXPANDTIME, "2");
        args[6] = props.getProperty(RestrictionConfig.PAGE_EXPANDTIME, "12");
        args[7] = props.getProperty(RestrictionConfig.INSERTHBASE_MAPCOUNT, "20");
        args[8] = props.getProperty(RestrictionConfig.INSERTHBASE_REDUCECOUNT, "120");

        args[9] = props.getProperty(RestrictionConfig.ROAD_RASTER_TABLE, "road_raster");

        args[10] = props.getProperty(RestrictionConfig.ROAD_RASTER_COLFAMILY, "1607");

        System.exit(ToolRunner.run(new InsertCrossDriver(), args));
    }

    public static void main(String[] args){
        try {
            System.exit(ToolRunner.run(new InsertCrossDriver(), args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
