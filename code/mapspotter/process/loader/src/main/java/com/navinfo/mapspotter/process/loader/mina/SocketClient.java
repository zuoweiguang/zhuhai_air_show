package com.navinfo.mapspotter.process.loader.mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by SongHuiXing on 2016/3/30.
 */
public class SocketClient {

    private final String server_host;
    private final int server_port;

    private NioSocketConnector connector = null;
    private ConnectFuture connectFuture = null;

    public SocketClient(String host, int port){
        server_host = host;
        server_port = port;
    }

    public boolean start(IoHandler handler){
        // 创建客户端连接器.
        connector = new NioSocketConnector();
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

        // 设置连接超时检查时间
        connector.setConnectTimeoutCheckInterval(30);
        connector.setHandler(handler);

        // 建立连接
        connectFuture = connector.connect(new InetSocketAddress(server_host, server_port));

        // 等待连接创建完成
        connectFuture.awaitUninterruptibly();

        return connectFuture.isConnected();
    }

    public boolean send(String message){
        if(null == connectFuture)
            return false;

        WriteFuture wf = connectFuture.getSession().write(message);

        try {
            wf = wf.await();
        } catch (InterruptedException e) {
            e.printStackTrace();

            return false;
        }

        return wf.isDone();
    }

    public void close(){
        // 等待连接断开
        connectFuture.getSession().getCloseFuture().awaitUninterruptibly();

        // 释放连接
        connector.dispose();
    }
}
