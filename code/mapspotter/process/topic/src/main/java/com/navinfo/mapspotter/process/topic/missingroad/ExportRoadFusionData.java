package com.navinfo.mapspotter.process.topic.missingroad;

import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.MatrixUtil;
import com.navinfo.mapspotter.process.topic.roaddetect.Constants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by cuiliang on 2016/2/4.
 * 根据文件中的瓦片列表或者瓦片号获取矩阵数据
 */
public class ExportRoadFusionData {

    public ExportRoadFusionData(String source, String road_table, String road_family, String source_table,
                                String target_table, String family_name, String resultType, String quorum) {
        this.source = source;

        this.road_table = road_table;
        this.road_family = road_family;

        this.source_table = source_table;
        this.target_table = target_table;
        this.family_name = family_name;
        this.resultType = resultType;
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", quorum);

        char[] type = resultType.toCharArray();

        if(type[0] == '1'){
            road_is_output = true;
        }

        if(type[1] == '1'){
            source_is_output = true;
        }

        if(type[2] == '1'){
            detect_is_output = true;
        }
    }

    public static Configuration configuration;

    private String source;
    private String road_table;
    private String road_family;
    private String source_table;
    private String target_table;
    private String family_name;
    private String resultType;

    private boolean road_is_output = false;
    private boolean source_is_output = false;
    private boolean detect_is_output = false;


    private static String QUALIFIER_ROAD = "road";
    private static String QUALIFIER_TUNNEL = "tunnel";
    private static String QUALIFIER_RAILWAY = "railway";
    private static String QUALIFIER_FERRY = "ferry";

    public Map<String, byte[]> GetRecordByRowKey(String rowKey)
            throws IOException {

        Connection connection = ConnectionFactory.createConnection(configuration);
        Get get = new Get(rowKey.getBytes());
        Map<String, byte[]> returnMap = new HashMap<>();

        if(road_is_output){
            Table r_table = connection.getTable(TableName.valueOf(road_table));
            Result rr = r_table.get(get);

            for (Cell cell : rr.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));

                if (family.equals(road_family) && qualifier.equals(QUALIFIER_ROAD)) {
                    byte[] qualifierByte = rr.getValue(road_family.getBytes(), QUALIFIER_ROAD.getBytes());
                    returnMap.put(QUALIFIER_ROAD, qualifierByte);
                }
                if (family.equals(road_family) && qualifier.equals(QUALIFIER_TUNNEL)) {
                    byte[] qualifierByte = rr.getValue(road_family.getBytes(), QUALIFIER_TUNNEL.getBytes());
                    returnMap.put(QUALIFIER_TUNNEL, qualifierByte);
                }
                if (family.equals(road_family) && qualifier.equals(QUALIFIER_RAILWAY)) {
                    byte[] qualifierByte = rr.getValue(road_family.getBytes(), QUALIFIER_RAILWAY.getBytes());
                    returnMap.put(QUALIFIER_RAILWAY, qualifierByte);
                }
                if (family.equals(road_family) && qualifier.equals(QUALIFIER_FERRY)) {
                    byte[] qualifierByte = rr.getValue(road_family.getBytes(), QUALIFIER_FERRY.getBytes());
                    returnMap.put(QUALIFIER_FERRY, qualifierByte);
                }
            }
            r_table.close();
        }

        if(source_is_output) {
            Table s_table = connection.getTable(TableName.valueOf(source_table));
            Result sr = s_table.get(get);
            for (Cell cell : sr.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));

                for (String s : source.split("\\|")) {
                    if (family.equals(family_name) && qualifier.equals(s)) {
                        byte[] qualifierByte = sr.getValue(family_name.getBytes(), s.getBytes());
                        returnMap.put(s, qualifierByte);
                    }
                }
            }
            s_table.close();
        }

        if(detect_is_output) {
            Table t_table = connection.getTable(TableName.valueOf(target_table));
            Result tr = t_table.get(get);
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
            t_table.close();
        }
        connection.close();
        return returnMap;
    }

    public void ExportByInput(String rowkey, String output, String source) throws Exception {
        Map<String, byte[]> map = null;
        try {
            StringBuilder sb = new StringBuilder(rowkey);
            map = GetRecordByRowKey(sb.reverse().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = output;
        ImageAlgorithm arrayUtil = new ImageAlgorithm();
        for (String s : source.split("\\|")) {
            if (map.get(s) != null) {
                File headPath = new File(path + File.separator + s + "/");
                if (!headPath.exists()) {
                    headPath.mkdirs();
                }
                Integer[][] sourceMatrix = MatrixUtil.deserializeMatrix(map.get(s), true);
                arrayUtil.arrayToFile(sourceMatrix, path + File.separator + File.separator + s + "/" + rowkey + ".txt");
            }
        }
        if (map.get(Constants.ROAD_DETECT_ROAD_FAMILY) != null) {
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_ROAD_FAMILY + "/");
            if (!headPath.exists()) {
                headPath.mkdirs();
            }
            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_ROAD_FAMILY), true);
            arrayUtil.arrayToFile(roadMatrix, path + File.separator + Constants.ROAD_DETECT_ROAD_FAMILY + "/" + rowkey + ".txt");
        }
        if (map.get(Constants.ROAD_DETECT_TUNNEL_QUALIFIER) != null) {
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_TUNNEL_QUALIFIER + "/");
            if (!headPath.exists()) {
                headPath.mkdirs();
            }
            Integer[][] roadMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_TUNNEL_QUALIFIER), true);
            arrayUtil.arrayToFile(roadMatrix, path + File.separator + Constants.ROAD_DETECT_TUNNEL_QUALIFIER + "/" + rowkey + ".txt");
        }

        if (map.get(Constants.ROAD_DETECT_DETECT_FAMILY) != null) {
            File headPath = new File(path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY + "/");
            if (!headPath.exists()) {
                headPath.mkdirs();
            }
            Integer[][] detectMatrix = MatrixUtil.deserializeMatrix(map.get(Constants.ROAD_DETECT_DETECT_FAMILY), true);
            arrayUtil.arrayToFile(detectMatrix, path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY + "/" + rowkey + ".txt");
            arrayUtil.byteToFile(map.get(source + "_confidence"),
                    path + File.separator + Constants.ROAD_DETECT_DETECT_FAMILY + "/" + rowkey + ".confidence");
        }
    }

    public void ExportByFile(String fileName, String output, String source, int thread_num) {
        List<String> list = this.fileToList(fileName);
        ExecutorService exs = Executors.newFixedThreadPool(thread_num);
        ArrayList<Future<String>> al = new ArrayList();
        for (String rowkey : list) {
            try {
                al.add(exs.submit(new MultThreadCallable(this, rowkey, output, source)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        exs.shutdown();
    }

    public static void main(String[] args) {
        String source = args[0];

        String road_table = args[1];
        String road_family = args[2];

        String source_table = args[3];
        String target_table = args[4];
        String family_name = args[5];
        String quorum = args[6];
        String type = args[7];
        String arg = args[8];
        String output = args[9];
        String resultType = args[10];
        int thread_num = Integer.parseInt(args[11]);
        ExportRoadFusionData export = new ExportRoadFusionData(source, road_table, road_family, source_table,
                target_table, family_name, resultType, quorum);
        if ("file".equals(type)) {
            export.ExportByFile(arg, output, source, thread_num);
        } else {
            try {
                export.ExportByInput(arg, output, source);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static List<String> fileToList(String fileName) {
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            File file = new File(fileName);
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line != null && !"".equals(line.trim()))
                    list.add(line.trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

        public MultThreadCallable(ExportRoadFusionData exp, String rowKey, String output, String source) {
            this.exp = exp;
            this.rowKey = rowKey;
            this.output = output;
            this.source = source;
        }

        public String call() throws IOException {
            try {
                exp.ExportByInput(rowKey, output, source);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
