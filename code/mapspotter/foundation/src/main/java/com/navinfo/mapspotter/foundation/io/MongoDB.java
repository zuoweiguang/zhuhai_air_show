package com.navinfo.mapspotter.foundation.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.bson.BsonDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/1/4.
 */
public class MongoDB extends DataSource {
    private static final Logger logger = Logger.getLogger(MongoDB.class);

    private MongoClient client = null;

    private String db = null;

    private ObjectMapper objectMapper = new ObjectMapper();

    public MongoDB() {
        super();
    }

    @Override
    public int open(DataSourceParams params) {
        try {
            if(StringUtil.isEmpty(params.getUser())) {
                client = new MongoClient(params.getHost(), params.getPort());
            } else{
                MongoCredential credential =
                        MongoCredential.createCredential(params.getUser(),
                                                         params.getDb(),
                                                         params.getPassword().toCharArray());

                List<MongoCredential> credentials = new ArrayList<>();
                credentials.add(credential);

                client = new MongoClient(new ServerAddress(params.getHost(), params.getPort()),
                                         credentials);
            }

            db = params.getDb();

        } catch (Exception e){
            logger.error(e);
            return -1;
        }

        return null != db ? 0 : -1;
    }

    @Override
    public void close() {
        if(null != client){
            client.close();
            client = null;
        }
    }

    public long count (String table, MongoOperator condition){
        if(null == db)
            return 0;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return 0;

        return dbColl.count(condition.getFilter());
    }

    public Cursor query(String table) {
        if(null == db)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find());
    }

    public Cursor query(String table, MongoOperator condition){
        if(null == db)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find(condition.getFilter()));
    }

    public Cursor query(String table, List<String> fields, MongoOperator condition){
        if(null == db)
            return null;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return null;

        return new MongoDBCursor(dbColl.find(condition.getFilter())
                .projection(condition.getProject(true, fields)));
    }

    public boolean create(String table){
        if(null == db)
            return  false;

        if(null == getTable(table)) {
            MongoDatabase database = client.getDatabase(db);
            database.createCollection(table);
        }

        return true;
    }

    public boolean createIndex(String table, Map<String, Boolean> indexFields){
        if(null == db)
            return false;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return false;

        Document indexDoc = null;
        for (Map.Entry<String, Boolean> field : indexFields.entrySet()){
            if(null != indexDoc){
                indexDoc.append(field.getKey(), field.getValue() ? 1 : -1);
            } else {
                indexDoc = new Document(field.getKey(), field.getValue() ? 1 : -1);
            }
            dbColl.createIndex(indexDoc);
        }

        return true;
    }

    public boolean createGeoIndex(String table, Map<String, String> indexFields){
        if(null == db)
            return false;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return false;

        Document indexDoc = null;
        for (Map.Entry<String, String> field : indexFields.entrySet()){
            if(null != indexDoc){
                indexDoc.append(field.getKey(), field.getValue());
            } else {
                indexDoc = new Document(field.getKey(), field.getValue());
            }
            dbColl.createIndex(indexDoc);
        }

        return true;
    }

    public boolean drop(String table){
        if(null == db)
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

        List<WriteModel<Document>> doces = new ArrayList<>();
        for(T obj : objects){
            String jsonStr = null;

            try {
                jsonStr = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                logger.error(e);
            }

            if(null == jsonStr)
                continue;

            doces.add(new InsertOneModel<>(Document.parse(jsonStr)));
        }

        dbColl.bulkWrite(doces);

        return 0;
    }

    public int insert(String table, Map<String, Object> one) {
        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return -1;

        Document doc = new Document(one);
        dbColl.insertOne(doc);

        return 0;
    }

    public int insertJsons(String table, List<String> jsonStrings){
        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return -1;

        List<WriteModel<Document>> doces = new ArrayList<>();
        for(String jsonStr : jsonStrings){
            doces.add(new InsertOneModel<>(Document.parse(jsonStr)));
        }

        BulkWriteOptions options = new BulkWriteOptions();
        options.ordered(false);

        BulkWriteResult result = dbColl.bulkWrite(doces, options);

        return result.getInsertedCount() - jsonStrings.size();
    }

    public boolean update(String table, MongoOperator condition, String fileds, Object value){
        if(null == db)
            return false;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return false;

        dbColl.updateMany(condition.getFilter(), new Document("$set", new Document(fileds,value)));

        return true;
    }

    public boolean update(String table, MongoOperator condition, Map<String, Object> one){
        if(null == db)
            return false;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return false;
        Document document = new Document(one);
        dbColl.updateOne(condition.getFilter(), new Document("$set", document));
        return true;
    }

    public long delete(String table, MongoOperator filter){
        if(null == db)
            return -1;

        MongoCollection<Document> dbColl = getTable(table);
        if(null == dbColl)
            return -1;

        DeleteResult res = dbColl.deleteMany(filter.getFilter());

        return res.getDeletedCount();
    }

    public MongoCollection<Document> getTable(String tablename){
        if(null == db)
            return null;

        MongoDatabase database = client.getDatabase(db);

        if(null == database)
            return null;

        return database.getCollection(tablename);
    }

    public MongoIterable<String> getTables() {
        if(null == db)
            return null;

        MongoDatabase database = client.getDatabase(db);

        if(null == database)
            return null;

        return database.listCollectionNames();
    }

    public MongoIterable<String> getDbs() {

        return client.listDatabaseNames();

    }

}
