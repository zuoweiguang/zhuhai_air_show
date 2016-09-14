package com.navinfo.mapspotter.process.convert.road.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.geojson.Feature;
import org.jblas.DoubleMatrix;

import java.sql.SQLException;
import java.util.*;

/**
 * 从母库导出的路口信息类
 * Created by SongHuiXing on 2016/4/5.
 */
public class ExportedCross {

    private Predicate findIn = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return ((RDCrossOutLink)o).getDirection() == 2;
        }
    };

    private Predicate findOut = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return ((RDCrossOutLink)o).getDirection() == 3;
        }
    };

    private final int cross_pid;

    public ExportedCross(int pid){
        cross_pid = pid;

        envelope[0] = 0;
        envelope[1] = 0;
        envelope[2] = 0;
        envelope[3] = 0;
    }

    public boolean build(OracleStatementContainer oracle, boolean getBranchLinks){
        findNodes(oracle);

        findLinks(oracle, getBranchLinks);

        buildRestrictionMatrix();

        queryPureUTurnInfo(oracle);

        return true;
    }

    private int findNodes(OracleStatementContainer oracle){
        try(SqlCursor cursor = oracle.queryCrossNodes(cross_pid)){
            while (cursor.next()){
                RDCrossNode node = RDCrossNode.getNode(cursor.getInteger(1), oracle);
                if(null == node)
                    continue;

                Point pt = node.getGeometry();

                if(envelope[0] != 0){
                    if(envelope[0] > pt.getX())
                        envelope[0] = pt.getX();
                    else if(envelope[2] < pt.getX())
                        envelope[2] = pt.getX();

                    if(envelope[1] > pt.getY())
                        envelope[1] = pt.getY();
                    else if(envelope[3] < pt.getY())
                        envelope[3] = pt.getY();
                } else {
                    envelope[0] = pt.getX();
                    envelope[2] = pt.getX();
                    envelope[1] = pt.getY();
                    envelope[3] = pt.getY();
                }

                nodes.add(node);
            }
        } catch (SQLException e){

        }

        return nodes.size();
    }

    private int findLinks(OracleStatementContainer oracle, boolean getBranch){
        for(RDCrossNode node : nodes){
            HashMap<Integer, Integer> s_links = node.getStartConnectLinks();
            for (Map.Entry<Integer, Integer> l : s_links.entrySet()){
                RDCrossOutLink link = RDCrossOutLink.getLink(l.getKey(),
                                                            l.getValue(),
                                                            node.getPID(),
                                                            true,
                                                            getBranch,
                                                            oracle);

                if(null == link){
                    continue;
                }

                outlinks.add(link);
            }

            HashMap<Integer, Integer> e_links = node.getEndConnectLinks();
            for (Map.Entry<Integer, Integer> l : e_links.entrySet()){
                RDCrossOutLink link = RDCrossOutLink.getLink(l.getKey(),
                                                            l.getValue(),
                                                            node.getPID(),
                                                            false,
                                                            getBranch,
                                                            oracle);

                if(null == link){
                    continue;
                }

                outlinks.add(link);
            }
        }

        return outlinks.size();
    }

    private void buildRestrictionMatrix(){
        int linkCount = outlinks.size();

        restrictions = DoubleMatrix.zeros(linkCount, linkCount);
        link2linkDir = DoubleMatrix.zeros(2, linkCount);

        for (int i = 0; i < linkCount; i++) {
            RDCrossOutLink link = outlinks.get(i);

            if (link.getCrossNodeIsStart()) {
                link2linkDir.put(0, i, 3);
                link2linkDir.put(1, i, 2);
            } else {
                link2linkDir.put(0, i, 2);
                link2linkDir.put(1, i, 3);
            }

            int dir = link.getDirection();
            HashMap<Integer, Integer> res = link.getRestrictions();

            if (3 != dir) {
                for (int j = 0; j < linkCount; j++) {
                    RDCrossOutLink out = outlinks.get(j);
                    if (2 == dir && 2 == out.getDirection()) {
                        restrictions.put(i, j, 26);//同是进入线造成的理论交限
                    } else {
                        if (res.containsKey(out.getLinkPid())) {
                            restrictions.put(i, j, res.get(out.getLinkPid()));
                        }
                    }
                }
            } else {    //退出线造成的理论交限
                for (int j = 0; j < linkCount; j++) {
                    restrictions.put(i, j, 25);
                }
            }
        }
    }

    /**
     * 是否是大路口附属的纯调头路口
     *              |           |
     * --------------------------------------
     *              |           |
     * --------------------------------------
     * PureUTurn|   |           |    |pure U turn
     * --------------------------------------
     *              |           |
     * --------------------------------------
     *              |           |
     * @return
     */
    public boolean queryPureUTurnInfo(OracleStatementContainer oracle){
        if(nodes.size() != 2)
            return false;

        if(outlinks.size() != 4)
            return false;

        //group
        HashMap<Integer, ArrayList<RDCrossOutLink>> node_conn_links = new HashMap<>();
        for(RDCrossOutLink link : outlinks){
            Integer nodeid = link.getCrossNode();
            int dir = link.getDirection();
            if(dir == 1)
                return false;   //有双向通行道路

            ArrayList<RDCrossOutLink> links;
            if(node_conn_links.containsKey(nodeid)){
                links = node_conn_links.get(nodeid);
            } else {
                links = new ArrayList<>();
            }

            if(links.size() > 1){
                return false;
            }

            links.add(link);

            if(2 == links.size()) {
                if(links.get(0).getDirection() == dir)
                    return false;   //都是进入link或都是退出link
            }

            node_conn_links.put(nodeid, links);
        }

        ArrayList<RDCrossOutLink> n1_links = node_conn_links.get(nodes.get(0).getPID());
        ArrayList<RDCrossOutLink> n2_links = node_conn_links.get(nodes.get(1).getPID());

        UTurnPath path1 = new UTurnPath();
        path1.InLink = (RDCrossOutLink) CollectionUtils.find(n1_links, findIn);
        path1.OutLink = (RDCrossOutLink) CollectionUtils.find(n2_links, findOut);

        UTurnPath path2 = new UTurnPath();
        path2.InLink = (RDCrossOutLink) CollectionUtils.find(n2_links, findIn);
        path2.OutLink = (RDCrossOutLink) CollectionUtils.find(n1_links, findOut);

        int parentCrossId = isAnotherCrossPath(path1, oracle);
        if(parentCrossId != -1){
            this.parent_cross = parentCrossId;
            this.forbiddenU[0] = path1.InLink.getLinkPid();
            this.forbiddenU[1] = path1.OutLink.getLinkPid();
            return true;
        }

        parentCrossId = isAnotherCrossPath(path2, oracle);
        if(parentCrossId != -1) {
            this.parent_cross = parentCrossId;
            this.forbiddenU[0] = path2.InLink.getLinkPid();
            this.forbiddenU[1] = path2.OutLink.getLinkPid();
            return true;
        }

        return false;
    }

    /**
     * 是否是另外一个路口的退出
     * 且该退出具有禁调
     * @param path
     * @return 父路口ID，-1--不具备
     */
    private int isAnotherCrossPath(UTurnPath path, OracleStatementContainer oracle){
        int inLinkid = path.InLink.getLinkPid();
        int inNodeid = path.InLink.getCrossNode();

        int outLinkid = path.OutLink.getLinkPid();
        int outNodeid = path.OutLink.getCrossNode();

        if(getRestrictionInfo(inLinkid, outLinkid) != 4)
            return -1;  //不是禁调

        if(50 < path.InLink.getLength() || 50 < path.OutLink.getLength())
            return  -1; //距离太远

        ArrayList<Integer> anotherTwoNodes = new ArrayList<>(2);
        try(SqlCursor cursor = oracle.queryLinkNodes(inLinkid, outLinkid)){
            while (cursor.next()){
                int snid = cursor.getInteger(1);
                if(snid != inNodeid && snid != outNodeid)
                    anotherTwoNodes.add(snid);

                int enid = cursor.getInteger(2);
                if(enid != inNodeid && enid != outNodeid)
                    anotherTwoNodes.add(enid);
            }
        } catch (SQLException e){
            return -1;
        }

        if(anotherTwoNodes.size() != 2)
            return -1;

        int sameCrossId = -1;
        try(SqlCursor cursor = oracle.querySameCross(anotherTwoNodes.get(0), anotherTwoNodes.get(1))){
            while (cursor.next()){
                if(sameCrossId != -1)
                    return -1;  //两个点属于两个不同路口

                sameCrossId = cursor.getInteger(1);
            }
        } catch (SQLException e){
        }

        return sameCrossId;
    }

    private int getRestrictionInfo(int inLink, int outLink){
        int inIndex = -1;
        int outIndex = -1;

        for (int i = 0; i < outlinks.size(); i++) {
            int id = outlinks.get(i).getLinkPid();

            if(id == inLink)
                inIndex = i;

            if(id == outLink)
                outIndex = i;
        }

        if(inIndex == -1 || outIndex == -1)
            return 0;

        return (int)restrictions.get(inIndex, outIndex);
    }

    @JsonProperty("pid")
    public int getPID(){
        return cross_pid;
    }

    private double[] envelope = new double[4];
    @JsonProperty("envelope")
    public double[] getEnvelope(){
        return envelope;
    }

    private List<RDCrossNode> nodes = new ArrayList<>();
    @JsonProperty("nodes")
    public List<Feature> getNodes(){
        ArrayList<Feature> fts = new ArrayList<>(nodes.size());
        for(RDCrossNode n : nodes){
            fts.add(n.toJsonFeature());
        }
        return fts;
    }

    private ArrayList<RDCrossOutLink> outlinks = new ArrayList<>();
    @JsonProperty("links")
    public  List<Feature> getLinks(){
        ArrayList<Feature> fts = new ArrayList<>(outlinks.size());
        for(RDCrossOutLink l: outlinks){
            fts.add(l.toJsonFeature());
        }
        return fts;
    }

    private DoubleMatrix restrictions = null;
    @JsonProperty("restrictions")
    public int[][] getRestrictions(){
        return restrictions.toIntArray2();
    }

    //相对于画线方向的走行方向 2:顺 3:逆
    //从Link进入路口的方向 [[2 2 3]
    //从路口退出到该Link的方向 [3 3 2]]
    private DoubleMatrix link2linkDir = null;
    @JsonProperty("linkdirection")
    public int[][] getLinkDirection(){
        return link2linkDir.toIntArray2();
    }

    @JsonProperty("linkenvelope")
    public double[] getLinkEnvelope(){
        double[] linkEnv = null;
        for (RDCrossOutLink link : outlinks){
            if(null != linkEnv){
                double[] env = link.getEnvelope();
                if(linkEnv[0] < env[0])
                    linkEnv[0] = env[0];

                if(linkEnv[1] < env[1])
                    linkEnv[1] = env[1];

                if(linkEnv[2] < env[2])
                    linkEnv[2] = env[2];

                if(linkEnv[3] < env[3])
                    linkEnv[3] = env[3];
            } else {
                linkEnv = link.getEnvelope();
            }
        }

        return linkEnv;
    }

    //如果该路口是调头口，则该属性为其所属路口id
    private int parent_cross = -1;
    @JsonProperty("parentcross")
    public int getParentCross(){
        return parent_cross;
    }

    //纯调头口的禁调路线[进入，退出]
    private int[] forbiddenU = new int[2];
    @JsonProperty("forbiddenuturn")
    public int[] getForbiddenU(){
        return forbiddenU;
    }
}

class UTurnPath{
    public RDCrossOutLink InLink = null;
    public RDCrossOutLink OutLink = null;
}