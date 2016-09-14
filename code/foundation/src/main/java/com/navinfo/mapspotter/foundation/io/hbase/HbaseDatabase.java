package com.navinfo.mapspotter.foundation.io.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.log4j.Logger;

import com.navinfo.mapspotter.foundation.util.PropertiesUtil;

/**
 * hbase database
 *
 * @author huanghai
 */
public class HbaseDatabase {
    private static final Logger logger = Logger.getLogger(HbaseDatabase.class);
    private Configuration conf;
    private Connection connection;

    public HbaseDatabase() throws IOException {
        getConnection();
    }

    public HbaseDatabase(String zkHbaseHost) throws IOException {
        getConnection(zkHbaseHost);
    }

    /**
     * 获取connection
     *
     * @return
     * @throws IOException
     */
    public Connection getConnection() throws IOException {
        conf = HBaseConfiguration.create();
        String zkHbaseHost = PropertiesUtil.getValue("ZK_HBASE_HOST");
        logger.info("zkHbaseHost -> " + zkHbaseHost);
        conf.set("hbase.zookeeper.quorum", zkHbaseHost);
        connection = ConnectionFactory.createConnection(conf);
        return connection;
    }

    /**
     * 获取connection
     *
     * @param zkHbaseHost
     * @return
     * @throws IOException
     */
    public Connection getConnection(String zkHbaseHost) throws IOException {
        conf = HBaseConfiguration.create();
        logger.info("zkHbaseHost -> " + zkHbaseHost);
        conf.set("hbase.zookeeper.quorum", zkHbaseHost);
        connection = ConnectionFactory.createConnection(conf);
        return connection;
    }

    /**
     * 获取connection
     *
     * @return
     */
    public Connection getConn() {
        return connection;
    }

    /**
     * 获取configuration
     *
     * @return
     */
    public Configuration getConfiguration() {
        return conf;
    }

    /**
     * close connection
     *
     * @return
     */
    public int closeConnetcion() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                logger.error("closeConnetcion() -> " + e);
                return -1;
            }
        }
        return 0;
    }

    /**
     * scan表,并设置cachesize和是否缓存数据
     *
     * @param tablename 表名
     * @param cacheSize 批获取条数
     * @param cacheFlag 是否缓存数据
     * @return
     * @throws IOException
     */
    public ResultScanner getResultScanner(String tablename, int cacheSize, boolean cacheFlag) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tablename));
        Scan scan = new Scan();
        scan.setCaching(cacheSize);
        scan.setCacheBlocks(cacheFlag);
        return table.getScanner(scan);
    }

    /**
     * 获取table实例
     *
     * @param tablename 表名
     * @throws IOException
     */
    public Table getTable(String tablename) throws IOException {
        return connection.getTable(TableName.valueOf(tablename));
    }

}
