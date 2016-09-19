package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zuoweiguang on 2016/9/19.
 * 脚本功能：将世纪高通提供的 correspondingOfzhuhai.csv 文档转换 存储到mongo中
 */
public class ConvertCsv2Mongo {

    private String fileName = null;
    private JSONObject prop = null;
    private Mongo mongo = null;
    private DB db = null;
    private DBCollection col = null;

    public ConvertCsv2Mongo(String filename) {
        this.fileName = filename;
        prop = PropertiesUtil.getProperties();
        mongo = new MongoClient(prop.getString("mongoHost"), prop.getInteger("mongoPort"));
        db = mongo.getDB(prop.getString("mongoDb"));
        col = db.getCollection("corresponding");
    }

    public void getCorresponding() {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        //创建CSVFormat（header mapping）
        CSVFormat csvFileFormat = CSVFormat.DEFAULT;
        try {
            //初始化FileReader object
            fileReader = new FileReader(fileName);
            csvFileParser = new CSVParser(fileReader, csvFileFormat);
            //CSV文件records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            System.out.println("csv records total:" + csvRecords.size());
            List<DBObject> objList = new ArrayList<>();
            for (int i = 1; i < csvRecords.size(); i++) {
                CSVRecord line = csvRecords.get(i);
                int columeSize = line.size();
//                System.out.println("size:" + columeSize);

                DBObject lineObj = new BasicDBObject();
                List<Integer> linkPidList = new ArrayList<>();
                for (int j = 0; j < columeSize; j ++) {
                    if (j == 0) {
                        int MeshNo = Integer.valueOf(line.get(j));
                        lineObj.put("mesh_code", MeshNo);
                    }
                    else if (j == 1) {
                        int RticLinkKind = Integer.valueOf(line.get(j));
                        lineObj.put("rtic_link_kind", RticLinkKind);
                    }
                    else if (j == 2) {
                        String RTIC_linkID = line.get(j);
                        lineObj.put("rtic_id", RTIC_linkID);
                    }
                    else if (j >= 6 && j % 2 == 0) {
                        if (null != line.get(j)) {
                            int NILink = Integer.valueOf(line.get(j));
                            linkPidList.add(NILink);
                        }

                    }

                }
                lineObj.put("link_pid_list", linkPidList);
                objList.add(lineObj);
                if (i % 2000 == 0) {
                    System.out.println("convert mongo count:" + i);
                    //存mongo
                    col.insert(objList);
                    objList.clear();
                }
//                System.out.println(lineObj.toString());
            }

            //创建索引
            col.createIndex("rtic_id");
            col.createIndex("link_pid_list");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null != mongo) {
                mongo.close();
            }
        }
    }


    public static void main(String[] args) {
        ConvertCsv2Mongo ccm = new ConvertCsv2Mongo("correspondingOfzhuhai.csv");
        ccm.getCorresponding();
    }

}
