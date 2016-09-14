package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import org.junit.*;

/**
 * Created by SongHuiXing on 2015/12/30.
 */
public class DatabaseTest {

    @BeforeClass
    public static void oneTimeSetUp() {
        System.out.println("@BeforeClass - oneTimeSetUp");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("@AfterClass - oneTimeTearDown");
    }

    @Before
    public void setUp() {
        System.out.println("@Before - setUp");
    }

    @After
    public void tearDown() {
        System.out.println("@After - tearDown");
    }

    @Test
    public void testOracle() {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.Oracle);
        params.setHost("192.168.3.151");
        params.setPort(1521);
        params.setDb("orcl");
        params.setUser("IDBG_15SUM_0705_BJ");
        params.setPassword("1");

        OracleDatabase db = (OracleDatabase) DataSource.getDataSource(params);
        Assert.assertNotNull(db);

        db.close();
    }

    @Test
    public void testMysql() {
        DataSourceParams params = new DataSourceParams();
        params.setType(DataSourceParams.SourceType.MySql);
        params.setHost("localhost");
        params.setPort(3306);
        params.setDb("reynold");
        params.setUser("root");
        params.setPassword("1qaz");

        MysqlDatabase db = (MysqlDatabase) DataSource.getDataSource(params);
        Assert.assertNotNull(db);

        db.close();
    }
}
