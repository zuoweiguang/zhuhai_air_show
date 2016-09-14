package com.navinfo.mapspotter.process.storage.pool;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.PostGISDatabase;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;

/**
 * Created by SongHuiXing on 6/27 0027.
 */
public class PostgisPool implements AutoCloseable {
    private PostGISDatabase pgDatabase = null;
    private final DataSourceParams params;

    public PostgisPool(String pghost, int pgPort, String pgDb, String pgUser, String pgPwd){
        params = IOUtil.makePostGISParam(pghost, pgPort, pgDb, pgUser, pgPwd);
    }

    public boolean setup() {
        if(null != pgDatabase)
            return true;

        pgDatabase = (PostGISDatabase) DataSource.getDataSource(params);

        return pgDatabase != null;
    }

    public PostGISDatabase getPgDatabase(){
        return pgDatabase;
    }

    @Override
    public void close() throws Exception {
        if (null != pgDatabase) {
            pgDatabase.close();
            pgDatabase = null;
        }
    }
}
