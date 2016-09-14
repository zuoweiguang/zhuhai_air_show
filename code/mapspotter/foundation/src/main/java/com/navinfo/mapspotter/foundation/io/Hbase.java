package com.navinfo.mapspotter.foundation.io;

import java.io.IOException;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * hbase database
 *
 * @author huanghai
 */
public class Hbase extends DataSource {
    private static final Logger logger = Logger.getLogger(Hbase.class);

    private Configuration conf = null;
    private Connection connection = null;
    private Table table = null;

    protected Hbase() {
        super();
    }

    @Override
    protected int open(DataSourceParams params) {
        try {
            conf = HBaseConfiguration.create();
            if (!StringUtil.isEmpty(params.getHost())) {
                logger.info("zkHbaseHost -> " + params.getHost());
                conf.set("hbase.zookeeper.quorum", params.getHost());
            }
            connection = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }
        return 0;
    }

    @Override
    public void close() {
        IOUtil.closeStream(table);
        table = null;
        IOUtil.closeStream(connection);
        connection = null;
    }

    /**
     * 获取table实例
     *
     * @param tablename 表名
     * @throws IOException
     */
    public Table getTable(String tablename) {
        try {
            return connection.getTable(TableName.valueOf(tablename));
        } catch (Exception e) {
            logger.error(e);
            return  null;
        }
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
    public HbaseCursor scanTable(String tablename, int cacheSize, boolean cacheFlag) throws IOException {
        Table table = getTable(tablename);
        Scan scan = new Scan();
        scan.setCaching(cacheSize);
        scan.setCacheBlocks(cacheFlag);
        ResultScanner rs = table.getScanner(scan);
        return new HbaseCursor(table, rs);
    }

    /**
     * 打开table并持有变量，方便接下来的查询，不会带来忘记close的问题
     *
     * @param tablename 表名
     * @return 0：成功；-1：失败
     */
    public int openTable(String tablename) {
        IOUtil.closeStream(table);
        table = getTable(tablename);
        return (table == null) ? -1 : 0;
    }

    /**
     * rowkey查询
     *
     * @param rowkey
     * @return
     */
    public HbaseCursor query(String rowkey) {
        return query(table, rowkey);
    }

    public static HbaseCursor query(Table table, String rowkey) {
        try {
            Get get = new Get(Bytes.toBytes(rowkey));
            Result result = table.get(get);
            return new HbaseCursor(result);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public HbaseCursor query(String rowkey, String family) {
        return query(table, rowkey, family);
    }

    public static HbaseCursor query(Table table, String rowkey, String family) {
        try {
            Get get = new Get(Bytes.toBytes(rowkey));
            get.addFamily(Bytes.toBytes(family));
            Result result = table.get(get);
            return new HbaseCursor(result);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public byte[] query(String rowkey, String family, String qualifier) {
        return query(table, rowkey, family, qualifier);
    }

    public static byte[] query(Table table, String rowkey, String family, String qualifier) {
        try {
            Get get = new Get(Bytes.toBytes(rowkey));
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            Result result = table.get(get);
            return result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier));
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public HbaseCursor query(String rowkey, String[] columns) {
        return query(table, rowkey, columns);
    }

    public static HbaseCursor query(Table table, String rowkey, String[] columns) {
        try {
            Get get = new Get(Bytes.toBytes(rowkey));
            for (int i = 0; i < columns.length - 1; i += 2) {
                get.addColumn(Bytes.toBytes(columns[i]), Bytes.toBytes(columns[i + 1]));
            }
            Result result = table.get(get);
            return new HbaseCursor(result);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * 创建实例自Configuration
     *
     * @param conf HBaseConfiguration
     * @return Instance of Hbase
     */
    public static Hbase createWithConfiguration(Configuration conf) {
        try {
            //System.setProperty("hadoop.home.dir", "E:\\cl\\hadoop\\hadoop-2.6.0");
            Hbase hbase = new Hbase();
            hbase.conf = conf;
            hbase.connection = ConnectionFactory.createConnection(hbase.conf);
            return hbase;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    public void insertData(String rowkey , String family , String qualify , byte[] data){
        insertData(table , rowkey , family , qualify , data);
    }

    public void insertData(Table htable , String rowkey , String family , String qualifier , byte[] data){
        try{
            Put put = new Put(rowkey.getBytes());
            put.addColumn(family.getBytes() , qualifier.getBytes() , data);
            htable.put(put);}catch(Exception e){
            logger.error(e);
        }
    }
}
