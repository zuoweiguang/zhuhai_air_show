package com.navinfo.mapspotter.process.loader.kafka.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/3/15.
 */
public class HDFSSourceConnector extends SourceConnector {
    public final static String File_Path = "FilePath";
    public final static String Topic = "Topic";

    private String target_file_path = "";
    private String topic = "";

    private ArrayList<String> files = new ArrayList<>();

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        if(!props.containsKey(File_Path)){
            throw new ConnectException(File_Path);
        }

        if(!props.containsKey(Topic)){
            throw new ConnectException(Topic);
        }

        topic = props.get(Topic);

        try {
            FileSystem fs = FileSystem.get(new Configuration());

            Path path = new Path(target_file_path);

            if(fs.isDirectory(path)){
                RemoteIterator<LocatedFileStatus> childFiles = fs.listFiles(path, true);
                while (childFiles.hasNext()){
                    LocatedFileStatus fileStatus = childFiles.next();

                    if(fileStatus.isFile()){
                        files.add(fileStatus.getPath().toString());
                    }
                }
            } else {
                files.add(target_file_path);
            }

            fs.close();
        } catch (IOException e) {
            throw new ConnectException(e.getMessage());
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return HDFSSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        int taskCount = files.size();

        ArrayList<Map<String, String>> configs = new ArrayList<>();

        for (int j = 0; j < taskCount; j++) {
            Map<String, String> taskCfg = new HashMap<>();
            taskCfg.put(HDFSSourceTask.Target_File, files.get(j));
            taskCfg.put(HDFSSourceConnector.Topic, topic);
            configs.add(taskCfg);
        }

        return configs;
    }

    @Override
    public void stop() {

    }
}
