package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.mysql.MysqlDataBase;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

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
    public void testOracle(){
        DataBase db = DataBase.getDatabase(DataBase.DataBaseType.Oracle);
        Assert.assertNotNull(db);

        ConnectParams prop = new ConnectParams();
        prop.setHost("192.168.3.151");
        prop.setPort(1521);
        prop.setDb("orcl");

        db.open(prop);

        try(DBConnection conn = db.getConnection("IDBG_15SUM_0705_BJ", "1")) {
            Assert.assertNotNull(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
    }

    @Test
    public void testMysql(){
        DataBase db = DataBase.getDatabase(DataBase.DataBaseType.MySql);
        Assert.assertNotNull(db);

        ConnectParams prop = new ConnectParams();
        prop.setHost("localhost");
        prop.setPort(3306);
        prop.setDb("reynold");

        db.open(prop);

        MysqlDataBase mysqlDB= (MysqlDataBase)db;

        try(DBConnection conn = db.getConnection("root", "1qaz")) {
            Assert.assertNotNull(conn);

            List<DBConnection> conns = new ArrayList<>();
            for(int i=0;i<9;i++){
                conns.add(db.getConnection("root", "1qaz"));
                Assert.assertEquals(i+1, mysqlDB.getConnectionCount());
            }

            for(DBConnection c : conns){
                c.close();
            }

            DBConnection conn2 = db.getConnection("another","321654");
            conn2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.close();
    }
}
