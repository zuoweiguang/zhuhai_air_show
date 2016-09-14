package com.navinfo.mapspotter.process.topic.coverage;

import com.navinfo.mapspotter.foundation.io.IOUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件读取
 *
 * @author huanghai
 */
public class PropertiesUtil {
    private static Log logger = LogFactory.getLog(PropertiesUtil.class);
    private static InputStream is = null;
    private static Properties pro = null;

    static {
        try {
            is = new FileInputStream(System.getProperty("user.dir")
                    + File.separator + "/conf.properties");
            pro = new Properties();
            logger.info("user.dir is : " + System.getProperty("user.dir") + " PropertiesUtil Properties : " + pro);
            pro.load(is);
        } catch (IOException e) {
            logger.error("PropertiesUtil -> IOException : " + e);
        } finally {
            IOUtil.closeStream(is);
        }
    }

    public static String getValue(String key) {
        return (String) pro.get(key);
    }

    public static int getIntValue(String key) {
        String value = (String) pro.get(key);
        return StringUtils.isEmpty(value) ? Integer.MIN_VALUE : Integer.parseInt(value);
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }
}
