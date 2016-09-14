package com.navinfo.mapspotter.foundation.io.oracle;

import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;

import java.sql.*;
import java.util.List;

/**
 * Oracle瘦客户端数据连接
 * Created by SongHuiXing on 2015/12/29.
 */
public class OracleConnection extends DBConnection {

    private Connection oraConnection = null;

    OracleConnection(Connection oraConn){
        oraConnection = oraConn;
    }

    /**
     * 执行SQL语句
     *
     * @param sql 需要执行的SQL语句
     * @return <code>0</code>打开连接成功;
     *          <code>-1</code>打开连接失败.
     */
    @Override
    public int execute(String sql) {
        if(null == oraConnection)
            return -1;

        int status = 0;

        try(Statement stmt = oraConnection.createStatement()){
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            status = e.getErrorCode();
        }

        return status;
    }

    /**
     * 通过查询语句查询数据库
     *
     * @param sql 查询语句
     * @return 结果集游标 @see DBCursor
     */
    @Override
    public DBCursor query(String sql) {
        if(null == oraConnection)
            return null;

        DBCursor cursor = null;

        try {
            Statement stmt = oraConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            cursor = new OracleCursor(stmt, rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return cursor;
    }

    @Override
    public int executeMany(String paramSql, List<List<Object>> datarows) {
        if(null == oraConnection)
            return -1;

        if(0 == datarows.size())
            return 0;

        int colCount = datarows.get(0).size();

        try(PreparedStatement stmt = oraConnection.prepareStatement(paramSql)){

            for(List<Object> data : datarows){
                for (int i=0;i<data.size();i++){
                    Object col = data.get(i);

                    if(col instanceof Integer){
                        stmt.setInt(i+1, ((Integer) col).intValue());
                    } else if(col instanceof String){
                        stmt.setString(i+1, col.toString());
                    } else if(col instanceof Double){
                        stmt.setDouble(i+1, ((Double) col).doubleValue());
                    } else if(col instanceof Boolean){
                        stmt.setBoolean(i+1, ((Boolean) col).booleanValue());
                    } else{
                        stmt.setObject(i+1, col);
                    }
                }

                if(!stmt.execute())
                    return -1;

                stmt.clearParameters();
            }
        } catch (SQLException e){
            e.printStackTrace();
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
    public void close() throws SQLException {
        if(null != oraConnection){
            oraConnection.close();
        }
    }
}
