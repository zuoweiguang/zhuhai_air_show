package com.navinfo.mapspotter.process.storage.crud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by SongHuiXing on 6/28 0028.
 */
public class InfoMongoUpdateTest {

    private MongoDB mongoDB;

    @Before
    public void setUp() throws Exception {
        mongoDB = (MongoDB) DataSource.getDataSource(
                                IOUtil.makeMongoDBParams("192.168.4.128",
                                                        27017,
                                                        "warehouse"));
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.close();
    }

    @Test
    public void testGetInfomation() throws Exception {
        InfoMongoUpdate mongoUpdate = new InfoMongoUpdate(mongoDB);

        //JSONArray infos = mongoUpdate.getInfomation("59567301");
        //assertTrue(infos.size() > 0);

        JSONObject info = mongoUpdate.getInformation("b92c7efb7cb9479d880c3189d4067c33");

        assertNotNull(info);
    }

    @Test
    public void testInsertInfo() throws Exception {
        InfoMongoUpdate mongoUpdate = new InfoMongoUpdate(mongoDB);

        JSONObject attr = new JSONObject();
        attr.put("b_featureKind", 1);
        attr.put("b_sourceCode", 1);
        attr.put("b_sourceId", "20160118226EE8C71B214BC7AFD020ADBD6AA6F0");
        attr.put("b_reliability", 1);

        Point pt = new GeometryFactory().createPoint(new Coordinate(116.73834, 39.2983943));
        attr.put("g_location", GeoUtil.geometry2WKT(pt));

        JSONObject poi = new JSONObject();
        poi.put("kindCode","AB0001");
        poi.put("name","超市发");
        poi.put("address","海淀区成府路1号");
        poi.put("telephone","60218787");
        attr.put("i_poi", poi);

        JSONObject road = new JSONObject();
        road.put("roadKind", 4);
        attr.put("i_road", road);

        attr.put("i_memo", "");
        attr.put("i_level", 1);
        attr.put("t_expectDate", 134325454);
        attr.put("t_expDateReliab", 2);

        JSONArray feeds = new JSONArray();
        JSONObject feed = new JSONObject();
        feed.put("user_id", "110002");
        feed.put("type", 3);
        feed.put("content", "20161001");
        feed.put("auditRemark", "确认通过");
        feed.put("date", 173263838);
        feeds.add(feed);
        attr.put("f_array", feeds);

        assertTrue(mongoUpdate.insertInfo(attr).getValue());

        JSONObject info = JSON.parseObject("{\"b_featureKind\":1,\"b_sourceCode\":1,\"b_sourceId\":\"20160118226EE8C71B214BC7AFD020ADBD6AA6F0\",\"b_reliability\":1,\"g_location\":\"\",\"i_poi\":{\"kindCode\":\"AB0001\",\"name\":\"超市发\",\"address\":\"海淀区成府路1号\",\"telephone\":\"60218787\"},\"i_road\":{\"roadKind\":4},\"i_memo\":\"\",\"i_level\":1,\"t_expectDate\":134325454,\"t_expDateReliab\":2,\"f_array\":[{\"user_id\":\"110002\",\"type\":3,\"content\":\"20161001\",\"auditRemark\":\"确认通过\",\"date\":173263838}]}");

        assertTrue(mongoUpdate.insertInfo(info).getValue());
    }

    @Test
    public void testUpdateFeedback() throws Exception {
        InfoMongoUpdate mongoUpdate = new InfoMongoUpdate(mongoDB);

        JSONObject attr = new JSONObject();
        attr.put("globalId", "816c51f5459f4c8aaef88e9f50006c62");
        attr.put("b_featureKind", 1);
        attr.put("b_sourceCode", 1);
        attr.put("b_sourceId", "20160118226EE8C71B214BC7AFD020ADBD6AA6F0");
        attr.put("c_isAdopted", 1);
        attr.put("c_denyReason", "库中已有");
        attr.put("c_denyRemark", "在16sum版数据中已经存在");
        attr.put("c_userId", 110001);
        attr.put("c_pid", 0);
        attr.put("c_fid", 0);

        assertTrue(mongoUpdate.updateExtension(attr).getValue());
    }
}