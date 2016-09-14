package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuiliang on 2016/4/20.
 */
public class HdfsTest {
    public static void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.default.name", "hdfs://192.168.4.128:9000");
        FileSystem fs = FileSystem.get(conf);
        String startDate = "20160201";
        String endDate = "20160306";
        String inputPathStr = "/data/road/source/";
        Path path = new Path(inputPathStr);
        List<Path> list = new ArrayList();

        getPathList(fs, path,  list,  startDate,  endDate, "part");
        int size = list.size();
        Path[] pathArray = list.toArray(new Path[size]);
        for(Path a :pathArray){
            System.out.println(a.toString());
        }
        String[] array = new String[size];
        for(int i = 0 ; i < pathArray.length ; i++){
            array[i] = pathArray[i].toString();
        }

        Long.toString(Hdfs.CalMapReduceSplitSize(array, fs, "part", 150));
    }

    public static void getPathList(FileSystem fs, Path path, List<Path> pathArray, String startDate, String endDate, String prefix) throws IOException {
        if (fs.isDirectory(path)) {
            String pathName = path.getName();
            if (pathName.startsWith("gps-track-")) {
                String week = pathName.replace("gps-track-", "");
                String[] weekArray = week.split("-");
                if(weekArray.length == 2){
                    if(Integer.parseInt(weekArray[0]) >= Integer.parseInt(startDate)
                            && Integer.parseInt(weekArray[0]) <= Integer.parseInt(endDate)
                            && Integer.parseInt(weekArray[1]) >= Integer.parseInt(startDate)
                            && Integer.parseInt(weekArray[1]) <= Integer.parseInt(endDate)){
                        Hdfs.addInputPath(path, fs, pathArray, prefix);
                    }
                }

            } else {
                FileStatus[] listStatus = fs.listStatus(path);
                for (FileStatus file : listStatus) {
                    getPathList(fs, file.getPath(), pathArray, startDate, endDate, prefix);
                }
            }
        }
    }

}
