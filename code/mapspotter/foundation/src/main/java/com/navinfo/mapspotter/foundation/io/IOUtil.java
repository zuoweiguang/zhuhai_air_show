package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * IO模块的工具类
 *
 * Created by gaojian on 2016/1/6.
 */
public class IOUtil {
    private static final Logger logger = Logger.getLogger(IOUtil.class);

    public static void closeStream(Closeable... args) {
        for (Closeable arg : args) {
            if (arg != null) {
                try {
                    arg.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * 读取配置文件，生成DataSource
     *
     * @param key 配置文件中的数据源参数字段
     * @return 数据源连接
     */
    public static DataSource getDataSourceFromProperties(String key) {
        return DataSource.getDataSource(
                readProperties(key)
        );
    }

    /**
     * 从配置文件中读取数据源连接参数
     *
     * @param key 配置文件中的数据源参数字段
     * @return instance of DataSourceParams
     */
    public static DataSourceParams readProperties(String key) {
        DataSourceParams.SourceType sourceType = DataSourceParams.SourceType.valueOf(
                PropertiesUtil.getValue(key)
        );
        switch (sourceType) {
            case Oracle:
                return makeOracleParams(
                        PropertiesUtil.getValue(key + ".host"),
                        Integer.parseInt(PropertiesUtil.getValue(key + ".port")),
                        PropertiesUtil.getValue(key + ".db"),
                        PropertiesUtil.getValue(key + ".user"),
                        PropertiesUtil.getValue(key + ".password")
                );
            case MySql:
                return makeMysqlParams(
                        PropertiesUtil.getValue(key + ".host"),
                        Integer.parseInt(PropertiesUtil.getValue(key + ".port")),
                        PropertiesUtil.getValue(key + ".db"),
                        PropertiesUtil.getValue(key + ".user"),
                        PropertiesUtil.getValue(key + ".password")
                );
            case MongoDB:
                return makeMongoDBParams(
                        PropertiesUtil.getValue(key + ".host"),
                        Integer.parseInt(PropertiesUtil.getValue(key + ".port")),
                        PropertiesUtil.getValue(key + ".db"),
                        PropertiesUtil.getValue(key + ".user"),
                        PropertiesUtil.getValue(key + ".password")
                );
            case Redis:
                return makeRedisParam(
                        PropertiesUtil.getValue(key + ".host"),
                        Integer.parseInt(PropertiesUtil.getValue(key + ".port")),
                        PropertiesUtil.getValue(key + ".password")
                );
            case HBase:
                return makeHBaseParam(
                        PropertiesUtil.getValue(key + ".host")
                );
            case Solr:
                return makeSolrParam(
                        PropertiesUtil.getValue(key + ".host"),
                        PropertiesUtil.getValue(key + ".db")
                );
            case Hive:
                return makeHiveParam(
                        PropertiesUtil.getValue(key + ".host"),
                        Integer.parseInt(PropertiesUtil.getValue(key + ".port")),
                        PropertiesUtil.getValue(key + ".db"),
                        PropertiesUtil.getValue(key + ".user"),
                        PropertiesUtil.getValue(key + ".password")
                );
            case HDFS:
                return makeHdfsParam();
        }
        return null;
    }

    /**
     * 生成Oracle连接参数实例
     *
     * @param host     host
     * @param port     port
     * @param db       db
     * @param user     user
     * @param password password
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeOracleParams(String host, int port, String db, String user, String password) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.Oracle);
        params.setHost(host);
        params.setPort(port);
        params.setDb(db);
        params.setUser(user);
        params.setPassword(password);
        return params;
    }

    /**
     * 生成Mysql连接参数实例
     *
     * @param host     host
     * @param port     port
     * @param db       db
     * @param user     user
     * @param password password
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeMysqlParams(String host, int port, String db, String user, String password) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.MySql);
        params.setHost(host);
        params.setPort(port);
        params.setDb(db);
        params.setUser(user);
        params.setPassword(password);
        return params;
    }

    public static DataSourceParams makeMongoDBParams(String host, int port, String db) {
        return makeMongoDBParams(host, port, db, null, null);
    }

    public static DataSourceParams makeMongoDBParams(String host, int port, String db, String user, String password) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.MongoDB);
        params.setHost(host);
        params.setPort(port);
        params.setDb(db);
        params.setUser(user);
        params.setPassword(password);
        return params;
    }

    /**
     * 生成Redis连接参数
     *
     * @param host     host
     * @param port     端口
     * @param password 密码，默认无
     * @param db       db，默认0
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeRedisParam(String host, int port, String password, int db) {
        DataSourceParams params = makeRedisParam(host, port, password);
        params.setDb(String.valueOf(db));
        return params;
    }

    public static DataSourceParams makeRedisParam(String host, int port, String password) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.Redis);
        params.setHost(host);
        params.setPort(port);
        params.setPassword(password);
        return params;
    }

    public static DataSourceParams makeRedisParam(String host, int port) {
        return makeRedisParam(host, port, null);
    }

    /**
     * 生成HBase连接参数实例
     *
     * @param zkHost HBase的zookeeper地址字符串
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeHBaseParam(String zkHost) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.HBase);
        params.setHost(zkHost);
        return params;
    }

    /**
     * 生成Solr连接参数实例
     *
     * @param zkHost Solr的zookeeper地址字符串
     * @param db db
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeSolrParam(String zkHost, String db) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.Solr);
        params.setHost(zkHost);
        params.setDb(db);
        return params;
    }

    /**
     * 生成Hive连接参数实例
     *
     * @param host     host
     * @param port     port
     * @param db       db
     * @param user     user
     * @param password password
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeHiveParam(String host, int port, String db, String user, String password) {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.Hive);
        params.setHost(host);
        params.setPort(port);
        params.setDb(db);
        params.setUser(user);
        params.setPassword(password);
        return params;
    }

    /**
     * 生成HDFS连接参数实例
     *
     * @return instance of DataSourceParams
     */
    public static DataSourceParams makeHdfsParam() {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.HDFS);
        return params;
    }

    public static DataSourceParams makePostGISParam(String host, int port, String db, String user, String password){
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.PostGIS);
        params.setHost(host);
        params.setPort(port);
        params.setDb(db);
        params.setUser(user);
        params.setPassword(password);
        return params;
    }

    public static byte[] readBlob2Bytes(InputStream is){

        byte[] bytesValue = new byte[255];
        int totalLen = 0;

        try {
            byte[] buffer = new byte[255];
            int readLen;
            while ((readLen = is.read(buffer)) > 0){
                int newLen = totalLen + readLen;

                if(newLen > bytesValue.length){
                    byte[] newBytes = new byte[bytesValue.length*2];
                    System.arraycopy(bytesValue, 0, newBytes, 0, totalLen);
                    bytesValue = newBytes;
                }

                System.arraycopy(buffer, 0, bytesValue, totalLen, readLen);

                totalLen = newLen;
            }
        } catch (IOException e){
            System.out.print(e.getStackTrace());
        }

        if(0 == totalLen)
            return null;

        byte[] res = new byte[totalLen];

        System.arraycopy(bytesValue, 0, res, 0, totalLen);

        return res;
    }
}
