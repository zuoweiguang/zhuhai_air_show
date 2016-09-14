package com.navinfo.mapspotter.process.loader.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.Scheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.navinfo.mapspotter.foundation.util.CommonPropertiesUtil;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.*;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

public class TypewriterTopo {

    private static final String KAFKA_SPOUT = "kafka-reader";
    private static final String FILTER_BOLT = "filter-bolt";

    private static final String MERCATOR_BOLT = "mercator-bolt";
    private static final String REDIS_BOLT = "redis-bolt";

    private static final String TRACK_BOLT = "track-bolt";
    private static final String HDFS_BOLT = "hdfs-bolt";


    public static void main(String[] args) {
        CommonPropertiesUtil util = new CommonPropertiesUtil(args[0]);

        /*====================================== KakfaSpout ======================================*/
        String zks = util.getStringValue("storm.zks");
        String topic = util.getStringValue("storm.topic");
        String zkRoot = util.getStringValue("storm.zkRoot");
        String id = util.getStringValue("storm.id");
        BrokerHosts brokerHosts = new ZkHosts(zks);
        SpoutConfig spoutConf = new SpoutConfig(brokerHosts, topic, zkRoot, id);

        String model = util.getStringValue("storm.model");

        Scheme messageScheme;

        if (model.equals("local")) {
            messageScheme = new MessageSchemeTest();
        }
        else{
            messageScheme = new MessageScheme();
        }

        spoutConf.scheme = new SchemeAsMultiScheme(messageScheme);
        String hdfs_url = util.getStringValue("storm.hdfs.url");
        int hdfs_writeLine = util.getIntValue("storm.hdfs.writeLine");
        int hdfs_fileSzie = util.getIntValue("storm.hdfs.fileSize");
        String hdfs_filePath = util.getStringValue("storm.hdfs.filePath");
        String hdfs_fileType = util.getStringValue("storm.hdfs.fileType");
        /*====================================== KakfaSpout ======================================*/

        String region_list = util.getStringValue("storm.filter.region.list");

        FilterBolt filterBolt = new FilterBolt(region_list);
//        MercatorBolt mercatorBolt = new MercatorBolt();

        int interval = util.getIntValue("storm.track.interval");
        TrackBolt trackBolt = new TrackBolt(interval);

//        String redis_host = util.getStringValue("storm.redis.host");
//        int redis_post = util.getIntValue("storm.redis.post");
//        String redis_auth = util.getStringValue("storm.redis.auth");
//        RedisBolt redisBolt = new RedisBolt(redis_host, redis_post, redis_auth);


        /*====================================== HDFS bolt ======================================*/
        SyncPolicy syncPolicy = new CountSyncPolicy(hdfs_writeLine);
        // rotate files when they reach 5MB
        FileRotationPolicy rotationPolicy =
                new FileSizeRotationPolicy(hdfs_fileSzie, FileSizeRotationPolicy.Units.MB);
        FileNameFormat fileNameFormat = new DefaultFileNameFormat()
                .withExtension(hdfs_fileType)
                .withPath(hdfs_filePath);

        RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("");

        HdfsBolt hdfsBolt = new HdfsBolt()
                .withFsUrl(hdfs_url)
                .withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);
        /*====================================== HDFS bolt ======================================*/


        String topo_name = util.getStringValue("storm.topology.name");
        int spoutNum = util.getIntValue("storm.spout.num");
        int boltFilterNum = util.getIntValue("storm.boltFilter.num");
        int boltMercatorNum = util.getIntValue("storm.boltMercator.num");
        int boltRedisNum = util.getIntValue("storm.boltRedis.num");
        int boltTrackNum = util.getIntValue("storm.boltTrack.num");
        int boltHdfsNum = util.getIntValue("storm.boltHdfs.num");
        int workerNum = util.getIntValue("storm.worker.num");

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(KAFKA_SPOUT, new KafkaSpout(spoutConf), spoutNum);
        builder.setBolt(FILTER_BOLT, filterBolt, boltFilterNum).shuffleGrouping(KAFKA_SPOUT);

        //builder.setBolt(MERCATOR_BOLT, mercatorBolt, boltMercatorNum).shuffleGrouping(FILTER_BOLT);
        //builder.setBolt(REDIS_BOLT, redisBolt, boltRedisNum).fieldsGrouping(MERCATOR_BOLT, new Fields("key"));

        builder.setBolt(TRACK_BOLT, trackBolt, boltTrackNum).fieldsGrouping(FILTER_BOLT, new Fields("userId"));
        builder.setBolt(HDFS_BOLT, hdfsBolt, boltHdfsNum).shuffleGrouping(TRACK_BOLT);

        Config conf = new Config();
        conf.setNumAckers(0);

        if (workerNum > 0) {
            conf.setNumWorkers(workerNum);
        }

        if (model.equals("local")) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(topo_name, conf, builder.createTopology());
        }

        if (model.equals("cluster")) {
            try {
                StormSubmitter.submitTopology(topo_name, conf, builder.createTopology());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                e.printStackTrace();
            } catch (AuthorizationException e) {
                e.printStackTrace();
            }
        }
    }
}
