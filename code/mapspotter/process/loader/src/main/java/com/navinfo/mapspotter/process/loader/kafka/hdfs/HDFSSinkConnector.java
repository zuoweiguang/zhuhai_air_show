package com.navinfo.mapspotter.process.loader.kafka.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkConnector;

import java.io.IOException;
import java.util.*;

/**
 * Created by SongHuiXing on 2016/3/15.
 */
public class HDFSSinkConnector extends SinkConnector {
    public final static String Folder_Path = "Folder";
    public final static String TOPICS = "Topics";

    private String target_folder = "";
    private ArrayList<String> topics = new ArrayList<>();

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        if(!props.containsKey(Folder_Path)){
            throw new ConnectException(Folder_Path);
        }

        target_folder = props.get(Folder_Path);

        if(!props.containsKey(TOPICS)){
            throw new ConnectException(TOPICS);
        }

        topics.addAll(Arrays.asList(props.get(TOPICS).split(",")));

        try {
            FileSystem fs = FileSystem.get(new Configuration());

            Path folderPath = new Path(target_folder);

            if(fs.exists(folderPath)){
                throw new ConnectException("Exist target folder");
            }

            fs.mkdirs(folderPath);

            fs.close();
        } catch (IOException e) {
            throw new ConnectException(e.getMessage());
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return HDFSSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        int taskCount = topics.size();

        ArrayList<Map<String, String>> configs = new ArrayList<>();

        for (String topic : topics){
            HashMap<String, String> cfg = new HashMap<>();
            cfg.put(HDFSSinkTask.TOPIC, topic);
            cfg.put(HDFSSinkTask.TARGET_FILE, target_folder+"//"+topic);

            configs.add(cfg);
        }

        return configs;
    }

    @Override
    public void stop() {

    }
}
