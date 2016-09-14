package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/2/1.
 */
public class SingleRoadDetection {


    Configuration configuration = null;
    Hbase hbase = null;

    public SingleRoadDetection(){
        configuration = HBaseConfiguration.create();
        //configuration.set("hbase.rootdir", "hdfs://Master.Hadoop:9000/hbase");
        configuration.set("hbase.zookeeper.quorum","Master.Hadoop:2181");
        //configuration.set("hbase.zookeeper.quorum","datanode01:2181,datanode02:2181,datanode03:2181");
        //configuration.set("hbase.master","Slave3.Hadoop");
        hbase = Hbase.createWithConfiguration(configuration);
    }

    public void scanTable() throws IOException{

        FileWriter fw=new FileWriter(new File("C:\\Users\\cuiliang.NAVINFO\\Desktop\\开发\\tunnel.txt"));

        BufferedWriter bw = new BufferedWriter(fw);

        Table table = hbase.getTable("road_source");
        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(true);
        ResultScanner scanner = table.getScanner(scan);

        for (Result result : scanner) {

            boolean existRoad = false;
            boolean existSource = false;
            boolean existDetect = false;
            for (Cell cell : result.rawCells()) {

                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));
                if (family.equals("road") && qualifier.equals("tunnel")) {
                    existRoad = true;
                }
//                if (family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY) && qualifier.equals("sogou")) {
//                    existSource = true;
//                }
//                if (family.equals(Constants.ROAD_DETECT_DETECT_FAMILY) && qualifier.equals("sogou")) {
//                    existDetect = true;
//                }

            }
            if (existRoad) {
                for (Cell cell : result.rawCells()) {
                    String rowKey = new String(CellUtil.cloneRow(cell));
                    System.out.println(StringUtil.reverse(rowKey));
                    bw.write(StringUtil.reverse(rowKey));
                    bw.newLine();
                }
            }
        }
        bw.close();
        fw.close();

        table.close();
    }

    public void deleteTableColumn(String type ,String source) throws IOException{

        Table table = hbase.getTable(Constants.ROAD_DETECT_TABLE);
        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(true);
        ResultScanner scanner = table.getScanner(scan);
        for (Result result : scanner) {
            String rowKey = Bytes.toString(result.getRow());

            boolean qualifierExist = false;
            boolean sourceFamilyExist = false;
            boolean detectFamilyExist = false;


            for (Cell cell : result.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell), "UTF-8");
                String qualifier = new String(CellUtil.cloneQualifier(cell), "UTF-8");

                if(type.equals("all")){
                    if(family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY)){
                        sourceFamilyExist = true;
                    }
                    if(family.equals(Constants.ROAD_DETECT_DETECT_FAMILY)){
                        detectFamilyExist = true;
                    }
                }
                else{
                    if(type.equals(family) && family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY)){
                        sourceFamilyExist = true;
                    }
                    if(type.equals(family) && family.equals(Constants.ROAD_DETECT_DETECT_FAMILY)){
                        detectFamilyExist = true;
                    }
                }
                if (qualifier.equals(source)) {
                    qualifierExist = true;
                }

            }
            if (qualifierExist) {
                Delete delete = new Delete(Bytes.toBytes(rowKey));
                if(sourceFamilyExist){
                    System.out.println("rowKey=" + rowKey + "   " + Constants.ROAD_DETECT_SOURCE_FAMILY + ":" +source);
                    delete.addColumn(Bytes.toBytes(Constants.ROAD_DETECT_SOURCE_FAMILY), Bytes.toBytes(source));
                }
                if(detectFamilyExist){
                    delete.addColumn(Bytes.toBytes(Constants.ROAD_DETECT_DETECT_FAMILY), Bytes.toBytes(source));
                }
                table.delete(delete);
            }
        }
    }


    private int initFilter = 10;
    private int firstPixelFilter = 100;
    private double eccentricityFilter = 0.95;
    private int secondPixelFilter = 200;

    private int expansionLength = 10;


    public void getOutput(String rowKey, String path) throws Exception {
        Table table = hbase.getTable(Constants.ROAD_DETECT_TABLE);
        Get get = new Get(rowKey.getBytes());
        String road = "road";
        Result result = table.get(get);

        SerializeUtil<int[][]> roadSerializeUtil = new SerializeUtil();
        ImageAlgorithm arrayUtil = new ImageAlgorithm();
        byte[] roadByte = result.getValue(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(), "new".getBytes());

        int[][] sparse = roadSerializeUtil.deserialize(roadByte);
        DoubleMatrix dMatrix = new DoubleMatrix(sparse[0], sparse[1], sparse[2]);
        int[][] roadMatrix_1 = dMatrix.toIntArray2();


        File headPath = new File(path + File.separator + road + "/");
        if(!headPath.exists()){
            headPath.mkdirs();
        }
        arrayUtil.arrayToFile(roadMatrix_1, path + File.separator + road + "/" + rowKey + "_1.txt");
        Integer[][] roadMatrix_2 = dMatrix.toIntegerArray2();
        arrayUtil.arrayToFile(roadMatrix_2, path + File.separator + road + "/" + rowKey + "_2.txt");


    }

    public void queryByRowKey(String path ,String rowKey) throws Exception{
        Table table = hbase.getTable("road_source");
        Get get = new Get(rowKey.getBytes());
        Result result = table.get(get);
        File headPath = new File(path + File.separator + rowKey + "/");
        if(!headPath.exists()){
            headPath.mkdirs();
        }
        byte[] roadByte = result.getValue(
                Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                "data".getBytes());

        byte[] tunnelByte = result.getValue(
                Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                "tunnel".getBytes());
        Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte ,true);
        Integer[][] tunnelMatrix = MatrixUtil.deserializeMatrix(tunnelByte ,true);
        MatrixUtil.matrix2Image(roadMatrix, headPath + File.separator + "road.jpg");
        MatrixUtil.matrix2Image(tunnelMatrix, headPath + File.separator + "tunnel.jpg");

    }


    public void getByRowKey(String rowKey,String path,String source) throws Exception{

        String fatherPath = path + File.separator  + source + File.separator;

        Table table = hbase.getTable("road_detect_12");
        Get get = new Get(rowKey.getBytes());
        //String source = "didi";
        Result result = table.get(get);

//        File headPath = new File(path + File.separator + s + "/");
//        if(!headPath.exists()){
//            headPath.mkdirs();
//        }


        boolean existRoad = false;
        boolean existSource = false;

        SerializeUtil<Integer[][]> serializeUtil = new SerializeUtil();

        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();

        ConnectedAlgorithm ca = new ConnectedAlgorithm();

        for (Cell cell : result.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));

            if (family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && qualifier.equals("data")) {
                existRoad = true;
            }
            if (family.equals(Constants.ROAD_DETECT_SOURCE_FAMILY) && qualifier.equals(source)) {
                existSource = true;
            }
        }

        if (existSource && existRoad) {

            byte[] roadByte = result.getValue(
                    Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(),
                    Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());

            byte[] sourceByte = result.getValue(
                    Constants.ROAD_DETECT_SOURCE_FAMILY.getBytes(),
                    source.getBytes());

            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(roadByte ,true);
            MatrixUtil.matrix2Image(roadMatrix,fatherPath + "road.jpg");
            imageAlgorithm.arrayToFile(roadMatrix, fatherPath + "road.txt");


            Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(sourceByte ,true);
            MatrixUtil.matrix2Image(sourceMatrix,fatherPath + "source.jpg");
            imageAlgorithm.arrayToFile(sourceMatrix, fatherPath + "source.txt");


            Integer[][] filterMatrix;

            if (!source.equals(Constants.SOURCE_BAIDU)) {
                //step 1: 小于10个点的像素滤掉
                filterMatrix = imageAlgorithm.filterLessThanPara(sourceMatrix, initFilter);
            } else {
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

            //step 5: 计算区域斜率、离心率
            Map<String, List<Integer[]>> region1 = imageAlgorithm.matrixToRegion(ff_Matrix1, 0);
            Map<String, Map<String, Object>> calculateResult1 = imageAlgorithm.CalEccentricityAndOrientation(region1, false, 0);
            //step 6: 顺斜率方向膨胀，二值化
            Map<String, Object> oriMap1 = calculateResult1.get("orientation");
            Map<String, List<int[]>> lineMap1 = imageAlgorithm.CalculationLineStrel(expansionLength, oriMap1);
            Integer[][] exp_Matrix1 = imageAlgorithm.expansion(region1, lineMap1);
            //step 7: 寻找连通区域，保留连通区域大于100像素，且离心率大约0.95的区域
            Integer[][] init_ff_Matrix2 = imageAlgorithm.initFloodFill(exp_Matrix1);
            //Integer[][] ff_Matrix2 = imageAlgorithm.floodFill(init_ff_Matrix2);
            Integer[][] ff_Matrix2 = ca.doLabel(init_ff_Matrix2);

            Map<String, List<Integer[]>> region2 = imageAlgorithm.matrixToRegion(ff_Matrix2, firstPixelFilter);
            Map<String, Map<String, Object>> calculateResult2 = imageAlgorithm.CalEccentricityAndOrientation(region2, true, eccentricityFilter);
            Map<String, Object> oriMap2 = calculateResult2.get("xyList");
            Integer[][] exp_Matrix2 = imageAlgorithm.regionToMatrix(oriMap2);

            //step 8: 与step1的结果求交集 并再次用路网擦除
            Integer[][] intersectionMatrix = imageAlgorithm.matrixIntersection(filterMatrix, exp_Matrix2);
            Integer[][] minusMatrix2 = imageAlgorithm.matrixMinus(intersectionMatrix, roadMatrix);

            //step 9: 寻找连通区域，计算区域斜率、离心率顺方向膨胀保留连通区域>=200的区域
            Integer[][] init_ff_Matrix3 = imageAlgorithm.initFloodFill(minusMatrix2);
            //Integer[][] ff_Matrix3 = imageAlgorithm.floodFill(init_ff_Matrix3);
            Integer[][] ff_Matrix3 = ca.doLabel(init_ff_Matrix3);

            Map<String, List<Integer[]>> region3 = imageAlgorithm.matrixToRegion(ff_Matrix3, 0);

            Map<String, Map<String, Object>> calculateResult3 = imageAlgorithm.CalEccentricityAndOrientation(region3, false, 0);
            Map<String, Object> oriMap3 = calculateResult3.get("orientation");

//                if (source.equals(Constants.SOURCE_BAIDU)) {
//                    expansionLength = 5;
//                }

            Map<String, List<int[]>> lineMap2 = imageAlgorithm.CalculationLineStrel(expansionLength, oriMap3);
            Integer[][] exp_Matrix3 = imageAlgorithm.expansion(region3, lineMap2);
            Integer[][] init_ff_Matrix4 = imageAlgorithm.initFloodFill(exp_Matrix3);
            //Integer[][] ff_Matrix4 = imageAlgorithm.floodFill(init_ff_Matrix4);
            Integer[][] ff_Matrix4 = ca.doLabel(init_ff_Matrix4);

            Map<String, List<Integer[]>> region4 = imageAlgorithm.matrixToRegion(ff_Matrix4, secondPixelFilter);

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
            MatrixUtil.matrix2Image(skeletonArray,fatherPath + "10.jpg");
            imageAlgorithm.arrayToFile(skeletonArray, fatherPath + "10.txt");
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
                byte[] finalByte = serializeUtil.serialize(skeletonArray);
            }
        }
    }

    public void resultDisplay(String rowKey,String path,String source) throws Exception {

        String fatherPath = path + File.separator + rowKey + File.separator + source + File.separator;

        Table table = hbase.getTable("road_detect_12");
        //Table table = hbase.getTable("road_detect_result");
        Get get = new Get(StringUtil.reverse(rowKey).getBytes());
        Result result = table.get(get);

        File headPath = new File(fatherPath);
        if(!headPath.exists()){
            headPath.mkdirs();
        }

        byte[] detectByte = result.getValue(
                //Constants.ROAD_DETECT_DETECT_FAMILY.getBytes(),
                "201604".getBytes(),
                source.getBytes());
        Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(detectByte ,true);

        MatrixUtil.matrix2Image(detectMatrix, fatherPath + rowKey + ".jpg");
    }

    public static List<String> fileToList(String fileName){
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try{
            File file = new File(fileName);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file)));
            String line = null;
            while((line = br.readLine()) != null){
                if(line!=null&&!"".equals(line.trim()))
                    list.add(line.trim());
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public static void main(String[] args) throws Exception {

//        List<String> list = SingleRoadDetection.fileToList("F:\\rowkey_list.txt");

        SingleRoadDetection singleRoadDetection = new SingleRoadDetection();
        //singleRoadDetection.deleteTableColumn("all","sogou");
        //3314_1626
        //3437_1687
        //3401_1695
        //3365_1545
        //3231_1680
        singleRoadDetection.getByRowKey(StringUtil.reverse("3430_1672"),"E:\\fusion\\road\\trace","sogou");
//        singleRoadDetection.getByRowKey(StringUtil.reverse("3437_1687"));
//        singleRoadDetection.getByRowKey(StringUtil.reverse("3401_1695"));
//        singleRoadDetection.getByRowKey(StringUtil.reverse("3365_1545"));
//        singleRoadDetection.getByRowKey(StringUtil.reverse("3219_1752"));
//        singleRoadDetection.scanTable();
//        singleRoadDetection.queryByRowKey("C:\\Users\\cuiliang.NAVINFO\\Desktop\\开发\\tunnel",StringUtil.reverse("3347_1780"));
//        singleRoadDetection.resultDisplay("3375_1551", "C:\\Users\\cuiliang.NAVINFO\\Desktop\\分析结果","baidu");
//        for(String rowkey:list){
//            System.out.println(rowkey);
//            singleRoadDetection.getByRowKey(StringUtil.reverse(rowkey));
//        }


        //singleRoadDetection.getOutput(StringUtil.reverse("3365_1545"),"E:\\fusion\\road\\test\\");
        //5451_5633
        //singleRoadDetection.scanTable();
    }
}
