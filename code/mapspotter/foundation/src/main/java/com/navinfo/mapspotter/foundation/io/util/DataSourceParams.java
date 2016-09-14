package com.navinfo.mapspotter.foundation.io.util;

/**
 * 连接数据源参数类
 */
public class DataSourceParams {

    public enum SourceType{
        Oracle,
        MySql,
        Sqlite,
        MongoDB,
        Redis,
        HBase,
        HDFS,
        Solr,
        Hive,
        PostGIS,
    }

    /**
     * 数据源类型
     */
    private SourceType type;

    /**
     * 主机地址
     */
    private String   host = null;

    /**
     * 端口号
     */
    private int      port = -1;

    /**
     * 数据库实例名
     */
    private String   db = null;

    /**
     * 用户名
     */
    private String   user = null;

    /**
     * 密码
     */
    private String   password = null;

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
