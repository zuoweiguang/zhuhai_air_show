package com.navinfo.mapspotter.foundation.io.solr;

import com.navinfo.mapspotter.foundation.io.Util;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.List;

/**
 * Created by huanghai on 2016/1/5.
 */
public class CloudSolrClientBase {
    private static final Logger logger = Logger.getLogger(CloudSolrClientBase.class);

    /**
     * 获取CloudSolrClient
     *
     * @return
     */
    public CloudSolrClient getSolrClient() {
        String zkSolrHost = PropertiesUtil.getValue("ZK_SOLR_HOST");
        logger.info("getSolrClient zkSolrHost -> " + zkSolrHost);
        return new CloudSolrClient(zkSolrHost);
    }

    /**
     * 保持数据
     *
     * @param sidList
     * @param collname
     * @return
     */
    public boolean saveSolrInputDocuments(List<SolrInputDocument> sidList, String collname) {
        CloudSolrClient solr = getSolrClient();
        solr.setDefaultCollection(collname);
        try {
            solr.add(collname, sidList);
            solr.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("saveSolrInputDocuments SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("saveSolrInputDocuments IOException -> " + e);
        } finally {
            Util.closeStream(solr);
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
        CloudSolrClient solr = getSolrClient();
        solr.setDefaultCollection(collname);
        try {
            solr.deleteById(collname, id);
            solr.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("deleteDocById SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("deleteDocById IOException -> " + e);
        } finally {
            Util.closeStream(solr);
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
        CloudSolrClient solr = getSolrClient();
        solr.setDefaultCollection(collname);
        try {
            solr.deleteById(collname, ids);
            solr.commit();
            return true;
        } catch (SolrServerException e) {
            logger.error("deleteDocByIds SolrServerException -> " + e);
        } catch (IOException e) {
            logger.error("deleteDocByIds IOException -> " + e);
        } finally {
            Util.closeStream(solr);
        }
        return false;
    }
}
