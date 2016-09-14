package com.navinfo.mapspotter.process.convert.vectortile;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.Redis;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * 读取Hbase内的pbf，存储到Redis
 * Created by SongHuiXing on 6/16 0016.
 */
public class Hbase2TafficRedis {
    private Logger logger = Logger.getLogger(Hbase2TafficRedis.class);

    private Connection m_hbaseConn = null;

    private Redis redis = null;

    private int redisDbNum = 0;

    public Hbase2TafficRedis(int redisDbNum){
        this.redisDbNum = redisDbNum;
    }

    public boolean prepare(){

        if(null == m_hbaseConn || m_hbaseConn.isClosed()){
            try {
                m_hbaseConn = ConnectionFactory.createConnection(getHBaseConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        redis = (Redis) DataSource.getDataSource(IOUtil.makeRedisParam("192.168.4.104", 6379, "navinfo1!rd"));

        if(null == m_hbaseConn || null == redis) {
            return false;
        } else {
            redis.selectDb(redisDbNum);
            //redis.clear();
            return true;
        }
    }

    public void shutdown(){
        if(null != m_hbaseConn) {
            try {
                if (!m_hbaseConn.isClosed())
                    m_hbaseConn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(null != redis){
            redis.close();
        }
    }

    private static Configuration getHBaseConfig(){
        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", "Master.Hadoop:2181,Slave3.Hadoop:2181,Slave1.Hadoop:2181");
        cfg.set("hbase.master", "Slave3.Hadoop");
        cfg.set("hbase.client.scanner.timeout.period", "60000");
        return cfg;
    }

    public void convert(String tablename, String colfamily, String column){

        long currenttime = 0;
        long count = 0, rowcount=0;

        try(Table table = m_hbaseConn.getTable(TableName.valueOf(tablename))){
            Scan scan = new Scan();
            if(column.equals("all")){
                scan.addFamily(Bytes.toBytes(colfamily));
            }else {
                scan.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(column));
            }
            scan.setBatch(1000);
            scan.setMaxVersions(1);

            if(redis.transaction() != 0)
                return;

            ResultScanner scanner = table.getScanner(scan);
            for (Result rs : scanner){
                String rowkey = Bytes.toString(rs.getRow());

                for (Cell cell : rs.rawCells()){
                    byte[] value = CellUtil.cloneValue(cell);

                    if(++rowcount % 1000 == 0){
                        logger.info("Has scan : " + rowcount);
                    }

                    if(value.length < 2)
                        continue;

                    if(++count % 5000 == 0){
                        currenttime++;
                        logger.info("HasInsert : " + count*currenttime);
                        count=0;
                    }

                    String col = Bytes.toString(CellUtil.cloneQualifier(cell));

                    redis.pipeSet(col+ "_" + rowkey, value);
                }
            }

            redis.commit();
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        logger.info("Convert traffic 2 redis complete.");
    }

    public static void main(String[] args){
//        String table = args[0];
//        String colfamily = args[1];
//        String column = args[2];
//        int redisDbNum = Integer.parseInt(args[3]);

        //java -cp
        Hbase2TafficRedis hbase2Redis = new Hbase2TafficRedis(2);

        if(hbase2Redis.prepare()){
            hbase2Redis.convert("Traffic_Vectortile", "Vectortile", "traffic");
//            hbase2Redis.convert(table, colfamily, column);
        }

        hbase2Redis.shutdown();
    }
}
