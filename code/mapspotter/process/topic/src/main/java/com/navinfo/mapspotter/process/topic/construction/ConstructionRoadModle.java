package com.navinfo.mapspotter.process.topic.construction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import java.util.List;


/**
 * Created by ZhangJin1207 on 2016/4/14.
 */
public class ConstructionRoadModle {

    private String linkpid;
    private int    functionclass;
    private String mcodes;


    @JsonProperty("link_pid")
    public String getLinkpid(){return linkpid;}
    public void  setLinkpid(String linkpid){this.linkpid = linkpid;}

    @JsonProperty("fun")
    public int getFunctionclass(){return functionclass;}
    public void  setFunctionclass(int functionclass){this.functionclass = functionclass;}


    @JsonProperty("mcodes")
    private List<String> mcodeslist = null;
    public void setMcodslist(List<String> list){
        this.mcodeslist = list;
    }

    public List<String> getMcodslist(){return mcodeslist;}

    public static ConstructionRoadModle prase(String json){
        try{
            ConstructionRoadModle cmodle = JsonUtil.getInstance().readValue(json , ConstructionRoadModle.class);
            return cmodle;
        }catch (Exception e){
            return null;
        }
    }
    @Override
    public String toString(){
        try{
            return JsonUtil.getInstance().write2String(this);
        }catch (Exception e){
            return null;
        }
    }
}
