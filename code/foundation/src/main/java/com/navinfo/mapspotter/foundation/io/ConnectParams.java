package com.navinfo.mapspotter.foundation.io;

/**
 * 数据库连接属性.
 */
public class ConnectParams {
    /**
     * 主机地址
     */
    private String   host;

    /**
     * 端口号
     */
    private int      port;

    /**
     * 数据库实例名
     */
    private String   db;

    /**
     * 用户名
     */
    private String   user;

    /**
     * 密码
     */
    private String   password;

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
