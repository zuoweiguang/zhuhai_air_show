package com.navinfo.mapspotter.process.loader.kafka.mongo;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.loader.kafka.base.BaseConsumer;
import com.navinfo.mapspotter.process.loader.kafka.base.ConsumeMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/3/29.
 */
public class MongoConsumer {
    private MongoDB mongoDB = null;

    private final String kafkaServer;
    private final String consumeGroup;

    public MongoConsumer(String host, int port, String db,
                         String kafkaServer, String consumeGrp){
        mongoDB = (MongoDB)DataSource.getDataSource(IOUtil.makeMongoDBParams(host, port, db));

        this.kafkaServer = kafkaServer;
        this.consumeGroup = consumeGrp;
    }

    public long consume(String topic){

        MongoMessageWriter messageConsumer = new MongoMessageWriter(mongoDB);

        try(BaseConsumer<String, String> consumer = new BaseConsumer<>(kafkaServer, consumeGroup, true)){
            List<String> topics = new ArrayList<>();

            topics.add(topic);

            return consumer.consume(topics, messageConsumer);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}

class MongoMessageWriter implements ConsumeMessage<String, String>{

    private final MongoDB mongoDB;

    private JsonUtil jsonUtil = JsonUtil.getInstance();

    public MongoMessageWriter(MongoDB db){
        mongoDB = db;
    }

    @Override
    public boolean interestWith(String topic) {
        return true;
    }

    @Override
    public boolean dealwithMessage(String topic, String collection, String msg) {
        Map<String, Object> object;
        try {
            object = jsonUtil.readMap(msg);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if(!mongoDB.create(collection))
            return false;

        return 0 == mongoDB.insert(collection, object);
    }
}
