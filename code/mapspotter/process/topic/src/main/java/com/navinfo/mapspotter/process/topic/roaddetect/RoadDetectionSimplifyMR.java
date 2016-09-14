package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.*;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.dom4j.Element;
import org.dom4j.Node;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/12.
 */
public class RoadDetectionSimplifyMR {
    public static class RoadDetectionSimplifyMapper extends
            TableMapper<ImmutableBytesWritable, Put> {
        public static final Logger logger = Logger.getLogger(RoadDetectionMR.class);
        private String source = "";
        private int initFilter = 10;
        private int firstPixelFilter = 100;
        private double eccentricityFilter = 0.95;
        private int secondPixelFilter = 200;

        private static final int expansionLength = 10;

        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
            initFilter = Integer.parseInt(context.getConfiguration().get("initFilter"));
            firstPixelFilter = Integer.parseInt(context.getConfiguration().get("firstPixelFilter"));
            secondPixelFilter = Integer.parseInt(context.getConfiguration().get("secondPixelFilter"));
            eccentricityFilter = Double.parseDouble(context.getConfiguration().get("eccentricityFilter"));
        }

        @Override
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {

            boolean existRoad = false;
            boolean existSource = false;
            String rowKey = Bytes.toString(result.getRow());
            ConnectedAlgorithm ca = new ConnectedAlgorithm();
            ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
            for (Cell cell : result.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));
                if (family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && qualifier.equals(Constants.ROAD_DETECT_ROAD_QUALIFIER)) {
                    existRoad = true;
                }
                if (family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY) && qualifier.equals(source)) {
                    existSource = true;
                }
            }
            if (existSource && existRoad /* && !rowKey.equals(StringUtil.reverse("3422_1663"))*/) {
                System.out.println(StringUtil.reverse(rowKey));
                byte[] roadByte = result.getValue(
                        Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                        Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());

                byte[] sourceByte = result.getValue(
                        Constants.ROAD_DETECT_SOURCE_FAMILY.getBytes(),
                        source.getBytes());

                Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte ,true);
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte ,true);
                Integer[][] filterMatrix;

                if(!source.equals(Constants.SOURCE_BAIDU)){
                    //step 1: 小于10个点的像素滤掉
                    filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, initFilter);
                }
                else{
                    filterMatrix = imageAlgorithm.filterStepOne(sourceMatrix, initFilter);
                }

                //step 2: 路网轨迹二值化并擦除
                Integer[][] minusMatrix = imageAlgorithm.matrixMinus(filterMatrix, roadMatrix);

                //step 3: 中值滤波
                Integer[][] medianMatrix = imageAlgorithm.medianFilter(minusMatrix, 3);

                //step 4: 寻找连通区域
                Integer[][] init_ff_Matrix1 = imageAlgorithm.initFloodFill(medianMatrix);
                //Integer[][] ff_Matrix1 = imageAlgorithm.floodFill(init_ff_Matrix1);
                Integer[][] ff_Matrix1 = ca.doLabel(init_ff_Matrix1);

                Map<String, List<Integer[]>> region4 = imageAlgorithm.matrixToRegion(ff_Matrix1, 40);

                Integer[][] final_Matrix = imageAlgorithm.mapToMatrix(region4);

                //step 10:抽骨骼
                int width = 1024;
                int height = 1024;

                ByteProcessor bp = new ByteProcessor(width, height);
                BinaryProcessor ibp = new BinaryProcessor(bp);

                ibp.setColor(Color.white);
                ibp.fill();
                for (int x = 0; x < 1024; x++) {
                    for (int y = 0; y < 1024; y++) {
                        if (final_Matrix[x][y] != null)
                            ibp.putPixel(x, y, 0);
                    }
                }

                ibp.skeletonize();

                Integer[][] skeletonArray = new Integer[1024][1024];
                for (int x = 0; x < 1024; x++) {
                    for (int y = 0; y < 1024; y++) {
                        if (ibp.get(x, y) == 0) {
                            skeletonArray[x][y] = 1;
                        }
                    }
                }

                boolean isEmpty = true;

                for (int i = 0; i < 1024; i++) {
                    for (int j = 0; j < 1024; j++) {
                        if (skeletonArray[i][j] != null
                                && skeletonArray[i][j] != 0) {
                            isEmpty = false;
                        }
                    }
                }
                if (!isEmpty) {
                    byte[] finalByte = MatrixUtil.serializeSparseMatrix(skeletonArray, true);
                    Put put = new Put(rowKey.getBytes());
                    put.addColumn(Constants.ROAD_DETECT_DETECT_FAMILY.getBytes(),
                            (source+"_s").getBytes(), finalByte);
                    context.write(rowkey, put);
                }
            }
        }
    }

    public static class RoadDetectionSimplifyReducer extends
            TableReducer<ImmutableBytesWritable, Put, NullWritable> {
        @Override
        public void reduce(ImmutableBytesWritable key, Iterable<Put> values,
                           Context context) throws IOException, InterruptedException {
            context.write(NullWritable.get(), values.iterator().next());
        }

    }

    public static void main(String[] args) throws Exception {
        String table_name = args[0];
        String source = args[1];
        String config = args[2];
        Configuration conf = new Configuration();
        List<Element> list = XmlUtil.parseXml2List(config);
        conf.set("source", source);
        for (Element element : list) {
            if (source.equals(element.getName().trim())) {
                for (int i = 0, size = element.nodeCount(); i < size; i++) {
                    Node node = element.node(i);
                    if (node instanceof Element) {
                        conf.set(node.getName().trim(), node.getText().trim());
                        System.out.println(node.getName().trim() + ":" + node.getText().trim());
                    }
                }
            }
        }

        // 2015-10-16新增
        conf.set(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, "120000");
        conf.setBoolean("mapred.map.tasks.speculative.execution", false);

        Job job = Job.getInstance(conf, "RoadDetectionSimplifyMR");

        job.setJarByClass(RoadDetectionMR.class);
        Scan scan = new Scan();
        // 2015-10-16新增
        scan.setCacheBlocks(false);
        scan.setCaching(10);

        scan.addColumn(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());
        scan.addColumn(Constants.ROAD_DETECT_SOURCE_FAMILY.getBytes(),
                source.getBytes());
        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, RoadDetectionSimplifyMR.RoadDetectionSimplifyMapper.class,
                ImmutableBytesWritable.class, Put.class, job);
        TableMapReduceUtil.initTableReducerJob(table_name,
                RoadDetectionSimplifyMR.RoadDetectionSimplifyReducer.class, job);
        job.setNumReduceTasks(16);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
