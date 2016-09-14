package com.navinfo.mapspotter.foundation.io.mysql;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.MysqlDefs;
import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by SongHuiXing on 2015/12/30.
 */
public class MysqlConnection extends DBConnection {

    private Connection mysqlConnection = null;

    private MysqlDataBase mysqlDB = null;

    MysqlConnection(Connection mysqlConn, MysqlDataBase db){
        mysqlConnection = mysqlConn;
        mysqlDB = db;
    }

    /**
     * 执行SQL语句
     *
     * @param sql 需要执行的SQL语句
     * @return <code>0</code>打开连接成功;
     * <code>-1</code>打开连接失败.
     */
    @Override
    public int execute(String sql) {
        int resultCode = 0;

        try {
            Statement stmt = mysqlConnection.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            resultCode = e.getErrorCode();
        }
        return resultCode;
    }

    /**
     * 通过查询语句查询数据库
     *
     * @param sql 查询语句
     * @return 结果集游标 @see DBCursor
     */
    @Override
    public DBCursor query(String sql) {
        DBCursor cursor = null;

        try {
            Statement stmt = mysqlConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            cursor = new MysqlCursor(stmt,rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cursor;
    }

    @Override
    public int executeMany(String paramSql, List<List<Object>> datarows) {
        if(null == mysqlConnection)
            return -1;

        if(0 == datarows.size())
            return 0;

        int colCount = datarows.get(0).size();

        try(PreparedStatement stmt = mysqlConnection.prepareStatement(paramSql)){

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
    public void close() throws Exception {
        mysqlDB.releaseConnection(this);
        mysqlConnection = null;
        mysqlDB = null;
    }

    void changeUser(String user, String password) throws SQLException {
        mysqlConnection.changeUser(user, password);
    }

    Connection getSqlConnection(){
        return mysqlConnection;
    }
}
