package com.navinfo.mapspotter.foundation.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 特殊游标，共享外部的statement，关闭时只关闭resultset
 * Created by SongHuiXing on 2016/2/20.
 */
public class SqlSharedCursor extends SqlCursor {

    protected SqlSharedCursor(Statement stmt, ResultSet rs) {
        super(stmt, rs);
    }

    @Override
    public void close() throws SQLException {
        if(null != sqlResultSet){
            sqlResultSet.close();
        }
    }
}
