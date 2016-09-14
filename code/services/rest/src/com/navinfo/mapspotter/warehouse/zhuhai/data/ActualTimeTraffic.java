package com.navinfo.mapspotter.warehouse.zhuhai.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.warehouse.zhuhai.util.PropertiesUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
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
                if (null != flow) {
                    JSONObject flowJson = this.getFlow(flow);
                    System.out.println("flow:" + flowJson.toString());
                }
                if (null != event) {

                }
            }
            System.out.println("size:" + meshList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public JSONObject getFlow(String flow) {
        JSONObject flowJson = new JSONObject();
        try {
            byte[] meshData = Base64.decodeBase64(flow.getBytes());
            // 计算2次网格号，JamNew中格网号占3个字节
            int i = 0;
            int x1 = (((meshData[i+2] & 0xF0)<<1)|(meshData[i]& 0xF8)>>3);
            int y1 = (((meshData[i+2] & 0xF)<<5)|(meshData[i+1]& 0xF8)>>3);
            int x2 = ((meshData[i] & 0x7) & 0xFF);
            int y2 = ((meshData[i+1] & 0x7) & 0xFF);
            int meshCode = y1 * 10000 + x1 * 100 + y2 * 10 + x2;
            flowJson.put("meshCode", meshCode);

            // RTIC记录数
            i += 3;
            int rticCnt = ( ((meshData[i] & 0xFF) << 8) | ((meshData[i+1]& 0xFF)) );
            flowJson.put("rticCnt", rticCnt);

            // 路况信息
            i += 2;
            for (int j = 0; j < rticCnt; j++) {

                // 路链分类     0高速；1快速；2一般；3其他
                int rticKind = ((meshData[i] & 0xF0) >> 4) + 1;
                flowJson.put("rticKind", rticKind);

                // 路链序号 路链序号取值范围为1～4095
                int rticId = (meshData[i] & 0x0F) << 8 | (meshData[i+1] & 0xFF);
                flowJson.put("rticId", rticId);

                // 路链旅行时间   当前路链的旅行时间，单位是秒，取值范围0-8190，当值为8191时，表示“不明”。
                i += 2;
                int travelTime = (meshData[i] & 0xFF) << 8 | (meshData[i+1] & 0xFF);
                flowJson.put("travelTime", travelTime);

                // 拥堵路段数    当前路链包含的路段的个数。
                int sectionCnt = meshData[i] & 0xFF;
                flowJson.put("sectionCnt", sectionCnt);

                // 拥堵程度 0不明；1通畅；2缓慢；3拥堵
                i += 1;
                int iLOS=(meshData[i] & 0x18) >> 3;
                flowJson.put("status", iLOS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return flowJson;
        }

    }



    public static void main(String[] args) {
        new ActualTimeTraffic().getTraffic();
    }

}
