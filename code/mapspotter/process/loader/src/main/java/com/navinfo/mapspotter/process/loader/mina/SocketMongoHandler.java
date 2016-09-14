package com.navinfo.mapspotter.process.loader.mina;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.io.IOException;
import java.util.Map;

/**
 * NIO socket的消息处理类
 * 写入消息到Mongo
 * Created by SongHuiXing on 2016/3/30.
 */
public class SocketMongoHandler extends IoHandlerAdapter {

    private MongoDB mongoDB = null;

    private final String mongo_host;
    private final int mongo_port;
    private final String mongo_db;
    private final String mongo_collection;

    private JsonUtil jsonUtil = JsonUtil.getInstance();

    public SocketMongoHandler(String host, int port, String db, String collection){
        mongo_host = host;
        mongo_port = port;
        mongo_db = db;
        mongo_collection = collection;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        mongoDB = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams(mongo_host,
                                                                              mongo_port,
                                                                              mongo_db));
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if(null != mongoDB){
            mongoDB.close();
            mongoDB = null;
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {


    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Map<String, Object> object = null;

        try {
            object = jsonUtil.readMap(message.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(null != object) {
            mongoDB.insert(mongo_collection, object);
        }
    }
}
