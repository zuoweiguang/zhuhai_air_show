package com.navinfo.mapspotter.foundation.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Oracle 数据库访问游标
 * Created by SongHuiXing on 2015/12/29.
 */
public class OracleCursor extends OracleSharedCursor {

    protected OracleCursor(Statement stmt, ResultSet rs){
        super(stmt, rs);
    }

    /**
     * 关闭游标
     *
     * @return
     */
    @Override
    public void close() throws SQLException {
        if(null != sqlResultSet){
            sqlResultSet.close();
        }

        if(null != sqlStmt){
            sqlStmt.close();
            sqlStmt = null;
        }
    }
}
