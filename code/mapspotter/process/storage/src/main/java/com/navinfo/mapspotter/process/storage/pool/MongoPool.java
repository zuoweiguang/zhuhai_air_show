package com.navinfo.mapspotter.process.storage.pool;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;

/**
 * Created by SongHuiXing on 6/27 0027.
 */
public class MongoPool implements AutoCloseable {
    private final DataSourceParams params;

    private MongoDB database;

    public MongoPool(String host, int port, String db){
        params = IOUtil.makeMongoDBParams(host, port, db);
    }

    public MongoPool(String host, int port, String db, String user, String pwd) {
        params = IOUtil.makeMongoDBParams(host, port, db, user, pwd);
    }

    public boolean setup() {
        if(null != database)
            return true;

        database = (MongoDB) DataSource.getDataSource(params);

        return database != null;
    }

    public MongoDB getMongo(){
        return database;
    }

    @Override
    public void close() throws Exception {
        if (null != database) {
            database.close();
            database = null;
        }
    }
}
