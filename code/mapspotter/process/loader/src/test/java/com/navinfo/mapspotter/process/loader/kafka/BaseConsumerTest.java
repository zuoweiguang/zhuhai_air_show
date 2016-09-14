package com.navinfo.mapspotter.process.loader.kafka;

import com.navinfo.mapspotter.process.loader.kafka.base.BaseConsumer;
import com.navinfo.mapspotter.process.loader.kafka.base.BaseProducer;
import com.navinfo.mapspotter.process.loader.kafka.base.ConsumeMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/3/29.
 */
public class BaseConsumerTest {

    @Test
    public void testConsume() throws Exception {

        String topic = "my-test";

        try(BaseProducer<String, String> producer = new BaseProducer<>(true, "Slave3.Hadoop:9092")) {

            for (int i = 0; i < 10; i++) {
                long time = System.currentTimeMillis();

                Map.Entry<Integer, Long> res = producer.send(topic, "test", String.format("TestMessage:%d", time));

                System.out.println(String.format("Send to partition %d, offset %d", res.getKey(), res.getValue()));
            }
        }

        MessageConsumer messageConsumer = new MessageConsumer();

        try(BaseConsumer<String, String> consumer = new BaseConsumer<>("Slave3.Hadoop:9092", "g2", true)){
            List<String> topics = new ArrayList<>();

            topics.add(topic);

            consumer.consume(topics, messageConsumer);
        }
    }
}

class MessageConsumer implements ConsumeMessage<String, String> {

    private int count = 0;

    @Override
    public boolean interestWith(String topic) {
        return true;
    }

    @Override
    public boolean dealwithMessage(String topic, String key, String msg) {
        count++;

        System.out.println(count);
        return true;
    }
}