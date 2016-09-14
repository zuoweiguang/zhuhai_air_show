package com.navinfo.mapspotter.process.loader.mina;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by SongHuiXing on 2016/3/30.
 */
public class SocketServer {
    private final int listen_port;

    public SocketServer(int port){
        listen_port = port;
    }

    public boolean start(IoHandler handler) {
        IoAcceptor acceptor = new NioSocketAcceptor();

        acceptor.getSessionConfig().setBothIdleTime(10);
        acceptor.getSessionConfig().setReadBufferSize(2048);

        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec",
                                            new ProtocolCodecFilter(
                                                    new TextLineCodecFactory(
                                                            Charset.forName("UTF-8")))
                                            );

        acceptor.setHandler(handler);

        InetSocketAddress address = new InetSocketAddress(listen_port);

        boolean res = false;

        try {
            acceptor.bind(address);

            acceptor.bind();

            res = true;
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            acceptor.unbind();
            acceptor.unbind(address);
        }

        return res;
    }
}
