package com.navinfo.mapspotter.process.loader.kafka.base;

import java.util.HashMap;

/**
 * Created by SongHuiXing on 2016/3/30.
 */
public class KafkaSendResult {
    public KafkaSendResult(long count){
        SendCount = count;
    }

    private HashMap<Integer, Long> partitionPos = new HashMap<>();
    public HashMap<Integer, Long> getPartitionInfo(){
        return partitionPos;
    }
    public void setPartitionInfo(HashMap<Integer, Long> partitionPos){
        this.partitionPos = partitionPos;
    }

    public long SendCount;
}
