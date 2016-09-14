package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by SongHuiXing on 2016/1/5.
 */
public class MongoDBTest {

    private MongoDB mongoDB = null;

    private MongoDBCursor cursor = null;

    private String testTable = "layer";

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDB();

        DataSourceParams params = new DataSourceParams();
        params.setHost("192.168.4.128");
        params.setPort(27017);
        params.setDb("manager");
        params.setUser("");
        params.setPassword("");

        mongoDB.open(params);

        MongoOperator operator1 = new MongoOperator();
        operator1.and(MongoOperator.FilterType.EQ, "soluID", "1");

        cursor = (MongoDBCursor) mongoDB.query(testTable, operator1);
    }

    @After
    public void tearDown() throws Exception {
        cursor.close();
        mongoDB.close();
    }

    @Test
    public void testOpenClose() throws Exception {
        MongoDB mongoDB = new MongoDB();

        DataSourceParams params = new DataSourceParams();
        params.setHost("119.29.88.106");
        params.setPort(27017);
        params.setDb("local");

        Assert.assertEquals(0, mongoDB.open(params));

        mongoDB.close();
    }

    @Test
    public void testCreate() throws Exception {
        //Assert.assertTrue(mongoDB.create(testTable));
    }

    @Test
    public void testQuery() throws Exception {
        MongoDBCursor cursor = (MongoDBCursor)mongoDB.query(testTable);

        Assert.assertNotNull(cursor);

        cursor.close();
    }

    @Test
    public void testQuery1() throws Exception {
        MongoOperator operator1 = new MongoOperator();
        operator1.and(MongoOperator.FilterType.EQ, "Job.Address", "Beijing");
        operator1.and(MongoOperator.FilterType.GT, "Age", 20);

        Cursor cursor = mongoDB.query(testTable, operator1);

        Assert.assertNotNull(cursor);

        cursor.close();
    }

    @Test
    public void testQuery2() throws Exception {
        MongoOperator operator1 = new MongoOperator();
        operator1.and(MongoOperator.FilterType.EQ, "Job.Address", "Beijing");
        operator1.and(MongoOperator.FilterType.GT, "Age", 20);

        ArrayList<String> fields = new ArrayList<>();
        fields.add("Name");
        fields.add("Age");
        fields.add("Job");

        Cursor cursor = mongoDB.query(testTable, fields, operator1);

        Assert.assertNotNull(cursor);

        cursor.close();
    }

    @Test
    public void testDrop() throws Exception {
        Assert.assertTrue(mongoDB.drop(testTable));
    }

    @Test
    public void testInsert() throws Exception {
        TestPOJO doc1 = new TestPOJO();
        doc1.Name = "Bob";
        doc1.Gender = true;
        doc1.Age = 22;

        TestPOJO.Work job1 = new TestPOJO.Work();
        job1.Address = "BeiJing";
        job1.Salary = 10000;
        job1.StartDay = new Date(2016, 1, 1);
        doc1.setWork(job1);

        TestPOJO doc2 = new TestPOJO();
        doc2.Name = "Zoe";
        doc2.Gender = false;
        doc2.Age = 26;

        TestPOJO.Work job2 = new TestPOJO.Work();
        job2.Address = "ShangHai";
        job2.Salary = 15000;
        job2.StartDay = new Date(2013, 7, 1);
        doc2.setWork(job2);

        ArrayList<Object> docs = new ArrayList<>();
        docs.add(doc1);
        docs.add(doc2);
        Assert.assertEquals(0,mongoDB.insert(testTable, docs));
    }

    @Test
    public void testDelete() throws Exception {
        MongoOperator operator = new MongoOperator();
        operator.and(MongoOperator.FilterType.EQ, "Job.Address", "Heilongjiang");
        operator.and(MongoOperator.FilterType.GT, "Job.Salary", 10001);

        ArrayList<String> names = new ArrayList<>();
        names.add("Rey");
        names.add("Jim");
        operator.orIn(true, "Name", names);

        long delCount = mongoDB.delete(testTable, operator);

        Assert.assertEquals(0, delCount);

        MongoOperator operator1 = new MongoOperator();
        operator1.and(MongoOperator.FilterType.EQ, "Job.Address", "Heilongjiang");
        operator1.and(MongoOperator.FilterType.GT, "Job.Salary", 9999);

        delCount = mongoDB.delete(testTable, operator1);

        Assert.assertEquals(1, delCount);
    }

    @Test
    public void testReset() throws Exception {
        int count =0;

        cursor.ascendSort("seqnum");
        while (cursor.next()){
            System.out.println(cursor.get("name"));
        }
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