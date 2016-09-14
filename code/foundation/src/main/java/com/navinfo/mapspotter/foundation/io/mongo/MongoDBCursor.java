package com.navinfo.mapspotter.foundation.io.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.mapspotter.foundation.io.BasicDBCursor;
import com.navinfo.mapspotter.foundation.io.DBCursor;
import com.vividsolutions.jts.geom.Geometry;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 2016/1/4.
 */
public class MongoDBCursor extends DBCursor {

    private FindIterable<Document> findResult = null;

    private MongoCursor<Document> cursor = null;

    private Document currentDoc = null;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public MongoDBCursor(FindIterable<Document> finded){
        findResult = finded;

        cursor = findResult.iterator();
    }

    @Override
    public boolean next() {
        if(null == cursor || !cursor.hasNext())
            return false;

        currentDoc = cursor.next();

        return true;
    }

    @Override
    public void reset() {
        currentDoc = null;
        cursor = findResult.iterator();
    }

    @Override
    public Object fetch() {
        return currentDoc.toJson();
    }

    public <T> T convert(Class<T> type){
        T obj;

        try {
            obj = jsonMapper.readValue(currentDoc.toJson(), type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return obj;
    }

    @Override
    public void close() throws Exception {
        if(null != findResult){
            findResult = null;
        }
    }
}
