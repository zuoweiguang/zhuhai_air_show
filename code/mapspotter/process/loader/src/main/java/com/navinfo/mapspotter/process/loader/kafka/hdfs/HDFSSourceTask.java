package com.navinfo.mapspotter.process.loader.kafka.hdfs;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by SongHuiXing on 2016/3/15.
 */
public class HDFSSourceTask extends SourceTask {
    public final static String Target_File = "TARGET";

    private String target_file = "";

    private String topic = "";

    private FileSystem fileSystem = null;

    private Map<String, Object> offset = new HashMap<>();

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        if(!props.containsKey(Target_File)){
            throw new ConnectException(Target_File);
        }

        target_file = props.get(Target_File);

        if(!props.containsKey(HDFSSourceConnector.Topic)){
            throw new ConnectException(HDFSSourceConnector.Topic);
        }

        topic = props.get(HDFSSourceConnector.Topic);

        loadOffset();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        try {
            Path filePath = new Path(target_file);

            if(!fileSystem.exists(filePath))
                return null;

            ArrayList<SourceRecord> records = new ArrayList<>();

            FSDataInputStream fsIn = fileSystem.open(filePath);

            InputStreamReader input = new InputStreamReader(fsIn);

            BufferedReader reader = new BufferedReader(input);

            Long streamOffset = 0l;
            if(offset.containsKey("position")) {
                streamOffset = (Long) offset.get("position");
            }

            String lineTxt = null;
            Long pos = 0l;
            while (input.ready() &&
                    null != (lineTxt = reader.readLine())) {
                if(pos < streamOffset){
                    pos++;
                    continue;
                }

                Map sourcePartition = Collections.singletonMap("filename", target_file);
                Map sourceOffset = Collections.singletonMap("position", pos);

                records.add(new SourceRecord(sourcePartition, sourceOffset,
                                            topic, Schema.STRING_SCHEMA, lineTxt));

                pos++;
            }

            reader.close();
            fsIn.close();

            return records;

        } catch (IOException e) {
            // Underlying stream was killed, probably as a result of calling stop.
            // Allow to return null, and driving thread will handle any shutdown if necessary.
        }

        return null;
    }

    @Override
    public void stop() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOffset(){
        Map sourcePartition = Collections.singletonMap("filename", target_file);
        offset.putAll(context.offsetStorageReader().offset(sourcePartition));
    }
}
