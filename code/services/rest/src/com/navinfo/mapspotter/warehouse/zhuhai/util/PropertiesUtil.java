package com.navinfo.mapspotter.warehouse.zhuhai.util;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletConfig;
import java.io.*;
import java.util.Properties;

/**
 * Created by zuoweiguang on 2016/9/7.
 */
public class PropertiesUtil {



    //获取配置文件数据库信息
    public static JSONObject getProperties() {
        String rootPath = PropertiesUtil.class.getClassLoader().getResource("").getPath();
        System.out.println("root path:" + rootPath);
        File directory = new File(rootPath + "/db.properties");
        System.out.println("file path:" + directory.toString());
        String filePath = directory.getAbsolutePath();
        Properties props = new Properties();
        InputStream in = null;
        JSONObject propObj = null;

        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);

            String actualTimeEventUrl = props.getProperty("actualTimeEventUrl");
            String ActualTimeForecastUrl = props.getProperty("ActualTimeForecastUrl");

            String pgHost = props.getProperty("pgHost");
            int pgPort = Integer.valueOf(props.getProperty("pgPort"));
            String pgDb = props.getProperty("pgDb");
            String pgUser = props.getProperty("pgUser");
            String pgPwd = props.getProperty("pgPwd");

            String mongoHost = props.getProperty("mongoHost");
            int mongoPort = Integer.valueOf(props.getProperty("mongoPort"));
            String mongoDb = props.getProperty("mongoDb");
            String eventColName = props.getProperty("eventColName");
            String forecastColName = props.getProperty("forecastColName");
            String trafficColName = props.getProperty("trafficColName");

            propObj = new JSONObject();

            propObj.put("actualTimeEventUrl", actualTimeEventUrl);
            propObj.put("ActualTimeForecastUrl", ActualTimeForecastUrl);

            propObj.put("pgHost", pgHost);
            propObj.put("pgPort", pgPort);
            propObj.put("pgDb", pgDb);
            propObj.put("pgUser", pgUser);
            propObj.put("pgPwd", pgPwd);

            propObj.put("mongoHost", mongoHost);
            propObj.put("mongoPort", mongoPort);
            propObj.put("mongoDb", mongoDb);
            propObj.put("eventColName", eventColName);
            propObj.put("forecastColName", forecastColName);
            propObj.put("trafficColName", trafficColName);

//            System.out.println(propObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {in.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
            return propObj;
        }
    }

    public static void main(String[] args) {
        System.out.println(getProperties().toString());
    }

}
