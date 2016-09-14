package com.navinfo.mapspotter.foundation.io.oracle;

import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SongHuiXing on 2015/12/29.
 */
public class OracleDatabase extends DataBase {

    public OracleDatabase() {
        super();
    }

    private OracleDataSource oraDataSource = null;

    /**
     * 打开数据库连接
     *
     * @param connProperty 数据库连接属性
     * @return <code>0</code>打开连接成功;
     *         <code>-1</code>打开连接失败.
     */
    @Override
    public int open(ConnectParams connProperty) {
        try{
            oraDataSource = new OracleConnectionPoolDataSource();

            String url = getConnectionString(connProperty);
            oraDataSource.setURL(url);
        } catch (SQLException e) {
            e.printStackTrace();
            return e.getErrorCode();
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
    public DBConnection getConnection(String user, String password) {
        if(null == oraDataSource){
            return null;
        }

        try {
            Connection sqlConn = oraDataSource.getConnection(user, password);
            return new OracleConnection(sqlConn);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭数据库连接
     *
     * @return
     */
    @Override
    public int close() {
        if(null != oraDataSource){
            try {
                oraDataSource.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return e.getErrorCode();
            }
        }

        return 0;
    }

    /**
     * 通过连接属性获取连接字符串
     * @param properties 连接属性
     * @return
     */
    private static String getConnectionString(ConnectParams properties){
        return String.format("jdbc:oracle:thin:@%1$s:%2$d:%3$s",
                properties.getHost(),
                properties.getPort(),
                properties.getDb());
    }
}
