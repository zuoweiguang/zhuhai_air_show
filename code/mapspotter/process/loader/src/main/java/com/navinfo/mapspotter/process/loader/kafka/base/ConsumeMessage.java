package com.navinfo.mapspotter.process.loader.kafka.base;

/**
 * Created by SongHuiXing on 2016/3/29.
 */
public interface ConsumeMessage<K, V> {
    boolean interestWith(String topic);

    boolean dealwithMessage(String topic, K key, V msg);
}
