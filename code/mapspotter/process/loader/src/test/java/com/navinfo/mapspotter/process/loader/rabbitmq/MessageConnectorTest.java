package com.navinfo.mapspotter.process.loader.rabbitmq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/25 0025.
 */
public class MessageConnectorTest {

    private MessageConnector connector;

    @Before
    public void setUp() throws Exception {
        connector = new MessageConnector("amqp://fos:fos@192.168.4.188:5672", null);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSend() throws Exception {
        ArrayList<String> msgs = new ArrayList<>();

        msgs.add("{\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"i_level\":2," +
                "\"b_sourceCode\":1," +
                "\"b_sourceId\":\"sfoiuojkw89234jkjsfjksf\"," +
                "\"b_reliability\":3," +
                "\"INFO_NAME\":\"道路通车\"," +
                "\"INFO_CONTENT\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\"}");

        msgs.add("{\"rowkey\":\"f1e8945e-3aa1-11e6-ac61-9e71128cae77\",\"i_level\":1," +
                "\"b_sourceCode\":1," +
                "\"b_sourceId\":\"82943298045\"," +
                "\"b_reliability\":2," +
                "\"INFO_NAME\":\"交限变更\"," +
                "\"INFO_CONTENT\":\"长安街全部禁止左转\"}");

        msgs.add("{\"rowkey\":\"f1e89922-3aa1-11e6-ac61-9e71128cae77\",\"i_level\":3," +
                "\"b_sourceCode\":1," +
                "\"b_sourceId\":\"9d0fsfjskf89\"," +
                "\"b_reliability\":3," +
                "\"INFO_NAME\":\"道路形状变化\"," +
                "\"INFO_CONTENT\":\"小营西路环岛变化\"}");

        connector.send("Info_Change", msgs);
    }
}