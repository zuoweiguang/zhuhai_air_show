package com.navinfo.mapspotter.foundation.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 配置文件读取
 *
 * @author huanghai
 *
 */
public class PropertiesUtil {
    private static final Logger logger = Logger.getLogger(PropertiesUtil.class);

    private static Properties pro = null;

    static {
        try (InputStream is = PropertiesUtil.class.getResourceAsStream("/conf.properties")) {
            pro = new Properties();
            pro.load(is);
        } catch (IOException e) {
            logger.error("PropertiesUtil -> IOException : " + e);
        }
    }

    public static String getValue(String key) {
        return (String) pro.get(key);
    }
}
