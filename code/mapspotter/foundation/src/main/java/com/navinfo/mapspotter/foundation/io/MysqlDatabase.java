package com.navinfo.mapspotter.foundation.io;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MYSQL数据库访问入口
 * Created by SongHuiXing on 2015/12/30.
 */
public class MysqlDatabase extends SqlDatabase {
    private static final Logger logger = Logger.getLogger(MysqlDatabase.class);

    private MysqlDataSource mysqlDs = null;

    protected MysqlDatabase() {
        super();
    }

    /**
     * 打开数据库
     *
     * @param params 数据库属性
     * @return <code>0</code>打开成功;
     * <code>-1</code>打开失败.
     */
    @Override
    protected int open(DataSourceParams params) {
        try {
            mysqlDs = new MysqlDataSource();
            mysqlDs.setServerName(params.getHost());
            mysqlDs.setPort(params.getPort());
            mysqlDs.setDatabaseName(params.getDb());
            mysqlDs.setUser(params.getUser());
            mysqlDs.setPassword(params.getPassword());

            mysqlDs.setConnectTimeout(2000);
            mysqlDs.setLoginTimeout(2000);

            sqlConnection = mysqlDs.getConnection();
        } catch (SQLException e) {
            logger.error(e);
        }

        return 0;
    }

    /**
     * 关闭数据库
     *
     * @return
     */
    @Override
    public void close() {
        super.close();

        mysqlDs = null;
    }
}
