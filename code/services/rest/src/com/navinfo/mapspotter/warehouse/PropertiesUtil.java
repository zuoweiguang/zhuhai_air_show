package com.navinfo.mapspotter.warehouse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by SongHuiXing on 7/1 0001.
 */
public class PropertiesUtil {
    public String get(String key) throws IOException {
        Properties properties = new Properties();

        InputStream in = ClassLoader.getSystemResourceAsStream("fileresource.properties");

        if(null == in) {
            return null;
        }

        properties.load(in);

        return properties.getProperty(key);
    }
}
