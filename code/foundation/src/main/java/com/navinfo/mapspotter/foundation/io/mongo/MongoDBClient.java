package com.navinfo.mapspotter.foundation.io.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DataBase;
import com.navinfo.mapspotter.foundation.io.ConnectParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 2016/1/4.
 */
public class MongoDBClient extends DataBase {

    private MongoClient client = null;

    private MongoDatabase db = null;

    @Override
    public int open(ConnectParams connProperty) {
        try {
            if(connProperty.getUser().isEmpty()) {
                client = new MongoClient(connProperty.getHost(), connProperty.getPort());
            } else{
                MongoCredential credential =
                        MongoCredential.createCredential(connProperty.getUser(),
                                                         connProperty.getDb(),
                                                         connProperty.getPassword().toCharArray());

                List<MongoCredential> credentials = new ArrayList<>();
                credentials.add(credential);

                client = new MongoClient(new ServerAddress(connProperty.getHost(), connProperty.getPort()),
                                         credentials);
            }

            db = client.getDatabase(connProperty.getDb());

        } catch (Exception e){
            return -1;
        }

        return null != db ? 0 : -1;
    }

    @Override
    public DBConnection getConnection(String user, String password) {
        if(null == db)
            return null;

        return new MongoDBInstance(db);
    }

    @Override
    public int close() {

        if(null != client){
            client.close();
            client = null;
        }

        return 0;
    }
}
