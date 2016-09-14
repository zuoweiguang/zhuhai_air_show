package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by ZhangJin1207 on 2016/4/28.
 */
public class GetHBaseData {
    private String zookeeperHost;
    private String tableName;
    private String family;
    private String qualifier;
    Hbase hbase = null;
    Table hTable = null;
    ConstructionBase constructionBase = new ConstructionBase();
    ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
    SerializeUtil<int[][]> serializeUtil = new SerializeUtil<>();
    public GetHBaseData(String zookeeperHost , String tableName , String family , String qualifier){
        this.zookeeperHost = zookeeperHost;
        this.tableName = tableName;
        this.family = family;
        this.qualifier = qualifier;
    }

    public boolean connect_hbase(){
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum" , zookeeperHost);
        hbase = Hbase.createWithConfiguration(configuration);
        if (hbase == null){
            return false;
        }
        hTable = hbase.getTable(tableName);
        return true;
    }

    public Result query(String rowkey){
        Result result = null;
        try{
            Get get = new Get(rowkey.getBytes());
            get.addColumn(family.getBytes() , qualifier.getBytes());
            result = hTable.get(get);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public void exportdata_rowkey(String rowkey , String outPath , int flag , int level , int value){
        if (rowkey.isEmpty() || outPath.isEmpty()){
            return;
        }

        StringBuilder stringBuilder = new StringBuilder(rowkey);
        Result rt = query(stringBuilder.reverse().toString());
        byte[] datas = rt.getValue(family.getBytes() , qualifier.getBytes());
        if (datas == null){
            System.out.println("Data not found!" + rowkey);
            return;
        }

        int[][] dataArr = serializeUtil.deserialize(datas);
        DoubleMatrix matrix = new DoubleMatrix(dataArr[0] , dataArr[1] , dataArr[2]);

        String filename = "";
        if (flag == 0) {
            int[][] matrixArr = matrix.toIntArray2();
            filename = outPath + "/" + rowkey + ".txt";
            System.out.println(filename);
            constructionBase.exportTXT(outPath + "/" + rowkey + ".txt" , matrixArr);
            return;
        }else{
            Integer[][] matrixArr = matrix.toIntegerArray2();
            Integer[][] medianMatrix = imageAlgorithm.medianFilter(matrixArr , level);
            Integer[][] resultMatrix = imageAlgorithm.filterLessThanPara(medianMatrix , value);

            int[][] filterM = new int[matrixArr.length][matrixArr[0].length];
            int[][] sourceM = new int[matrixArr.length][matrixArr[0].length];

            for (int i = 0 ; i < matrixArr.length ; i++){
                for (int j = 0 ; j < matrixArr[0].length ; j++){
                    if (matrixArr[i][j] == null){
                        sourceM[i][j] = 0;
                    }else{
                        sourceM[i][j] = matrixArr[i][j];
                    }

                    if (resultMatrix[i][j] == null){
                        filterM[i][j] = 0;
                    }else {
                        filterM[i][j] = resultMatrix[i][j];
                    }
                }
            }

            constructionBase.exportTXT(outPath + "/" + rowkey + "_F.txt" , filterM);
            constructionBase.exportTXT(outPath + "/" + rowkey + ".txt" , sourceM);
        }
    }

    public void exportdata(String fileName , String outpath , int flag , int level , int value){
        if (fileName.isEmpty() || outpath.isEmpty()){
            return;
        }

        File file = new File(fileName);
        if (!file.exists()){
            return;
        }
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String strLine;
            while ((strLine = bufferedReader.readLine()) != null){
                strLine = strLine.trim();
                System.out.println("begin deal " + strLine);
                exportdata_rowkey(strLine , outpath , flag , level , value);
            }

            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        String filename = "";
        String outpath = "";
        String zookeekerHost = "";
        String tablename = "";
        String family = "";
        String qualifier = "";
        int flag = 0;
        int level = 3;
        int value = 5;

        if (args.length < 6){
            return;
        }
        zookeekerHost = args[0];
        tablename = args[1];
        family = args[2];
        qualifier = args[3];
        filename = args[4];
        outpath = args[5];

        if(args.length == 7){
            flag = Integer.parseInt(args[6]);
        }else if (args.length == 8){
            flag = Integer.parseInt(args[6]);
            level = Integer.parseInt(args[7]);
        }else if (args.length > 8){
            flag = Integer.parseInt(args[6]);
            level = Integer.parseInt(args[7]);
            value = Integer.parseInt(args[8]);
        }

        System.out.println("zookeekerHost:" + zookeekerHost + " tablename:" + tablename + " family:" + family + " qualifier:" + qualifier);
        System.out.println("filename:" + filename + " outpath:" + outpath);
        GetHBaseData getHBaseData = new GetHBaseData(zookeekerHost , tablename , family , qualifier);
        getHBaseData.connect_hbase();
        getHBaseData.exportdata(filename , outpath , flag , level , value);
    }
}
