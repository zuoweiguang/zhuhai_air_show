package com.navinfo.mapspotter.process.loader.kafka;

import com.navinfo.mapspotter.process.loader.kafka.base.KafkaService;
import com.navinfo.mapspotter.process.loader.kafka.hdfs.HDFSProducer;
import com.navinfo.mapspotter.process.loader.kafka.mongo.MongoConsumer;

import java.util.Map;

/**
 * 自动将HDFS上的json文件上传到Mongo库
 * Created by SongHuiXing on 2016/4/21.
 */
public class SendHdfs2Mongo {
    private final String kafka;
    private final String topic;

    private MongoConsumer mongoConsumer;

    private HDFSProducer hdfsProducer;

    public SendHdfs2Mongo(String kafkaServer, String topic,
                          String mongoHost, int mongoPort, String mongoDb){
        this.kafka = kafkaServer;
        this.topic = topic;

        mongoConsumer = new MongoConsumer(mongoHost, mongoPort, mongoDb,
                                        kafkaServer, "MongoConsumeGroup");
    }

    private boolean checkTopic(){
        KafkaService service = new KafkaService(kafka);

        try {
            if(service.isTopicExist(this.topic))
                return true;

            service.createTopic(topic, 1, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void send(String dfsPath, Map<String, String> file2Collection){
        hdfsProducer = new HDFSProducer(dfsPath, kafka);

        hdfsProducer.send(this.topic, file2Collection);

        mongoConsumer.consume(this.topic);
    }
}
