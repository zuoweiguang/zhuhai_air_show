package com.navinfo.mapspotter.process.topic.restriction.io;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.MeshUtil;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 母库路口数据的Json模型
 * Created by SongHuiXing on 2016/1/11.
 */
public class BaseCrossJsonModel {

    /**
     * 从子路口调头信息
     */
    public static class TurnFromChild{
        public long childCrossPid = 0;      //子路口pid
        public int inLinkIndex = 0;          //进入线在父路口内的序号
        public int outLinkIndex = 0;         //退出线在父路口内的序号
    }

    public BaseCrossJsonModel(){
        m_pid = 0;
    }

    private long m_pid;
    @JsonProperty("pid")
    public long getPID(){
        return m_pid;
    }
    public void setPID(long id){
        m_pid = id;
    }

    private double[] m_center = new double[2];
    @JsonIgnore
    public double[] getCenter(){
        return m_center;
    }

    private String mesh;
    @JsonIgnore
    public String getMesh(){
        return mesh;
    }
    public void setMesh(String mesh_str){
        mesh = mesh_str;
    }

    private String m_envelope;
    @JsonRawValue
    public String getEnvelope(){
        return m_envelope;
    }
    public void setEnvelope(String env){
        m_envelope = env;
    }

    private String m_linkenvelope;
    @JsonProperty("linkenvelope")
    @JsonRawValue
    public String getLinkenvelope(){
        return m_linkenvelope;
    }
    public void setLinkenvelope(String env){
        m_linkenvelope = env;
    }

    private String m_restriction;
    @JsonProperty("restrictions")
    @JsonRawValue
    public String getRestriction(){
        return m_restriction;
    }
    public void setRestriction(String restric){
        m_restriction = restric;
    }

    private String m_linkDirection;
    @JsonProperty("linkdirection")
    @JsonRawValue
    public String getLinkDirection(){
        return m_linkDirection;
    }
    public void setLinkDirection(String linkDirs){
        m_linkDirection = linkDirs;
    }

    private String m_nodes;
    @JsonProperty("nodes")
    @JsonRawValue
    public String getNodes(){
        return m_nodes;
    }
    public void setNodes(String nodesStr){
        m_nodes = nodesStr;
    }

    private String m_links;
    @JsonProperty("links")
    @JsonRawValue
    public String getLinks(){
        return m_links;
    }
    public void setLinks(String linksStr){
        m_links = linksStr;
    }

    //是否是纯调头口
    public boolean IsPureUTurn(){
        return -1 != parentCrossPid;
    }

    //纯调头口的父路口id
    private long parentCrossPid = -1;
    @JsonProperty("parentcross")
    public long getParentCrossPid(){
        return parentCrossPid;
    }
    public void setParentCrossPid(long pid){
        this.parentCrossPid = pid;
    }

    //该路口的禁调link号[进入link，退出link],对应父路口的调头路线是从‘退出link’到‘进入link’
    private long[] forbiddenUTurn;
    @JsonProperty("forbiddenuturn")
    public long[] getForbiddenUTurn(){
        return forbiddenUTurn;
    }
    public void setForbiddenUTurn(long[] forbidden){
        forbiddenUTurn = forbidden;
    }

    public void addChild(long pid, int inlink, int outlink){
        TurnFromChild child = new TurnFromChild();
        child.childCrossPid = pid;
        child.inLinkIndex = inlink;
        child.outLinkIndex = outlink;

        childTurns.add(child);
    }

    private ArrayList<TurnFromChild> childTurns = new ArrayList<>();
    @JsonProperty("childturns")
    public ArrayList<TurnFromChild> getChildTurns(){
        return childTurns;
    }
    public void setChildTurns(ArrayList<TurnFromChild> children){
        childTurns = children;
    }

    public static BaseCrossJsonModel readCrossJson(String crossJson){
        ObjectMapper mapper = new ObjectMapper();

        TypeFactory factory = mapper.getTypeFactory();

        ArrayType longArrayType = factory.constructArrayType(long.class);
        CollectionType childTurnType = factory.constructCollectionType(ArrayList.class,
                                                                        TurnFromChild.class);

        BaseCrossJsonModel crossModel = new BaseCrossJsonModel();
        try {
            JsonNode root = mapper.readTree(crossJson);

            JsonNode childNode = root.path("pid");
            crossModel.m_pid = childNode.longValue();

            childNode = root.path("envelope");
            crossModel.m_envelope = childNode.toString();

            Iterator<JsonNode> coordNodes = childNode.elements();
            double[] envelope = new double[4];
            int i=0;
            while (coordNodes.hasNext()) {
                JsonNode temp = coordNodes.next();
                envelope[i] = temp.doubleValue();
                i++;
            }
            crossModel.m_center[0] = (envelope[0] + envelope[2]) / 2.0;
            crossModel.m_center[1] = (envelope[1] + envelope[3]) / 2.0;

            crossModel.setMesh(MeshUtil.coordinate2Mesh(crossModel.m_center));

            childNode = root.path("linkenvelope");
            crossModel.m_linkenvelope = childNode.toString();

            childNode = root.path("restrictions");
            crossModel.m_restriction = childNode.toString();

            childNode = root.path("linkdirection");
            crossModel.m_linkDirection = childNode.toString();

            childNode = root.path("links");
            crossModel.m_links = childNode.toString();

            childNode = root.path("nodes");
            crossModel.m_nodes = childNode.toString();

            childNode = root.path("parentcross");
            crossModel.parentCrossPid = childNode.longValue();

            childNode = root.path("forbiddenuturn");
            crossModel.forbiddenUTurn = mapper.readValue(childNode.toString(), longArrayType);

            JsonNode childrenNode = root.findPath("childturns");
            if(!childrenNode.isMissingNode()){
                ArrayList<TurnFromChild> children = mapper.readValue(childrenNode.toString(), childTurnType);
                crossModel.setChildTurns(children);
            }

        } catch (JsonProcessingException e) {
            Logger.getLogger(BaseCrossJsonModel.class).error(crossJson);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return crossModel;
    }

    public static Map.Entry<Long, Long> getParentCrossId(String crossJson){
        long pid = -1, parentid = -1;

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(crossJson);

            JsonNode childNode = root.path("parentcross");
            parentid = childNode.longValue();

            childNode = root.path("pid");
            pid = childNode.longValue();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry<>(pid, parentid);
    }
}

