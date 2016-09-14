package com.navinfo.mapspotter.process.topic.poihang;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by cuiliang on 2016/2/20.
 * 停用词过滤常量
 */
public class StopwordsConstant {
    public static Map<String,Boolean>  postfixMap= new HashMap<String,Boolean>();
    public static Map getPostfixMap(){

        postfixMap.put("停车场", true);
        postfixMap.put("代理店", true);

        return postfixMap;
    }
}
