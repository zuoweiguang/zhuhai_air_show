package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 数据库访问接口
 */
public abstract class SqlDatabase extends DataSource {
    private static final Logger logger = Logger.getLogger(SqlDatabase.class);

    private static final int cursor_fetch_size = 50;

    protected Connection sqlConnection = null;

    protected SqlDatabase() {
        super();
    }

    protected SqlCursor getCursor(Statement stmt, ResultSet rs) {
        return new SqlCursor(stmt, rs);
    }

    protected SqlSharedCursor getSharedCursor(PreparedStatement stmt, ResultSet rs){
        return new SqlSharedCursor(stmt, rs);
    }

    /**
     * 执行SQL语句
     * @param sql 需要执行的SQL语句
     * @return <code>0</code>打开连接成功;
     *         <code>-1</code>打开连接失败.
     */
    public int execute(String sql) {
        if(null == sqlConnection)
            return -1;

        int status = 0;

        try(Statement stmt = sqlConnection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error(e);
            status = e.getErrorCode();
        }

        return status;
    }

    public int execute(String paramSql, Object... paramValues) {
        if(null == sqlConnection)
            return -1;

        try(PreparedStatement stmt = sqlConnection.prepareStatement(paramSql)){

            bindParameters(stmt, paramValues);

            stmt.execute();
        } catch (SQLException e){
            logger.error(e);
            return e.getErrorCode();
        }

        return 0;
    }

    /**
     * 通过查询语句查询数据库
     * @param sql 查询语句
     * @return 结果集游标 @see Cursor
     */
    public SqlCursor query(String sql) {
        if(null == sqlConnection)
            return null;

        SqlCursor cursor = null;

        try {
            Statement stmt = sqlConnection.createStatement();
            stmt.setFetchSize(cursor_fetch_size);

            ResultSet rs = stmt.executeQuery(sql);

            cursor = getCursor(stmt, rs);
        } catch (SQLException e) {
            logger.error(e);
            return null;
        }

        return cursor;
    }

    public SqlCursor query(String sql, Object... paramValues) throws SQLException {
        if(null == sqlConnection)
            return null;

        SqlCursor cursor = null;

        try {
            PreparedStatement stmt = sqlConnection.prepareStatement(sql);

            bindParameters(stmt, paramValues);

            stmt.setFetchSize(cursor_fetch_size);

            ResultSet rs = stmt.executeQuery();

            cursor = getCursor(stmt, rs);
        } catch (SQLException e) {
            logger.error(e);
            return null;
        }

        return cursor;
    }

    public PreparedStatement prepare(String sql) throws SQLException {
        return sqlConnection.prepareStatement(sql);
    }

    public SqlSharedCursor query(PreparedStatement stmt, Object... paramValues) throws SQLException {
        if(null == sqlConnection)
            return null;

        SqlSharedCursor cursor = null;

        try {
            bindParameters(stmt, paramValues);

            stmt.setFetchSize(cursor_fetch_size);

            ResultSet rs = stmt.executeQuery();

            cursor = getSharedCursor(stmt, rs);
        } catch (SQLException e) {
            logger.error(e);
            return null;
        }

        return cursor;
    }

    /**
     * 执行多次
     * @param paramSql 参数化SQL
     * @param datarows 绑定数据
     * @return
     */
    public int executeMany(String paramSql, List<List<Object>> datarows) {
        if(null == sqlConnection)
            return -1;

        if(0 == datarows.size())
            return 0;

        try(PreparedStatement stmt = sqlConnection.prepareStatement(paramSql)){

            for(List<Object> data : datarows){
                bindParameters(stmt, data);

                stmt.execute();

                stmt.clearParameters();
            }
        } catch (SQLException e){
            logger.error(e);
            return e.getErrorCode();
        }

        return 0;
    }

    public int excute(PreparedStatement stmt, Object... params) throws SQLException {
        if(null == stmt || stmt.isClosed())
            return -1;

        stmt.clearParameters();

        bindParameters(stmt, params);

        stmt.execute();

        return 0;
    }

    /**
     * 调用一个VARCHAR输入一个VARCHAR输出的存储过程，
     * @param proc 存储过程名
     * @param param 输入参数
     * @return 输出
     */
    public String callProcedure(String proc, String param) {
        if(null == sqlConnection)
            return null;

        String out = null;
        String call = String.format("{ call %s(?,?) }", proc);
        try (CallableStatement cs = sqlConnection.prepareCall(call)) {
            cs.setString(1, param);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            out = cs.getString(2);
        } catch (SQLException e) {
            logger.error(e);
        }

        return out;
    }

    @Override
    public void close() {
        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        sqlConnection = null;
    }

    private void bindParameters(PreparedStatement statement, Object... paramValues) throws SQLException {

        for (int i=0;i<paramValues.length;i++) {
            Object col = paramValues[i];

            if (col instanceof Integer) {
                statement.setInt(i + 1, ((Integer) col).intValue());
            } else if (col instanceof String) {
                statement.setString(i + 1, col.toString());
            } else if (col instanceof Double) {
                statement.setDouble(i + 1, ((Double) col).doubleValue());
            } else if (col instanceof Boolean) {
                statement.setBoolean(i + 1, ((Boolean) col).booleanValue());
            } else if (col instanceof InputStream) {
                statement.setBlob(i + 1, (InputStream) col);
            } else if (col instanceof byte[]) {
                statement.setBytes(i + 1, (byte[]) col);
            } else if(col instanceof Timestamp) {
                statement.setTimestamp(i+1, (Timestamp)col);
            } else {
                statement.setObject(i + 1, col);
            }
        }
    }

}
