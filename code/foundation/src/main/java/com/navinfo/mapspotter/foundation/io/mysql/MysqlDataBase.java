package com.navinfo.mapspotter.foundation.io.mysql;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * MYSQL数据库访问入口
 * Created by SongHuiXing on 2015/12/30.
 */
public class MysqlDataBase extends DataBase {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MysqlDataSource mysqlDs = null;

    private Map<MysqlConnection, Boolean> connections = new HashMap<>();

    private int maxPoolSize = 10;
    private int waitTime = 1000;

    public MysqlDataBase(){

    }

    public int getConnectionCount(){
        return connections.size();
    }

    /**
     * 打开数据库
     *
     * @param connProperty 数据库属性
     * @return <code>0</code>打开成功;
     * <code>-1</code>打开失败.
     */
    @Override
    public int open(ConnectParams connProperty) {
        try {
            mysqlDs = new MysqlDataSource();
            mysqlDs.setServerName(connProperty.getHost());
            mysqlDs.setPort(connProperty.getPort());
            mysqlDs.setDatabaseName(connProperty.getDb());

            mysqlDs.setConnectTimeout(2000);
            mysqlDs.setLoginTimeout(2000);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获取数据库连接
     *
     * @param user     用户名
     * @param password 密码
     * @return
     */
    @Override
    public synchronized DBConnection getConnection(String user, String password) {
        MysqlConnection mysqlconn = null;
        try {
            for (Map.Entry<MysqlConnection, Boolean> entry : connections.entrySet()) {
                if (entry.getValue()) {
                    mysqlconn = entry.getKey();
                    mysqlconn.changeUser(user, password);
                    connections.put(mysqlconn, false);
                    break;
                }
            }
            if (null == mysqlconn) {
                if (connections.size() < maxPoolSize) {
                    mysqlconn = getNewConnection(user, password);
                    connections.put(mysqlconn, false);
                } else {
                    wait(waitTime);
                    mysqlconn = (MysqlConnection) getConnection(user, password);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mysqlconn;
    }

    /**
     * 获取新的数据库连接
     * @param user
     * @param password
     * @return
     */
    private MysqlConnection getNewConnection(String user, String password){
        MysqlConnection mysqlConn = null;

        try {
            Connection conn = (Connection) mysqlDs.getConnection(user,password);
            mysqlConn = new MysqlConnection(conn, this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mysqlConn;
    }

    /**
     * 释放连接
     * @param mysqlConn
     */
    void releaseConnection(MysqlConnection mysqlConn){
        if(null == mysqlConn)
            return;

        try {
            Connection conn = mysqlConn.getSqlConnection();

            if(connections.containsKey(mysqlConn)) {
                if (conn.isClosed()) {
                    connections.remove(mysqlConn);
                } else {
                    if(!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                    connections.put(mysqlConn, true);
                }
            } else {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭数据库
     *
     * @return
     */
    @Override
    public int close() {
        mysqlDs = null;

        return 0;
    }
}
