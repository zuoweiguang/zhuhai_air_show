package com.navinfo.mapspotter.process.topic.construction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZhangJin1207 on 2016/3/8.
 */
public class ConstructionResultInfo {
    @JsonProperty("link_pid")
    private int link_pid = 0;
    public void  setLink_pid(int link_pid){
        this.link_pid = link_pid;
    }
    public int getLink_pid(){
        return link_pid;
    }

    @JsonProperty("link_pn")
    private int link_pn = 0;
    public void setLink_pn(int link_pn){
        this.link_pn = link_pn;
    }
    public int getLink_pn(){
        return link_pn;
    }

    @JsonProperty("track_pn")
    private int track_pn = 0;
    public void setTrack_pn(int track_pn){
        this.track_pn = track_pn;
    }
    public int getTrack_pn(){
        return track_pn;
    }

    @JsonProperty("link_indensity")
    private int link_indensity = 0;
    public void setLink_indensity(int link_indensity) {
        this.link_indensity = link_indensity;
    }
    public int getLink_indensity() {
        return link_indensity;
    }
    @JsonProperty("far_link_pn")
    private int far_link_pn = 0;
    public void setFar_link_pn(int far_link_pn) {
        this.far_link_pn = far_link_pn;
    }
    public int getFar_link_pn() {

        return far_link_pn;
    }

    @JsonProperty("far_track_pn")
    private int far_track_pn = 0;
    public void setFar_track_pn(int far_track_pn) {
        this.far_track_pn = far_track_pn;
    }
    public int getFar_track_pn() {
        return far_track_pn;
    }

    @JsonProperty("far_link_indensity")
    private int far_link_indensity = 0;
    public void setFar_link_indensity(int far_link_indensity) {
        this.far_link_indensity = far_link_indensity;
    }
    public int getFar_link_indensity() {
        return far_link_indensity;
    }

    @JsonProperty("near_link_pn")
    private int near_link_pn = 0;
    public void setNear_link_pn(int near_link_pn) {
        this.near_link_pn = near_link_pn;
    }
    public int getNear_link_pn() {
        return near_link_pn;
    }

    @JsonProperty("near_track_pn")
    private int near_track_pn = 0;
    public void setNear_track_pn(int near_track_pn) {
        this.near_track_pn = near_track_pn;
    }
    public int getNear_track_pn() {
        return near_track_pn;
    }

    @JsonProperty("near_link_indensity")
    private int near_link_indensity = 0;
    public void setNear_link_indensity(int near_link_indensity) {
        this.near_link_indensity = near_link_indensity;
    }
    public int getNear_link_indensity() {
        return near_link_indensity;
    }

    @JsonProperty("weight")
    private double weight = 1.0;
    public void setWeight(double weight){
        this.weight = weight;
    }
    public double getWeight(){
        return weight;
    }

    @JsonProperty("tile_indensity")
    private double tile_indensity;
    public void setTile_indensity(double tile_indensity){this.tile_indensity = tile_indensity;}
    public double getTile_indensity(){return tile_indensity;}

    @JsonProperty("tile")
    private String tile;
    public String getTile(){return tile;}
    public void setTile(String tile){this.tile = tile;}

    public static ConstructionResultInfo GetInfo(String strInfo){
        if (strInfo.isEmpty()){
            return null;
        }
        try {
            ConstructionResultInfo constructionResultInfo = JsonUtil.getInstance().readValue(strInfo , ConstructionResultInfo.class);
            return constructionResultInfo;
        }catch (Exception e){
            return null;
        }
    }

    public String ConvertJsonStr(){
        String strJson = "";
        try{
            strJson = JsonUtil.getInstance().write2String(this);
            return strJson;
        }catch (Exception e){
            return null;
        }
    }
    @Override
    public String toString(){
        String strret = "";

        strret += getLink_pid() + "\t";
        strret += getLink_pn() + "\t" + getTrack_pn() + "\t" + getLink_indensity() + "\t";
        strret += getFar_link_pn() + "\t" + getFar_track_pn() + "\t" + getFar_link_indensity() + "\t";
        strret += getNear_link_pn() + "\t" + getNear_track_pn() + "\t" + getNear_link_indensity() + "\t";
        strret += getWeight()  + "\t" + getTile_indensity() + "\t" + getTile();

        return strret;
    }
}
