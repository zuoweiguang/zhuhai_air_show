package com.navinfo.mapspotter.foundation.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by cuiliang on 2016/3/29.
 */
public class CommonPropertiesUtil {
    private static Log logger = LogFactory.getLog(PropertiesUtil.class);
    private static Properties pro = null;

    public CommonPropertiesUtil(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            pro = new Properties();
            pro.load(is);
        } catch (IOException e) {
            logger.error("PropertiesUtil -> IOException : " + e);
        }
    }
    public String getStringValue(String key) {
        return  pro.getProperty(key).trim();
    }

    public int getIntValue(String key) {
        return  Integer.parseInt(pro.getProperty(key).trim());
    }

    public static void main(String[] args) {
        CommonPropertiesUtil util = new CommonPropertiesUtil("F:\\conf.properties");
        System.out.println(util.getStringValue("storm.zks"));
    }
}
