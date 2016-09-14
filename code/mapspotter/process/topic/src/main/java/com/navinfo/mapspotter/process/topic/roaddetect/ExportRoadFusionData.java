package com.navinfo.mapspotter.process.topic.roaddetect;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;


/**
 * Created by cuiliang on 2016/2/4.
 * 根据文件中的瓦片列表或者瓦片号获取矩阵数据
 */
public class ExportRoadFusionData {

    public ExportRoadFusionData(String source, String source_table,String target_table,String family_name, String quorum){
        this.source = source;
        this.source_table = source_table;
        this.target_table = target_table;
        this.family_name = family_name;
        this.quorum = quorum;
        configuration = HBaseConfiguration.create();
        //Master.Hadoop:2181
        configuration.set("hbase.zookeeper.quorum", quorum);
    }
    public static Configuration configuration;

    private String source;
    private String source_table;
    private String target_table;
    private String family_name;
    private String quorum;

    public Map<String, byte[]> GetRecordByRowKey(String rowkey)
            throws IOException {

        Connection connection = ConnectionFactory.createConnection(configuration);

        Table s_table = connection.getTable(TableName
                .valueOf(source_table));
        Table t_table = connection.getTable(TableName
                .valueOf(target_table));

        Get get = new Get(rowkey.getBytes());
        Result sr = s_table.get(get);
        Result tr = t_table.get(get);
        Map<String, byte[]> returnMap = new HashMap<>();

        for (Cell cell : sr.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));

            if (family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && qualifier.equals(Constants.ROAD_DETECT_ROAD_QUALIFIER)) {
                byte[] qualifierByte = sr.getValue(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(), Constants.ROAD_DETECT_ROAD_QUALIFIER.getBytes());
                returnMap.put(Constants.ROAD_DETECT_ROAD_FAMILY, qualifierByte);
            }

            if (family.equals(Constants.ROAD_DETECT_ROAD_FAMILY) && qualifier.equals(Constants.ROAD_DETECT_TUNNEL_QUALIFIER)) {
                byte[] qualifierByte = sr.getValue(Constants.ROAD_DETECT_ROAD_FAMILY.getBytes(), Constants.ROAD_DETECT_TUNNEL_QUALIFIER.getBytes());
                returnMap.put("tunnel", qualifierByte);
            }

            for(String s : source.split("\\|")){
                if (family.equals(family_name) && qualifier.equals(s)) {
                    byte[] qualifierByte = sr.getValue(family_name.getBytes(), s.getBytes());
                    returnMap.put(s, qualifierByte);
                }
            }
        }

        for (Cell cell : tr.rawCells()) {
            String family = new String(CellUtil.cloneFamily(cell));
            String qualifier = new String(CellUtil.cloneQualifier(cell));
            if (family.equals(family_name) && qualifier.equals(source)) {
                byte[] qualifierByte = tr.getValue(family_name.getBytes(), source.getBytes());
                returnMap.put(Constants.ROAD_DETECT_DETECT_FAMILY, qualifierByte);
            }
            if (family.equals(family_name) && qualifier.equals(source + "_confidence")) {
                byte[] qualifierByte = tr.getValue(family_name.getBytes(), (source + "_confidence").getBytes());
                returnMap.put(source + "_confidence", qualifierByte);
            }
        }

        s_table.close();
        t_table.close();
        connection.close();
        return returnMap;
    }

    public void ExportByInput(String rowkey, String output, String source) throws Exception{
        Map<String, byte[]> map = null;
        try {
            StringBuilder sb = new StringBuilder(rowkey);
            map = GetRecordByRowKey(sb.reverse().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = output;
        ImageAlgorithm arrayUtil = new ImageAlgorithm();
        for(String s : source.split("\\|")){
            if(map.get(s) != null){
                File headPath = new File(path + File.separator + s + "/");
                if(!headPath.exists()){
                    headPath.mkdirs();
                }
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(map.get(s) ,true);
                arrayUtil.arrayToFile(sourceMatrix, path + File.separator + File.separator + s + "/" + rowkey + ".txt");
            }
        }
        if(map.get(Constants.ROAD_DETECT_ROAD_FAMILY) != null){
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_ROAD_FAMILY + "/");
            if(!headPath.exists()){
                headPath.mkdirs();
            }
            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_ROAD_FAMILY) ,true);
            arrayUtil.arrayToFile(roadMatrix, path + File.separator + Constants.ROAD_DETECT_ROAD_FAMILY + "/" + rowkey + ".txt");
        }
        if(map.get(Constants.ROAD_DETECT_TUNNEL_QUALIFIER) != null){
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_TUNNEL_QUALIFIER + "/");
            if(!headPath.exists()){
                headPath.mkdirs();
            }
            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_TUNNEL_QUALIFIER) ,true);
            arrayUtil.arrayToFile(roadMatrix, path + File.separator + Constants.ROAD_DETECT_TUNNEL_QUALIFIER + "/" + rowkey + ".txt");
        }

        if(map.get(Constants.ROAD_DETECT_DETECT_FAMILY) != null){
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY+"/");
            if(!headPath.exists()){
                headPath.mkdirs();
            }
            Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_DETECT_FAMILY) ,true);
            arrayUtil.arrayToFile(detectMatrix, path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY + "/" + rowkey + ".txt");
            arrayUtil.byteToFile(map.get(source + "_confidence"),
                    path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY + "/" + rowkey + ".confidence");
        }
    }
    public void ExportByFile(String fileName, String output, String source, int thread_num){
        List<String> list = this.fileToList(fileName);
        ExecutorService exs = Executors.newFixedThreadPool(thread_num);
        ArrayList<Future<String>> al = new ArrayList();
        for(String rowkey :list){
            try{
                al.add(exs.submit(new MultThreadCallable(this, rowkey, output, source)));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        exs.shutdown();
    }

    public static void main(String[] args){
        String source = args[0];
        String source_table = args[1];
        String target_table = args[2];
        String family_name = args[3];
        String quorum = args[4];
        String type = args[5];
        String arg = args[6];
        String output = args[7];
        int thread_num = Integer.parseInt(args[8]);
        ExportRoadFusionData export = new ExportRoadFusionData(source, source_table,target_table,family_name, quorum);
        if("file".equals(type)){
            export.ExportByFile(arg, output, source, thread_num);
        }
        else{
            try{
                export.ExportByInput(arg, output, source);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

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

    public static class MultThreadCallable implements Callable<String> {

        private ExportRoadFusionData exp;
        private String rowKey;
        private String output;
        private String source;
        public MultThreadCallable(ExportRoadFusionData exp, String rowKey, String output, String source){
            this.exp = exp;
            this.rowKey = rowKey;
            this.output = output;
            this.source = source;
        }

        public String call() throws IOException  {
            try {
                exp.ExportByInput(rowKey, output, source);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
