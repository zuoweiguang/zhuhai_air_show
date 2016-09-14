package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by SongHuiXing on 2015/12/29.
 */
public class OracleDatabase extends SqlDatabase {
    private static final Logger logger = Logger.getLogger(OracleDatabase.class);

    private OracleDataSource oraDataSource = null;

    protected OracleDatabase() {
        super();
    }

    /**
     * 打开数据库连接
     *
     * @param params 数据库连接属性
     * @return <code>0</code>打开连接成功;
     *         <code>-1</code>打开连接失败.
     */
    @Override
    protected int open(DataSourceParams params) {
        try {
            oraDataSource = new OracleConnectionPoolDataSource();

            String url = makeURL(params);
            oraDataSource.setURL(url);
            oraDataSource.setUser(params.getUser());
            oraDataSource.setPassword(params.getPassword());

            sqlConnection = oraDataSource.getConnection();
        } catch (SQLException e) {
            logger.error(e);
            return e.getErrorCode();
        }

        return 0;
    }

    /**
     * 关闭数据库连接
     *
     * @return
     */
    @Override
    public void close() {
        super.close();

        if (null != oraDataSource) {
            try {
                oraDataSource.close();
                oraDataSource = null;
            } catch (SQLException e) {
                logger.error(e);
            }
        }
    }

    /**
     * 通过连接属性获取连接字符串
     * @param params 连接属性
     * @return
     */
    private static String makeURL(DataSourceParams params){
        return String.format("jdbc:oracle:thin:@%1$s:%2$d:%3$s",
                params.getHost(),
                params.getPort(),
                params.getDb());
    }

    @Override
    protected SqlCursor getCursor(Statement stmt, ResultSet rs) {
        return new OracleCursor(stmt, rs);
    }

    @Override
    protected SqlSharedCursor getSharedCursor(PreparedStatement stmt, ResultSet rs){
        return new OracleSharedCursor(stmt, rs);
    }
}
