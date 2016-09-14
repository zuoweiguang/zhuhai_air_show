package com.navinfo.mapspotter.process.topic.roaddetect;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.*;
import com.vividsolutions.jts.geom.Coordinate;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
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

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/1/31
 * 路网挖掘结果导出并串LINK程序
 */
public class RoadDetectionExportMR {
    public static class RoadDetectionExportMapper extends TableMapper<Text, Text> {

        private String source = "";
        private String version = "";

        @Override
        public void setup(Context context) {
            source = context.getConfiguration().get("source");
            version = context.getConfiguration().get("version");
        }
        ConnectedAlgorithm ca = new ConnectedAlgorithm();
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        int level = 12;
        MercatorUtil mkt = new MercatorUtil(1024, level);
        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());

            String mCode = StringUtil.reverse(rowKey);
            try {
                for (Cell cell : result.rawCells()) {
                    String family = new String(CellUtil.cloneFamily(cell));
                    String qualifier = new String(CellUtil.cloneQualifier(cell));

                    if (family.equals(version) && qualifier.equals(source)) {
                        byte[] detectByte = result.getValue(version.getBytes(), source.getBytes());
                        if (detectByte != null && detectByte.length > 0) {
                            JSONObject json = new JSONObject();
                            Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(detectByte, true);

                            int width = 1024;
                            int height = 1024;

                            ByteProcessor bp = new ByteProcessor(width, height);
                            BinaryProcessor ibp = new BinaryProcessor(bp);

                            ibp.setColor(Color.white);
                            ibp.fill();
                            for (int x = 0; x < 1024; x++) {
                                for (int y = 0; y < 1024; y++) {
                                    if (detectMatrix[x][y] != null && detectMatrix[x][y] !=0)
                                        ibp.putPixel(x, y, 0);
                                }
                            }

                            ibp.skeletonize();

                            Integer[][] matrix = new Integer[1024][1024];
                            for (int x = 0; x < 1024; x++) {
                                for (int y = 0; y < 1024; y++) {
                                    if (ibp.get(x, y) == 0) {
                                        matrix[x][y] = 1;
                                    }
                                }
                            }

                            Integer[][] scanMatrix = new Integer[3][3];
                            List<int[]> list = new ArrayList<>();
                            for (int i = 0; i < matrix.length - 2; i++) {
                                for (int j = 0; j < matrix.length - 2; j++) {
                                    scanMatrix[0][0] = matrix[i][j] != null ? 1 : 0;
                                    scanMatrix[0][1] = matrix[i][j + 1] != null ? 1 : 0;
                                    scanMatrix[0][2] = matrix[i][j + 2] != null ? 1 : 0;
                                    scanMatrix[1][0] = matrix[i + 1][j] != null ? 1 : 0;
                                    scanMatrix[1][1] = matrix[i + 1][j + 1] != null ? 1 : 0;
                                    scanMatrix[1][2] = matrix[i + 1][j + 2] != null ? 1 : 0;
                                    scanMatrix[2][0] = matrix[i + 2][j] != null ? 1 : 0;
                                    scanMatrix[2][1] = matrix[i + 2][j + 1] != null ? 1 : 0;
                                    scanMatrix[2][2] = matrix[i + 2][j + 2] != null ? 1 : 0;
                                    int sum = 0;
                                    for (int x = 0; x < scanMatrix.length; x++) {
                                        for (int y = 0; y < scanMatrix.length; y++) {
                                            sum = sum + scanMatrix[x][y];
                                        }
                                    }
                                    if (sum > 3 && scanMatrix[1][1] >= 1) {
                                        matrix[i + 1][j + 1] = 0;
                                        int[] xcoord = {i + 1, j + 1};
                                        list.add(xcoord);
                                    }
                                }
                            }

                            Integer[][] initff_Matrix = imageAlgorithm
                                    .initFloodFill2(matrix);

                            //Integer[][] ff_Matrix = imageAlgorithm.floodFill(initff_Matrix);
                            Integer[][] ff_Matrix = ca.doLabel(initff_Matrix);

                            String[][] resultArray = new String[1024][1024];

                            for (int x = 0; x < ff_Matrix.length; x++) {
                                for (int y = 0; y < ff_Matrix[x].length; y++) {
                                    if (ff_Matrix[x][y] == null)
                                        resultArray[x][y] = "0";
                                    else
                                        resultArray[x][y] = String.valueOf(ff_Matrix[x][y]);
                                }
                            }
//                            for (int[] xcoord : list) {
//                                resultArray[xcoord[0]][xcoord[1]] = "x";
//                            }

//                            String[][] finalArray = imageAlgorithm
//                                    .batchDeal(resultArray);
//                            String[][] lastFinalArray = imageAlgorithm
//                                    .batchDeal(finalArray);

                            Map<String, List<int[]>> region = imageAlgorithm
                                    .matrixToRegion(resultArray);
                            json.put("mercator", mCode);
                            List<List<double[]>> links = new ArrayList<>();
                            @SuppressWarnings("rawtypes")
                            Iterator iter = region.entrySet().iterator();
                            while (iter.hasNext()) {
                                @SuppressWarnings("rawtypes")
                                Map.Entry entry = (Map.Entry) iter.next();
                                @SuppressWarnings("unchecked")
                                List<int[]> xyList = (List<int[]>) entry
                                        .getValue();
                                System.out.println(mCode);
                                xyList = imageAlgorithm.sortNode(xyList);

                                List<double[]> doubleList = new ArrayList<>();
                                if (xyList != null && xyList.size() > 0) {

                                    for (int[] xy : xyList) {
                                        int x = xy[0];
                                        int y = xy[1];

                                        IntCoordinate pixel = mkt.inTile2Pixels(x, y, mCode);
                                        Coordinate coord = mkt.pixels2LonLat(pixel);

                                        DecimalFormat df = new DecimalFormat("#.#####");
                                        double[] lonLat = {Double.parseDouble(df.format(coord.x)), Double.parseDouble(df.format(coord.y))};

                                        doubleList.add(lonLat);
                                    }
                                }
                                links.add(doubleList);
                            }
                            json.put("links", links);
                            if (links.size() > 0)
                                context.write(new Text(json.toString()),
                                        new Text(""));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.write(new Text(mCode), new Text(""));
            }
        }
    }

    public static class RoadDetectionExportReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text val : values) {
                context.write(key, val);
            }
        }
    }

    public static void main(String[] args) throws IOException,
            ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        String table_name = args[0];
        String version = args[1];
        String source = args[2];
        String output = args[3];
        conf.set("version", version);
        conf.set("source", source);
        Job job = Job.getInstance(conf, "NewRoadDetectionExportMR");
        job.setJarByClass(RoadDetectionExportMR.class);

        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(false);

        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, RoadDetectionExportMR.RoadDetectionExportMapper.class, Text.class,
                Text.class, job);

        job.setReducerClass(RoadDetectionExportMR.RoadDetectionExportReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);
        FileOutputFormat.setOutputPath(job, new Path(output));
        boolean b = job.waitForCompletion(true);
        System.out.println("result flag : " + b);
    }
}
