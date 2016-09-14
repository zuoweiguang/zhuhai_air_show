package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.io.Hbase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by cuiliang on 2016/3/17.
 */
public class RowCount {
    Configuration configuration = null;
    Hbase hbase = null;

    public RowCount() {
        configuration = HBaseConfiguration.create();
//        configuration.set("hbase.rootdir", "hdfs://Master.Hadoop:9000/hbase");
//        configuration.set("hbase.zookeeper.quorum", "Master.Hadoop:2181");
//        configuration.set("hbase.master", "Slave3.Hadoop");
        hbase = Hbase.createWithConfiguration(configuration);
    }

    public void scanTable(String file, String table_name) throws IOException {
        Table table = hbase.getTable(table_name);
        Scan scan = new Scan();
        scan.setCaching(10);
        scan.setCacheBlocks(true);
        ResultScanner scanner = table.getScanner(scan);
        Map<String, Integer> countMap = new HashMap();
        for (Result result : scanner) {
            for (Cell cell : result.rawCells()) {
                String family = new String(CellUtil.cloneFamily(cell));
                String qualifier = new String(CellUtil.cloneQualifier(cell));
                String key = family + ":" + qualifier;
                byte[] byteData = result.getValue(family.getBytes(), qualifier.getBytes());
                if (byteData != null) {
                    if (countMap.get(key) != null && countMap.get(key) != 0) {
                        countMap.put(key, countMap.get(key) + 1);
                    } else {
                        countMap.put(key, 1);
                    }
                }

            }
        }
        table.close();
        FileWriter fw = new FileWriter(new File(file + table_name + ".txt"));

        BufferedWriter bw = new BufferedWriter(fw);
        Iterator iter = countMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            bw.write(key + ":" + val);
            bw.newLine();
        }
        bw.close();
        fw.close();
    }

    public static void main(String[] args){
        String path = args[0];
        String table = args[1];
        RowCount rc = new RowCount();
        try {
            rc.scanTable(path,table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
