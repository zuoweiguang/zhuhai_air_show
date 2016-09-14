package com.navinfo.mapspotter.process.loader.mina;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by SongHuiXing on 2016/3/30.
 */
public class SocketHDFSHandler extends IoHandlerAdapter {

    private FileSystem fileSystem = null;

    private SocketClient client = null;

    public boolean open(Configuration cfg, String host, int port){
        try {
            fileSystem = FileSystem.get(cfg);
        } catch (Exception e){
            return false;
        }

        client = new SocketClient(host, port);

        return client.start(this);
    }

    public long SendFile(Path file){

        long totalCount = 0l;

        try {
            if(null == fileSystem || !fileSystem.exists(file))
                return totalCount;

            FSDataInputStream fsIn = fileSystem.open(file);

            InputStreamReader input = new InputStreamReader(fsIn);

            BufferedReader reader = new BufferedReader(input);

            String lineTxt = null;
            while (null != (lineTxt = reader.readLine())) {
                if(client.send(lineTxt)) {
                    totalCount++;
                }
            }

            reader.close();
            fsIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalCount;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
    }

    public void close() throws Exception {
        if(null != fileSystem){
            fileSystem.close();
        }
    }
}
