package com.navinfo.mapspotter.foundation.io.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.navinfo.mapspotter.foundation.io.DBConnection;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 2016/1/4.
 */
public class MongoDBInstance extends DBConnection {
    private MongoDatabase database = null;

    private ObjectMapper objectMapper = new ObjectMapper();

    public MongoDBInstance(MongoDatabase db){
        database = db;
    }

    @Override
    public int execute(String sql) {
        throw new NotImplementedException();
    }

    @Override
    public int executeMany(String paramSql, List<List<Object>> datarows) {
        throw new NotImplementedException();
    }

    @Override
    public DBCursor query(String table) {
        if(null == database)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find());
    }

    public DBCursor query(String table, MongoOperator condition){
        if(null == database)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find(condition.getFilter()));
    }

    public DBCursor query(String table, List<String> fields, MongoOperator condition){
        if(null == database)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find(condition.getFilter())
                                       .projection(condition.getProject(true, fields)));
    }

    public boolean create(String table){
        if(null == database)
            return  false;

        database.createCollection(table);

        return true;
    }

    public boolean drop(String table){
        if(null == database)
            return false;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return false;

        dbColl.drop();

        return true;
    }

    public <T> int insert(String table, List<T> objects){
        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return -1;

        List<Document> doces = new ArrayList<>();
        for(T obj : objects){
            String jsonStr = null;

            try {
                jsonStr = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            if(null == jsonStr)
                continue;

            doces.add(Document.parse(jsonStr));
        }

        dbColl.insertMany(doces);

        return 0;
    }

    public long delete(String table, MongoOperator filter){
        if(null == database)
            return -1;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return -1;

        DeleteResult res = dbColl.deleteMany(filter.getFilter());

        return res.getDeletedCount();
    }

    private MongoCollection<Document> getTable(String tablename){
        if(null == database)
            return null;

        return database.getCollection(tablename);
    }

    @Override
    public void close() throws Exception {
        database = null;
    }
}
