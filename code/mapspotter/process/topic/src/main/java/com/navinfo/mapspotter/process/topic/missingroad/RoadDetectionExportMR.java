package com.navinfo.mapspotter.process.topic.missingroad;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Created by cuiliang on 2016/1/31
 * 路网挖掘结果导出并串LINK程序
 */
public class RoadDetectionExportMR {
    public static class RoadDetectionExportMapper extends TableMapper<Text, Text> {

        private String source = "";
        private String version = "";
        private int matrix_size = 1024;
        private int level = 12;

        private int scan_size = 3;
        private int dis = (scan_size - 1) / 2;

        private ConnectedAlgorithm ca;
        private ImageAlgorithm imageAlgorithm;

        private SerializeUtil<List<int[]>> serializeUtil = new SerializeUtil();

        private Map<String, Integer> tileMap;


        @Override
        public void setup(Context context) throws IOException {
            source = context.getConfiguration().get("source");
            version = context.getConfiguration().get("version");
            ca = new ConnectedAlgorithm();
            imageAlgorithm = new ImageAlgorithm();
        }


        public void map(ImmutableBytesWritable rowkey, Result result,
                        Context context) throws IOException, InterruptedException {
            String rowKey = Bytes.toString(result.getRow());
            String mCode = StringUtil.reverse(rowKey);
            System.out.println(rowKey);
            try {
                for (Cell cell : result.rawCells()) {
                    String family = new String(CellUtil.cloneFamily(cell));
                    String qualifier = new String(CellUtil.cloneQualifier(cell));
                    System.out.println(family+":"+qualifier);
                    if (family.equals(version) && qualifier.equals(source)) {
                        byte[] detectByte = result.getValue(version.getBytes(), source.getBytes());
                        byte[] confidenceByte = result.getValue(version.getBytes(), (source + "_confidence").getBytes());

                        if (detectByte != null && detectByte.length > 0) {
                            /**
                             * 获取置信度信息
                             */
                            String confidence = new String(confidenceByte);
                            JSONArray jsonArray = JSON.parseArray(confidence);
                            Map<Integer, String> tunnelMap = new HashMap();
                            Map<Integer, Double> confidenceMap = new HashMap();

                            for (Object object : jsonArray) {
                                JSONObject json = (JSONObject) object;
//                                if (json.getInteger("tunnel") == 1)
//                                    tunnelMap.put(json.getInteger("key"), "");
                                confidenceMap.put(json.getIntValue("key"), json.getDoubleValue("confidence"));
                            }

                            //JSONObject json = new JSONObject();
                            Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(detectByte, true);
                            //过滤隧道数据
//                            detectMatrix = imageAlgorithm.filterTunnelMatrix(detectMatrix, tunnelMap);


                            /**
                             * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素中心点像素无值而其上下左右的点有值则给该点打上值
                             */
                            Integer[][] scanMatrixPatch = new Integer[scan_size][scan_size];
                            for (int i = 0; i < detectMatrix.length; i++) {
                                for (int j = 0; j < detectMatrix.length; j++) {
                                    for (int m = 0; m < scan_size; m++) {
                                        for (int n = 0; n < scan_size; n++) {
                                            scanMatrixPatch[m][n] = imageAlgorithm.getMatrixValue(detectMatrix, i + m - dis, j + n - dis);
                                        }
                                    }
                                    if (scanMatrixPatch[1][1] == 0) {
                                        if (scanMatrixPatch[0][1] > 0 && scanMatrixPatch[1][0] > 0 && scanMatrixPatch[2][1] > 0 && scanMatrixPatch[1][2] > 0)
                                            detectMatrix[i][j] = scanMatrixPatch[0][1];
                                    }
                                }
                            }


                            //=============================抽骨骼=============================
                            ByteProcessor bp = new ByteProcessor(matrix_size, matrix_size);
                            BinaryProcessor ibp = new BinaryProcessor(bp);
                            ibp.setColor(Color.white);
                            ibp.fill();
                            for (int x = 0; x < matrix_size; x++) {
                                for (int y = 0; y < matrix_size; y++) {
                                    if (detectMatrix[x][y] != null && detectMatrix[x][y] != 0)
                                        ibp.putPixel(x, y, 0);
                                }
                            }
                            ibp.skeletonize();
                            Integer[][] skeletonArray = new Integer[matrix_size][matrix_size];
                            for (int x = 0; x < matrix_size; x++) {
                                for (int y = 0; y < matrix_size; y++) {
                                    if (ibp.get(x, y) == 0) {
                                        skeletonArray[x][y] = detectMatrix[x][y];
                                    }
                                }
                            }
                            //=============================END=============================

                            Integer[][] matrix = imageAlgorithm.binarizationMatrix(skeletonArray);
                            Integer[][] matrix2 = imageAlgorithm.copyMatrix(matrix);


                            Integer[][] scanMatrix = new Integer[scan_size][scan_size];
                            List<int[]> list = new ArrayList();

                            /**
                             * 通过3*3矩阵扫描，如果3*3的矩阵中值的像素大于3则将其记为0，并且将其坐标记录在list中
                             */
                            for (int i = 0; i < matrix.length; i++) {
                                for (int j = 0; j < matrix.length; j++) {
                                    for (int m = 0; m < scan_size; m++) {
                                        for (int n = 0; n < scan_size; n++) {
                                            scanMatrix[m][n] = imageAlgorithm.getMatrixValue(matrix, i + m - dis, j + n - dis);
                                        }
                                    }
                                    int sum = 0;
                                    for (int x = 0; x < scanMatrix.length; x++) {
                                        for (int y = 0; y < scanMatrix.length; y++) {
                                            sum = sum + scanMatrix[x][y];
                                        }
                                    }
                                    if (sum > 3 && scanMatrix[1][1] >= 1) {
                                        matrix2[i][j] = 0;
                                        int[] xcoord = {i, j};
                                        list.add(xcoord);
                                    }
                                }
                            }

                            /**
                             * 去掉交叉区域后找连通区域
                             */
                            Integer[][] init_Matrix = imageAlgorithm.initFloodFill2(matrix2);
                            Integer[][] ff_Matrix = ca.doLabel(init_Matrix);

                            String[][] resultArray = new String[matrix_size][matrix_size];

                            for (int x = 0; x < ff_Matrix.length; x++) {
                                for (int y = 0; y < ff_Matrix[x].length; y++) {
                                    if (ff_Matrix[x][y] == null)
                                        resultArray[x][y] = "0";
                                    else
                                        resultArray[x][y] = String.valueOf(ff_Matrix[x][y]);
                                }
                            }
                            for (int[] xcoord : list) {
                                resultArray[xcoord[0]][xcoord[1]] = "x";
                            }
                            /**
                             * 通过周边值侵蚀x区域
                             */
                            //String[][] finalArray;

                            while (true) {
                                resultArray = imageAlgorithm.batchDeal(resultArray);
                                int x_num = 0;
                                for (String[] array : resultArray) {
                                    for (String str : array) {
                                        if (str.equals("x")) {
                                            x_num++;
                                        }
                                    }
                                }
                                if (x_num == 0)
                                    break;
                            }
                            Map<String, List<int[]>> region = imageAlgorithm.matrixToRegion(resultArray);
                            Iterator iterator = region.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry entry = (Map.Entry) iterator.next();
                                String new_group_code = (String) entry.getKey(); //重新找连通性后的编码
                                List<int[]> xyList = (List<int[]>) entry.getValue();
                                if (xyList != null && xyList.size() > 0) {
                                    int[] start = xyList.get(0);
                                    int group_code = skeletonArray[start[0]][start[1]];
                                    double confidenceValue = confidenceMap.get(group_code);

//                                    if(confidenceValue >= 0.4) {
//                                        String key = mCode + "|" + group_code + "|" + new_group_code + "|" + confidenceValue;
//                                        context.write(new Text(key), new Text(serializeUtil.serialize(xyList)));
//                                    }

                                    String key = mCode + "|" + group_code + "|" + new_group_code + "|" + confidenceValue;
                                    context.write(new Text(key), new Text(serializeUtil.serialize(xyList)));

                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class RoadDetectionExportReducer extends Reducer<Text, Text, Text, Text> {

        private SerializeUtil<List<int[]>> serializeUtil = new SerializeUtil();
        private ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        private MercatorUtil mkt = new MercatorUtil(1024, 12);
        private DecimalFormat df = new DecimalFormat("#.#####");

        private DecimalFormat confidenceFormat = new DecimalFormat("#.##");

        Map<Integer, MercatorUtil> mercatorList = new HashMap();


        @Override
        public void setup(Context context) {
            for (int zoom = 14; zoom <= 18; zoom++) {
                MercatorUtil mercator = new MercatorUtil(1024, zoom);
                mercatorList.put(zoom, mercator);
            }
        }

        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            String key_str = key.toString();
            System.out.println(key_str);
            String[] keyArray = key_str.split("\\|");

            String mCode = keyArray[0];
            String group_code = keyArray[1];
            String new_group_code = keyArray[2];
            double confidenceValue = Double.parseDouble(keyArray[3]);

            for (Text val : values) {
                byte[] bytes = val.getBytes();
                List<int[]> xyList = serializeUtil.deserialize(bytes);
                xyList = imageAlgorithm.sortNodes(xyList);

                if (xyList.size() > 0) {
                    JSONObject json = new JSONObject();

                    JSONObject properties = new JSONObject();
                    properties.put("tile", mCode + "_" + 12);
                    properties.put("key", group_code);
                    properties.put("code", new_group_code);
                    properties.put("confidence", Double.parseDouble(confidenceFormat.format(confidenceValue)));
                    JSONObject geo_json = new JSONObject();
                    geo_json.put("type", "LineString");

                    JSONArray coordinates = new JSONArray();

                    Coordinate first_coord = null;

                    for (int[] xy : xyList) {
                        int x = xy[1];
                        int y = xy[0];
                        IntCoordinate pixel = mkt.inTile2Pixels(x, y, mCode);
                        Coordinate coord = mkt.pixels2LonLat(pixel);
                        JSONArray coordinate = new JSONArray();
                        coordinate.add(Double.parseDouble(df.format(coord.x)));
                        coordinate.add(Double.parseDouble(df.format(coord.y)));
                        coordinates.add(coordinate);

                        if (first_coord == null) {
                            first_coord = coord;
                        }
                    }
                    JSONArray tiles = new JSONArray();
                    for (int zoom = 14; zoom <= 18; zoom++) {
                        MercatorUtil mercator = mercatorList.get(zoom);
                        String rowKey = mercator.lonLat2MCode(first_coord);
                        tiles.add(rowKey + "_" + zoom);
                    }
                    properties.put("tiles", tiles);

                    geo_json.put("coordinates", coordinates);
                    json.put("properties", properties);
                    json.put("geometry", geo_json);

                    context.write(new Text(json.toString()), new Text(""));
                }
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
        int reduceNum = Integer.parseInt(args[4]);


        conf.set("version", version);
        conf.set("source", source);
        Job job = Job.getInstance(conf, "NewRoadDetectionExportMR");
        job.setJarByClass(RoadDetectionExportMR.class);

        Scan scan = new Scan();
        scan.setCaching(10);

        scan.setCacheBlocks(false);
        scan.addColumn(version.getBytes(), source.getBytes());
        scan.addColumn(version.getBytes(), (source + "_confidence").getBytes());
        TableMapReduceUtil.initTableMapperJob(table_name,
                scan, RoadDetectionExportMR.RoadDetectionExportMapper.class, Text.class,
                Text.class, job);

        job.setReducerClass(RoadDetectionExportMR.RoadDetectionExportReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(reduceNum);
        FileOutputFormat.setOutputPath(job, new Path(output));
        boolean b = job.waitForCompletion(true);
        System.out.println("result flag : " + b);
    }
}
