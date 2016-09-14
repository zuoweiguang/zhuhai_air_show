package com.navinfo.mapspotter.foundation.io;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Created by gaojian on 2016/1/15.
 */
public class SolrCursor implements Cursor {
    private SolrDocumentList list;
    private int index;
    private SolrDocument currentDocument;

    protected SolrCursor(SolrDocumentList list) {
        this.list = list;
        index = -1;
        currentDocument = null;
    }

    @Override
    public boolean next() {
        index++;

        if (index >= list.size()) {
            index = list.size();
            currentDocument = null;
            return false;
        }

        currentDocument = list.get(index);
        return true;
    }

    @Override
    public void reset() {
        index = -1;
        currentDocument = null;
    }

    @Override
    public Object get(String field) throws Exception {
        if (currentDocument == null) {
            throw new IllegalStateException();
        }

        Object value = currentDocument.getFieldValue(field);

        return value;
    }

    @Override
    public void close() throws Exception {
        list = null;
        index = -1;
        currentDocument = null;
    }
}
