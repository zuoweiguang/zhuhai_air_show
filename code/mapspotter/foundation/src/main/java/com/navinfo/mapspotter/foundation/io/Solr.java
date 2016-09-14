package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.io.util.SolrQueryClause;
import com.navinfo.mapspotter.foundation.util.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.List;

/**
 * Created by huanghai on 2016/1/5.
 */
public class Solr extends DataSource {
    private static final Logger logger = Logger.getLogger(Solr.class);

    private CloudSolrClient solrClient = null;

    protected Solr() {
        super();
    }

    /**
     * 打开连接
     * @param params 数据源参数 host：zookeeper host；db：Default Collection
     * @return 0：成功；-1：失败
     */
    @Override
    protected int open(DataSourceParams params) {
        try {
            solrClient = new CloudSolrClient(params.getHost());
            solrClient.setDefaultCollection(params.getDb());
            solrClient.connect();
        } catch (Exception e) {
            logger.error(e);
            solrClient = null;
            return -1;
        }
        return 0;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {
        IOUtil.closeStream(solrClient);
        solrClient = null;
    }

    /**
     * 查询
     * @param query 查询条件
     * @param fields 返回字段
     * @return
     */
    public Cursor query(SolrQueryClause query, List<String> fields) {
        return query(query.toString(), fields);
    }

    public Cursor query(String query, List<String> fields) {
        return query(query, "", fields);
    }

    public Cursor query(SolrQueryClause query, List<SolrQueryClause> filterQueries, List<String> fields) {
        return query(query.toString(), filterQueries, fields);
    }

    public Cursor query(String query, List<SolrQueryClause> filterQuery, List<String> fields) {
        String fq = "";

        for (SolrQueryClause clause : filterQuery) {
            if (fq.isEmpty()) {
                fq = clause.toString();
            } else {
                fq = fq + " AND " + clause.toString();
            }
        }

        return query(query, fq, fields);
    }

    public Cursor query(String query, String filterQuery, List<String> fields) {
        SolrQuery solrQuery = new SolrQuery(query);

        if (filterQuery != null && !filterQuery.isEmpty()) {
            solrQuery.setFilterQueries(filterQuery);
        }

        if (fields != null) {
            for (String field : fields) {
                solrQuery.addField(field);
            }
        }

        QueryResponse response = null;
        try {
            response = solrClient.query(solrQuery);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }

        return new SolrCursor(response.getResults());
    }

    /**
     * 保持数据
     *
     * @param sidList
     * @param collname
     * @return
     */
    public boolean saveSolrInputDocuments(List<SolrInputDocument> sidList, String collname) {
        try {
            solrClient.add(collname, sidList);
            solrClient.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("saveSolrInputDocuments SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("saveSolrInputDocuments IOException -> " + e);
        }

        return false;
    }

    /**
     * 根据id删除doc文档
     *
     * @param id       id集合
     * @param collname collection名称
     * @return
     */
    public boolean deleteDocById(String id, String collname) {
        try {
            solrClient.deleteById(collname, id);
            solrClient.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("deleteDocById SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("deleteDocById IOException -> " + e);
        }

        return false;
    }

    /**
     * 根据ids集合删除文档
     *
     * @param ids      id集合
     * @param collname collection名称
     * @return
     */
    public boolean deleteDocByIds(List<String> ids, String collname) {
        try {
            solrClient.deleteById(collname, ids);
            solrClient.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("deleteDocByIds SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("deleteDocByIds IOException -> " + e);
        }

        return false;
    }
}
