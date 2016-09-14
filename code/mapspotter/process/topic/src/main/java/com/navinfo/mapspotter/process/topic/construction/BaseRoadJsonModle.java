package com.navinfo.mapspotter.process.topic.construction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;

/**
 * 解析施工道路Json串
 * Created by ZhangJin1207 on 2016/1/18.
 */
public class BaseRoadJsonModle {
    private int PID;
    @JsonProperty
    public int getPID(){
        return PID;
    }

    public void setPID(int pid){
        PID = pid;
    }

    private int Funtion_Class;
    @JsonProperty
    public int getFuntion_Class(){
        return Funtion_Class;
    }

    public void setFuntion_Class(int fun){
        Funtion_Class = fun;
    }

    private double sdo_srid;
    public double getSdo_srid(){
        return sdo_srid;
    }

    public void setSdo_srid(double dsrid){
        sdo_srid = dsrid;
    }
    private double[] Coordinates;

    public double[] getCoordinates(){
        return Coordinates;
    }

    public void setCoordinates(double[] coors){
        Coordinates = coors;
    }

    private double sdo_gtype;
    public double getSdo_gtype(){
        return sdo_gtype;
    }
    public void setSdo_gtype(double gtype){
        sdo_gtype = gtype;
    }
    public static BaseRoadJsonModle PraseRoadJson(String roadjson){
        BaseRoadJsonModle BaseJson = new BaseRoadJsonModle();
        ObjectMapper mapper = new ObjectMapper();

        try{
            JsonNode root = mapper.readTree(roadjson);
            JsonNode pidNode = root.path("pid");
            BaseJson.setPID(pidNode.intValue());

            JsonNode funNode = root.path("fun");
            BaseJson.setFuntion_Class(funNode.intValue());

            JsonNode GeoNode = root.path("geom");

            JsonNode ordinateNode = GeoNode.findPath("SDO_ORDINATES");

            String strordinates = ordinateNode.toString();
            strordinates = strordinates.replace("[" , "");
            strordinates = strordinates.replace("]" , "");

            String[] coordinates = strordinates.split(",");
            double[] dcoordinates = new double[coordinates.length];
            int i = 0;
            for (String coor : coordinates){
                dcoordinates[i] = Double.parseDouble(coor);
                i++;
            }
            BaseJson.setCoordinates(dcoordinates);
            JsonNode GeosidNode = GeoNode.findPath("SDO_SRID");

            double d_srid = GeosidNode.doubleValue();
            BaseJson.setSdo_srid(d_srid);

            JsonNode gtypeNode = GeoNode.findPath("SDO_GTYPE");
            BaseJson.setSdo_gtype(gtypeNode.doubleValue());

        }catch (JsonProcessingException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return BaseJson;
    }
}
