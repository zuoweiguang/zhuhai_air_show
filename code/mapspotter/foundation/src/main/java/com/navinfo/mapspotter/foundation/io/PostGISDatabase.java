package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 访问PostgreSQL+PostGIS的数据库接口类
 * Created by SongHuiXing on 5/16 0016.
 */
public class PostGISDatabase extends SqlDatabase {
    private static final Logger logger = Logger.getLogger(PostGISDatabase.class);

    protected PostGISDatabase(){
        super();
    }

    @Override
    protected int open(DataSourceParams params) {
        try {
            Class.forName("org.postgresql.Driver");

            String url = makeURL(params);

            sqlConnection = DriverManager.getConnection(url, params.getUser(), params.getPassword());

        } catch (ClassNotFoundException | SQLException e) {
            logger.error(e);
            return -1;
        }

        return 0;
    }

    /**
     * 通过连接属性获取连接字符串
     * @param params 连接属性
     * @return
     */
    private static String makeURL(DataSourceParams params){
        return String.format("jdbc:postgresql://%1$s:%2$d/%3$s",
                params.getHost(),
                params.getPort(),
                params.getDb());
    }
}
