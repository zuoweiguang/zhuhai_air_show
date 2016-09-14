package com.navinfo.mapspotter.process.loader.kafka;

import com.navinfo.mapspotter.process.loader.kafka.base.BaseProducer;
import org.junit.Test;
import scala.Int;

import java.util.Map;

/**
 * Created by SongHuiXing on 2016/3/29.
 */
public class BaseProducerTest {

    @Test
    public void testSend() throws Exception {
        try(BaseProducer<String, String> producer = new BaseProducer<>(true, "Slave3.Hadoop:9092")) {

            for (int i = 0; i < 10; i++) {
                long time = System.currentTimeMillis();

                Map.Entry<Integer, Long> res = producer.send("my-test", "test", String.format("TestMessage:%d", time));

                System.out.println(String.format("Send to partition %d, offset %d", res.getKey(), res.getValue()));

                Thread.sleep(500);
            }
        }
    }
}