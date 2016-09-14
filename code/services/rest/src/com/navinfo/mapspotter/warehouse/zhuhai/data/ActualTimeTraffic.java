package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.warehouse.zhuhai.util.Base64;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ActualTimeTraffic extends ActualTimeRequest {

    private Logger logger = Logger.getLogger(ActualTimeEvent.class);
    private static JSONObject propObj = PropertiesUtil.getProperties();

    public void getTraffic() {
        byte[] flowByte = null;
        byte[] eventByte = null;
        try{
            String url = propObj.getString("ActualTimeTrafficUrl");
            String result = sendGet(url);
            JSONObject resultObj = JSONObject.parseObject(result);
            JSONObject resultValue = resultObj.getJSONObject("result");
            JSONObject city = resultValue.getJSONObject("cities").getJSONObject("city");
            String adcode = city.getString("adcode");
            String updatetime = city.getString("updatetime");
            String version = city.getString("version");
            JSONArray meshList = city.getJSONArray("mesh");
            System.out.println("adcode:" + adcode + ", updatetime:" + updatetime + ", version:" + version);
            for (int i = 0; i < meshList.size(); i ++) {
                JSONObject mesh = meshList.getJSONObject(i);
                String code = mesh.getString("code");
                System.out.println("code:" + code);
                String flow = mesh.getString("flow");
                String event = mesh.getString("event");
                System.out.print("flow:");
                if (null != flow) {
                    flowByte = Base64.decodeBase64(flow);
//                    for (int a = 0; a < flowByte.length; a ++) {
//                        System.out.print(flowByte[a]);
//                    }
                    System.out.print(new String(flowByte));
                }
                System.out.println();
                System.out.print("event:");
                if (null != event) {
                    eventByte = Base64.decodeBase64(event);
//                    for (int a = 0; a < eventByte.length; a ++) {
//                        System.out.print(eventByte[a]);
//                    }
                    System.out.print(new String(eventByte));
                }
                System.out.println();
            }
            System.out.println("size:" + meshList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    public static void main(String[] args) {
        new ActualTimeTraffic().getTraffic();
    }

}
