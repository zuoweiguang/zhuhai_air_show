package com.navinfo.mapspotter.foundation.io.mongo;

import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by SongHuiXing on 2016/1/5.
 */
public class MongoDBClientTest {

    @Test
    public void testGetDatabase() throws Exception {
        DataBase client = DataBase.getDatabase(DataBase.DataBaseType.Mongo);

        Assert.assertNotNull(client);
    }

    @Test
    public void testOpenClose() throws Exception {
        DataBase client = DataBase.getDatabase(DataBase.DataBaseType.Mongo);

        Assert.assertNotNull(client);

        ConnectParams prop = new ConnectParams();
        prop.setHost("119.29.88.106");
        prop.setPort(27017);
        prop.setDb("local");

        Assert.assertEquals(0, client.open(prop));

        Assert.assertEquals(0, client.close());
    }

    @Test
    public void testGetConnection() throws Exception {
        DataBase client = DataBase.getDatabase(DataBase.DataBaseType.Mongo);

        Assert.assertNotNull(client);

        ConnectParams prop = new ConnectParams();
        prop.setHost("119.29.88.106");
        prop.setPort(27017);
        prop.setDb("local");

        Assert.assertEquals(0, client.open(prop));

        MongoDBInstance mongoDB = (MongoDBInstance)client.getConnection("", "");

        Assert.assertNotNull(mongoDB);

        Assert.assertEquals(0, client.close());
    }
}