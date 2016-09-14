package com.navinfo.mapspotter.process.loader.rabbitmq;

/**
 * Created by SongHuiXing on 6/21 0021.
 */
public class JsonMessageConsumer {
    protected byte[] lock = new byte[0];

    public boolean consumer(String json){
        synchronized (lock){

        }

        return true;
    }
}
