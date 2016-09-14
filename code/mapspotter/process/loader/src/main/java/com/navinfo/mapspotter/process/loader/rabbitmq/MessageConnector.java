package com.navinfo.mapspotter.process.loader.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * RabbitMQ 消息监听类
 * 启动多个通道进行消费监听
 * Created by SongHuiXing on 6/21 0021.
 */
public class MessageConnector {
    private String mqExchange = null;

    private ArrayList<Connection> mqConns = new ArrayList<>();
    private ArrayList<Channel> openListeners = new ArrayList<>();

    private ConnectionFactory factory = new ConnectionFactory();

    public MessageConnector(String uri, String exchange) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        factory.setUri(uri);
        mqExchange = exchange;
    }

    private Connection getNewConnection() throws IOException, TimeoutException {
        Connection conn = factory.newConnection();
        if(null != conn)
            mqConns.add(conn);

        return conn;
    }

    public boolean openListenning(String queue, JsonMessageConsumer msgConsumer) {
        try {
            Connection conn = getNewConnection();

            for (int i = 0; i < 4; i++) {
                Channel channel = conn.createChannel();

                channel.basicConsume(queue, false, new MqMsgConsumer(channel, msgConsumer));

                openListeners.add(channel);
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void close() throws IOException {
        for (Channel channel : openListeners){
            try {
                channel.close();
            } catch (Exception e) {
            }
        }

        openListeners.clear();

        for (Connection conn : mqConns){
            conn.close();
        }
        mqConns.clear();
    }

    public boolean send(String queue, List<String> messages){
        try {
            Connection conn = getNewConnection();

            Channel channel = conn.createChannel();

            channel.queueDeclare(queue, true, false, false,
                                new HashMap<String, Object>() {});

            for (String msg : messages){
                channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            }

            channel.close();

            mqConns.remove(conn);
            conn.close();

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}

class MqMsgConsumer implements Consumer{

    private final Channel _channel;
    private final JsonMessageConsumer _consumer;

    private volatile String _consumerTag;

    public MqMsgConsumer(Channel channel,
                         JsonMessageConsumer msgConsumer) {
        _channel = channel;
        _consumer = msgConsumer;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        this._consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException e) {

    }

    @Override
    public void handleRecoverOk(String consumerTag) {

    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        String routingKey = envelope.getRoutingKey();
        String contentType = properties.getContentType();
        long deliveryTag = envelope.getDeliveryTag();

        String jsonMsg = new String(body);

        if(_consumer.consumer(jsonMsg)) {
            _channel.basicAck(deliveryTag, false);
        }else {
            _channel.basicReject(deliveryTag, true);
        }
    }
}
