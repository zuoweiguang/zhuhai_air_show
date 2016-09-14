package com.navinfo.mapspotter.process.topic.construction;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ZhangJin1207 on 2016/3/9.
 */
public class ConfigProperties {
    Properties pro = new Properties();
    public ConfigProperties(String fileName){
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            pro.load(in);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getProperty(String strKey){
        return  pro.getProperty(strKey);
    }
}
