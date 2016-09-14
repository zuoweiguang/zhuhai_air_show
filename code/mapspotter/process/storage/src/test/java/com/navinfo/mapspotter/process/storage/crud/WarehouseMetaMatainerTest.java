package com.navinfo.mapspotter.process.storage.crud;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 7/18 0018.
 */
public class WarehouseMetaMatainerTest {

    private MongoDB db = null;

    @Before
    public void setup(){
        db = (MongoDB) DataSource.getDataSource(IOUtil.makeMongoDBParams("192.168.4.128",
                                                                        27017,
                                                                        "manager"));
    }

    @After
    public void clean(){
        db.close();
    }

    @Test
    public void testUpdateSourceMetadata() throws Exception {

    }

    @Test
    public void testInitDefaultStyle() throws Exception {
        WarehouseMetaMatainer matainer = new WarehouseMetaMatainer(db);

        matainer.initDefaultStyle("E:\\WorkSpace\\MapSpotter\\warehouse metadata");
    }
}