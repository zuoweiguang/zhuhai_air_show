package com.navinfo.mapspotter.process.topic.construction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navinfo.mapspotter.foundation.util.JsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ZhangJin1207 on 2016/3/2.
 */
public class BaseRoadModle {
    private String strGeo = null;
    public String getStrGeo(){
        return strGeo;
    }
    public  void setStrGeo(String strgeo){
        strGeo = strgeo;
    }
    private double[] Geom;
    public double[] getGeom(){return Geom;}
    public void setGeom(double[] geom){
        Geom = geom;
    }
    private int Link_Pid = 0;
    public int getLink_Pid(){
        return Link_Pid;
    }
    public void setLink_Pid(int link_pid){
        Link_Pid = link_pid;
    }

    private int Function_Class = 0;
    public int getFunction_Class(){
        return Function_Class;
    }
    public void setFunction_Class(int function_class){
        Function_Class = function_class;
    }

    private double Link_Len = .0;
    public double getLink_Len(){
        return Link_Len;
    }
    public void setLink_Len(double link_len){
        Link_Len = link_len;
    }

    private int Kind = 0;
    public int getKind(){
        return Kind;
    }
    public void  setKind(int kind){
        Kind = kind;
    }

    private int Mesh_ID = 0;
    public int getMesh_ID(){
        return Mesh_ID;
    }
    public void setMesh_ID(int mesh_id){
        Mesh_ID = mesh_id;
    }

    private int Limit_Type = 0;
    public int getLimit_Type(){
        return Limit_Type;
    }
    public void setLimit_Type(int limit_type){
        Limit_Type = limit_type;
    }

    public static BaseRoadModle PraseJsonStr(String strJson){
        if (strJson == null || strJson.isEmpty()){
            return null;
        }
        JsonUtil jsonUtil = JsonUtil.getInstance();
        BaseRoadModle baseRoadModle = new BaseRoadModle();
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            JsonNode root = objectMapper.readTree(strJson);
            JsonNode GeoNode = root.path("geometry");
            String strgeo = GeoNode.toString();
            baseRoadModle.setStrGeo(strgeo);
            JsonNode CoordinateNode = GeoNode.findPath("coordinates");
            Iterator<JsonNode> its1 = CoordinateNode.elements();
            List<double[]> coorlist = new ArrayList<>();
            while(its1.hasNext()){
                JsonNode node = its1.next();
                double[] coor = jsonUtil.readDoubleArray(node.toString());
                coorlist.add(coor);
            }
            double[] Geom = new double[coorlist.size()*2];
            int i = 0;
            for (double[] co : coorlist){
                Geom[i++] = co[0];
                Geom[i++] = co[1];
            }
            baseRoadModle.setGeom(Geom);
            JsonNode PropNode = root.path("properties");
            JsonNode PidNode = PropNode.findPath("link_pid");
            baseRoadModle.setLink_Pid(PidNode.intValue());
            JsonNode FunclassNode = PropNode.findPath("function_class");
            baseRoadModle.setFunction_Class(FunclassNode.intValue());
            JsonNode KindNode = PropNode.findPath("kind");
            baseRoadModle.setKind(KindNode.intValue());
            JsonNode LenNode = PropNode.findPath("length");
            baseRoadModle.setLink_Len(LenNode.doubleValue());
            JsonNode MeshNode = PropNode.findPath("mesh_id");
            baseRoadModle.setMesh_ID(MeshNode.intValue());

            JsonNode LimiteNode = PropNode.findPath("link_limit");
            Iterator<JsonNode> its = LimiteNode.elements();
            while(its.hasNext()){
                JsonNode node = its.next();
                JsonNode LimitTypeNode = node.findPath("type");
                if (LimitTypeNode.intValue() == 4){
                    baseRoadModle.setLimit_Type(LimitTypeNode.intValue());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return baseRoadModle;
    }

    public static void TestParseJson(String strJson){
        if (strJson == null || strJson.isEmpty()){
            return;
        }

        JsonUtil jsonUtil = JsonUtil.getInstance();

        try{
            Map<Object , Object> fmap = jsonUtil.readMap(strJson);

            for (Map.Entry entry : fmap.entrySet()){
                String strKey = entry.getKey().toString();

                Map<Object , Object> value = (Map<Object , Object>)entry.getValue();

                for(Map.Entry entry1 : value.entrySet()){
                    String Key = entry1.getKey().toString();
                    String sVal = entry1.getValue().toString();
                    int b = 0;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
