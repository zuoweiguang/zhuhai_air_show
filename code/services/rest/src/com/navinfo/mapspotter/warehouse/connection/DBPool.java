package com.navinfo.mapspotter.warehouse.connection;

import com.navinfo.mapspotter.process.storage.pool.MongoPool;
import com.navinfo.mapspotter.process.storage.pool.PostgisPool;

/**
 * Created by SongHuiXing on 6/27 0027.
 */
public class DBPool {
    private volatile static DBPool uniqueInstance;

    private final MongoPool mgPool;

    private final MongoPool mgManagerPool;

    private final MongoPool zhuhaiMongo;

    private final PostgisPool pgPool;

    private DBPool() {
        mgPool = new MongoPool("192.168.4.128", 27017, "warehouse");
        mgManagerPool = new MongoPool("192.168.4.128", 27017, "manager");
        zhuhaiMongo = new MongoPool("192.168.4.128", 27017, "zhuhai_air_show");
        pgPool = new PostgisPool("192.168.4.104", 5440, "navinfo", "postgres", "navinfo1!pg");
//        mgPool = new MongoPool("172.20.10.122", 27017, "warehouse");
//        mgManagerPool = new MongoPool("172.20.10.122", 27017, "manager");
//        mgBaotouPool = new MongoPool("172.20.10.122", 27017, "baotou_demo");
//        pgPool = new PostgisPool("172.20.10.121", 5440, "baotou_demo", "postgres", "navinfo1!pg");
    }

    public static DBPool getInstance() {
        if (uniqueInstance == null) {
            synchronized (DBPool.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new DBPool();
                }
            }
        }

        return uniqueInstance;
    }

    public boolean initDatabaseConnections(){
        System.out.println("Initialize postgis and mongo");

        boolean pg = pgPool.setup();
        if(!pg){
            System.out.println("Initialize postgis failed!");
        }

        boolean mg = mgPool.setup();
        if(!mg){
            System.out.println("Initialize mongo failed!");
        }

        boolean mgM = mgManagerPool.setup();
        if(!mg){
            System.out.println("Initialize mgManagerPool failed!");
        }

        boolean mgB = zhuhaiMongo.setup();
        if(!mgB){
            System.out.println("Initialize zhuhaiMongo failed!");
        }

        return pg && mg && mgM && mgB;
    }

    public void closeConnections(){
        System.out.println("Close postgis and mongo");

        try {
            pgPool.close();
            mgPool.close();
            mgManagerPool.close();
            zhuhaiMongo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized MongoPool getMongo(){
        return mgPool;
    }

    public synchronized MongoPool getMongoManagerPool(){
        return mgManagerPool;
    }

    public synchronized MongoPool getZhuhaiMongoPool(){
        return zhuhaiMongo;
    }

    public synchronized PostgisPool getPostGIS(){
        return pgPool;
    }
}
