package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by huanghai on 2016/1/5.
 */
public class SolrTest {

    @Test
    public void testOpen() {
        DataSourceParams params = new DataSourceParams();
        params.setHost(PropertiesUtil.getValue("ZK_SOLR_HOST"));
        params.setDb("ut_test_coll");

        Solr solr = new Solr();
        solr.open(params);
        solr.close();
    }


    @Test
    public void saveSolrInputDocuments() {
        Solr solr = new Solr();
        List<SolrInputDocument> sidList = new ArrayList<SolrInputDocument>();
        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("name", "yellowsea");
        sidList.add(doc1);
        SolrInputDocument doc2 = new SolrInputDocument();
        doc2.addField("name", "huanghai");
        sidList.add(doc2);
        boolean flag = solr.saveSolrInputDocuments(sidList, "ut_test_coll");
        assertTrue(flag);
    }


    @Test
    public void deleteDocById() {
        Solr solr = new Solr();
        boolean flag = solr.deleteDocById("140c9f4b-79e9-4e8c-a9f1-211477578d39", "ut_test_coll");
        assertTrue(flag);
    }

    @Test
    public void deleteDocByIds() {
        Solr solr = new Solr();
        List<String> ids = new ArrayList<String>();
        ids.add("6fe17e59-2d8c-49df-af5e-b4ee05136dc0");
        ids.add("e641b3f9-122f-4dc3-b14e-6cb5fcbb99d0");
        boolean flag = solr.deleteDocByIds(ids, "ut_test_coll");
        assertTrue(flag);
    }
}
