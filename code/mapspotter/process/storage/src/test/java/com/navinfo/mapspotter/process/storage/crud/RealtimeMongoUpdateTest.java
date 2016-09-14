package com.navinfo.mapspotter.process.storage.crud;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/23 0023.
 */
public class RealtimeMongoUpdateTest {

    private RealtimeMongoUpdate mongoUpdate;

    @Before
    public void setUp() throws Exception {
        mongoUpdate =
                new RealtimeMongoUpdate("192.168.4.128", 27017, "warehouse", null, null,
                        "amqp://fos:fos@192.168.4.188:5672", null, "notify_poi");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConsumer() throws Exception {
        if(!mongoUpdate.open()) {
            assertTrue(false);
            return;
        }

        while (true){
            
        }
    }
}