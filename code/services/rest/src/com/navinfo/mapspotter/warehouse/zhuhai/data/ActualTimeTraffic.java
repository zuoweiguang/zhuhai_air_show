package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.warehouse.zhuhai.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ActualTimeTraffic {

    public static String sendGet() {
        String url = "http://192.168.59.235:8080/TEGateway/123456/RTICTraffic.json?bizcode=ths&adcode=440400&version=1501&datatype=3";
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
//        System.out.println(result);
        getTraffic(result);
        return result;
    }


    public static void getTraffic(String resps) {

        byte[] flowByte = null;
        byte[] eventByte = null;
        try {
            JSONObject jsonObj = JSONObject.parseObject(resps);
            JSONObject result = jsonObj.getJSONObject("result");
            JSONObject city = result.getJSONObject("cities").getJSONObject("city");
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
        ActualTimeTraffic.sendGet();
    }

}
