package com.navinfo.mapspotter.foundation.io.mongo;

import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by SongHuiXing on 2016/1/5.
 */
public class MongoDBCursorTest {

    private DataBase client = null;

    private MongoDBInstance mongoDB = null;

    private String testTable = "TestCollection";

    private MongoDBCursor cursor = null;

    @Before
    public void setUp() throws Exception {
        client = DataBase.getDatabase(DataBase.DataBaseType.Mongo);

        ConnectParams prop = new ConnectParams();
        prop.setHost("119.29.88.106");
        prop.setPort(27017);
        prop.setDb("reynold");

        client.open(prop);

        mongoDB = (MongoDBInstance)client.getConnection("", "");

        MongoOperator operator1 = new MongoOperator();
        operator1.and(MongoOperator.FilterType.EQ, "Job.Address", "ShangHai");
        operator1.and(MongoOperator.FilterType.GT, "Age", 20);

        ArrayList<String> fields = new ArrayList<>();
        fields.add("Name");
        fields.add("Age");
        fields.add("Job");

        cursor = (MongoDBCursor) mongoDB.query(testTable, fields, operator1);
    }

    @After
    public void tearDown() throws Exception {
        cursor.close();
        mongoDB.close();
        client.close();
    }

    @Test
    public void testReset() throws Exception {
        int count =0;

        while (cursor.next()){
            count++;
        }

        Assert.assertEquals(0, count);

        cursor.reset();

        Assert.assertTrue(cursor.next());
    }

    @Test
    public void testConvert() throws Exception {
        while (cursor.next()){
            TestPOJO pojo = cursor.convert(TestPOJO.class);

            Assert.assertNotNull(pojo);

            System.out.println(String.format("%tF", pojo.getWork().StartDay));

            Assert.assertNotNull(pojo.Name);

            Assert.assertFalse(pojo.Gender);
        }
    }
}