package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.mongodb.MongoException;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.DateTimeUtil;
import com.navinfo.mapspotter.foundation.util.JsonUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 将交限分析结果的json导入Mongodb
 * Created by SongHuiXing on 2016/3/17.
 */
public class InsertDisplay2Mongo implements AutoCloseable {

    private MongoDB db = null;

    //private File provincePath = null;
    private File crossPath = null;
    private File detailPath = null;

    private JsonUtil jsonUtil = JsonUtil.getInstance();

    public InsertDisplay2Mongo(String host, int port, String database,
                               String dataPath){
        db = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams(host, port, database));

        getDataPath(dataPath);
    }

    private void getDataPath(String path){
        //provincePath = new File(province);
        File f = new File(path);
        if(!f.isDirectory())
            return;

        for (File child : f.listFiles()) {
            String childName = child.getName().toLowerCase();

            if(childName.equals("cross")){
                crossPath = child;
            } else if(childName.equals("detail")) {
                detailPath = child;
            }
        }
    }

    public boolean Insert(){
        boolean res = true;

//        if(provincePath.exists()){
//            res &= insertFolder(provincePath, "restric_province");
//        }

        if(null != crossPath && crossPath.exists()){
            res &= insertFolder(crossPath, "restric_cross");
        }

        if(null != detailPath && detailPath.exists()) {
            res &= insertFolder(detailPath, "restric_detail");
        }

        return res;
    }

    private boolean insertFolder(File folderPath, String tablePrefix){
        String tablename = String.format("sogou_%s_%s", tablePrefix, DateTimeUtil.formatDate("YYYYMMdd"));

        db.drop(tablename);

        if(!db.create(tablename))
            return false;

        try {
            if (folderPath.isDirectory()) {
                String[] files = folderPath.list();
                for (String file : files) {
                    File f = new File(folderPath + File.separator + file);
                    if (f.isDirectory())
                        continue;

                    insertFile(f, tablename);
                }
            } else {
                insertFile(folderPath, tablename);
            }
        } catch (IOException e){
            return false;
        }

        db.update(tablename, new MongoOperator(), "type", "Feature");

        HashMap<String, Boolean> indexs = new HashMap<>();

        indexs.put("properties.tile", true);
        db.createIndex(tablename, indexs);

        indexs.clear();
        indexs.put("properties.tiles", true);
        db.createIndex(tablename, indexs);

        indexs.clear();
        indexs.put("properties.crossid", true);
        db.createIndex(tablename, indexs);

        return true;
    }

    private void insertFile(File f, String tablename) throws IOException {
        InputStreamReader input = new InputStreamReader(new FileInputStream(f));

        BufferedReader reader = new BufferedReader(input);

        ArrayList<String> jsonStrings = new ArrayList<>();
        String lineTxt;
        while (null != (lineTxt = reader.readLine())) {
            jsonStrings.add(lineTxt);
        }

        reader.close();
        input.close();

        if(0 == jsonStrings.size())
            return;

        if(0 != db.insertJsons(tablename, jsonStrings)){
            throw new MongoException(String.format("Write to %s failed.", tablename));
        }
    }

    @Override
    public void close() throws Exception {
        if(null != db){
            db.close();
            db = null;
        }
    }

    public static void main(String[] args){
        if(args.length < 1){
            throw new UnsupportedOperationException("Please input the data path.");
        }

        try(InsertDisplay2Mongo display2Mongo =
                    new InsertDisplay2Mongo("192.168.4.128", 27017, "mapspotter",
                                            args[0])) {

            System.out.println(display2Mongo.Insert());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
