package com.navinfo.mapspotter.foundation.io;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Created by SongHuiXing on 2016/1/4.
 */
public class MongoDBCursor implements Cursor {
    private static final Logger logger = Logger.getLogger(MongoDBCursor.class);

    private static final int CURSOR_BATCHSIZE = 500;

    private FindIterable<Document> findResult = null;

    private MongoCursor<Document> cursor = null;

    private Document currentDoc = null;

    protected MongoDBCursor(FindIterable<Document> finded){
        findResult = finded.batchSize(CURSOR_BATCHSIZE);

        cursor = findResult.iterator();
    }

    public void ascendSort(String... ascendingFields){
        findResult = findResult.sort(Sorts.ascending(ascendingFields));
        cursor = findResult.iterator();
    }

    public void descendSort(String... descendingFields){
        findResult = findResult.sort(Sorts.descending(descendingFields));
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
    public Object get(String field) throws Exception {
        return currentDoc.get(field);
    }

    public <T> T convert(Class<T> type){
        T obj;

        try {
            obj = JsonUtil.getInstance().readValue(currentDoc.toJson(), type);
        } catch (IOException e) {
            logger.error(e);
            return null;
        }

        return obj;
    }

    public <K,V> Map<K, V> convert(){
        try {
            return JsonUtil.getInstance().readMap(currentDoc.toJson());
        } catch (IOException e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        if(null != findResult){
            findResult = null;
        }
    }
}
