package com.navinfo.mapspotter.process.loader.kafka.base;

import kafka.admin.AdminUtils;
import kafka.api.TopicMetadata;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

import java.util.ArrayList;
import java.util.List;
import scala.collection.JavaConversions;

import java.util.Map;
import java.util.Properties;

/**
 * Kafka服务管理器
 * Created by SongHuiXing on 2016/3/3.
 */
public class KafkaService {

    private String zookepeerServer = "";

    public KafkaService(String zkServer){
        zookepeerServer = zkServer;
    }

    public boolean createTopic(String topic, int partitions, int replication) throws Exception {
        try {
            ZkUtils zkUtils = getZkUtils();

            // add per-topic configurations settings here
            Properties topicConfig = new Properties();

            AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig);

            zkUtils.close();

        } catch (Exception e){
            throw e;
        }

        return true;
    }

    public boolean isTopicExist(String topic) throws Exception {
        boolean isExist;

        try {
            ZkUtils zkUtils = getZkUtils();

            isExist = AdminUtils.topicExists(zkUtils, topic);

            zkUtils.close();

        } catch (Exception e){
            throw e;
        }

        return isExist;
    }

    public void deleteTopic(String topic){
        try {
            ZkUtils zkUtils = getZkUtils();

            AdminUtils.deleteTopic(zkUtils, topic);

            zkUtils.close();

        } catch(Exception e){
        }
    }

    public List<String> queryTopics(boolean containDeleted){
        List<String> topics = new ArrayList<>();

        try {
            ZkUtils zkUtils = getZkUtils();

            scala.collection.Map<String, Properties> topicProps = AdminUtils.fetchAllTopicConfigs(zkUtils);

            Map<String, Properties> topicWithProps = JavaConversions.mapAsJavaMap(topicProps);
            for (Map.Entry<String, Properties> topic : topicWithProps.entrySet()){
                String topicName = topic.getKey();

                TopicMetadata metadata = AdminUtils.fetchTopicMetadataFromZk(topicName, zkUtils);

                Properties topicProp = topic.getValue();

                //String status = topicProp.getProperty("");

                topics.add(topicName);
            }

        } catch(Exception e){
        }

        return topics;
    }

    public int getTopicPartitionCount(String topic){
        try {
            ZkUtils zkUtils = getZkUtils();

            TopicMetadata metadata = AdminUtils.fetchTopicMetadataFromZk(topic, zkUtils);

            return metadata.partitionsMetadata().size();

        } catch(Exception e){
        }

        return 0;
    }

    private ZkUtils getZkUtils(){
        ZkUtils zkUtils = null;

        int sessionTimeoutMs = 10 * 1000;
        int connectionTimeoutMs = 8 * 1000;

        try {
            // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
            // createTopic() will only seem to work (it will return without error).  The topic will exist in
            // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
            // topic.
            ZkClient zkClient = new ZkClient(zookepeerServer,
                    sessionTimeoutMs,
                    connectionTimeoutMs,
                    ZKStringSerializer$.MODULE$);

            ZkConnection zkConnection = new ZkConnection(zookepeerServer);

            zkUtils = new ZkUtils(zkClient, zkConnection, false);

        } catch (Exception e){
            throw e;
        }

        return zkUtils;
    }
}
