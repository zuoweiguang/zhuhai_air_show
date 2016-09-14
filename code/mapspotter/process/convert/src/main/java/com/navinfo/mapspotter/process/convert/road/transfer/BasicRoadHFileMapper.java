package com.navinfo.mapspotter.process.convert.road.transfer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huanghai on 2016/3/8.
 */
public class BasicRoadHFileMapper extends Mapper<LongWritable, Text, Text, Text> {
    private static final Logger logger = Logger.getLogger(BasicRoadMapper.class);
    private int zoom;
    private int matrixInt;
    private double thresholdOne;
    private int thresholdTwo;
    private Text keyText = new Text();
    private Text valText = new Text();
    private MercatorUtil mercatorUtil;
    private Map<Integer, String> map = new HashMap<Integer, String>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        zoom = Integer.parseInt(conf.get("zoom"));
        matrixInt = Integer.parseInt(conf.get("matrixInt"));
        logger.info("setup zoom : " + zoom + " matrixInt : " + matrixInt);
        mercatorUtil = new MercatorUtil(matrixInt, zoom);
        //  门限值
        thresholdOne = Double.parseDouble(conf.get("thresholdOne"));
        thresholdTwo = Integer.parseInt(conf.get("thresholdTwo"));
        logger.info("thresholdOne : " + thresholdOne + " thresholdTwo : " + thresholdTwo);

        for (int i = 0; i <= 1024; i++) {
            BigInteger bi = new BigInteger(String.valueOf(i));
            String str = bi.toString(36);
            map.put(i, str);

        }
    }


    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        JSONObject jsonObject = JSON.parseObject(value.toString());
        // 获取pid和车道数
        JSONObject properties = jsonObject.getJSONObject("properties");
        int lane_num = properties.getIntValue("lane_num");
        int lane_left = properties.getIntValue("lane_left");
        int lane_right = properties.getIntValue("lane_right");
        int pid = properties.getIntValue("link_pid");
        // base64字符串存储
//        BigInteger bInt = new BigInteger(String.valueOf(pid));
//        String pidstr = bInt.toString(36);
        String pidstr = String.valueOf(pid);
        int laneNum = (int) Math.round((lane_num + lane_left + lane_right) * thresholdOne) + thresholdTwo;
        if (laneNum % 2 == 0) { // 偶数+1，必须为奇数
            laneNum = laneNum + 1;
        }
        if(zoom == 12)
            // 临时解决方案：道路buffer为每边各buffer2个格子
            laneNum = 5;
        // 获取经纬度
        Map<String, StringBuilder> builderMap = new HashMap<>();
        JSONArray jsonArray = jsonObject.getJSONObject("geometry").getJSONArray("coordinates");
        for (int i = 0; i < jsonArray.size() - 1; i++) {
            // 第一个点
            JSONArray arr_1 = (JSONArray) jsonArray.get(i);
            Coordinate coordinate1 = new Coordinate(arr_1.getDoubleValue(0), arr_1.getDoubleValue(1));
            IntCoordinate pixels1 = mercatorUtil.lonLat2Pixels(coordinate1, zoom);
            // 第二个点
            JSONArray arr_2 = (JSONArray) jsonArray.get(i + 1);
            Coordinate coordinate2 = new Coordinate(arr_2.getDoubleValue(0), arr_2.getDoubleValue(1));
            IntCoordinate pixels2 = mercatorUtil.lonLat2Pixels(coordinate2, zoom);
            // 计算直线经过了哪几个像素坐标
            gridOperate(pixels1.x, pixels1.y,
                    pixels2.x, pixels2.y, laneNum, pidstr, builderMap);
        }
        for (Map.Entry<String, StringBuilder> entry : builderMap.entrySet()) {
            keyText.set(entry.getKey());
            valText.set(pidstr + "+" + entry.getValue().toString());
            context.write(keyText, valText);
        }
    }

    /**
     * @param xStart 开始像素点坐标
     * @param yStart 开始像素点坐标
     * @param xEnd   结束像素点坐标
     * @param yEnd   结束像素点坐标
     */
    public void gridOperate(int xStart, int yStart, int xEnd, int yEnd, int laneNum, String pidstr, Map<String, StringBuilder> builderMap) throws IOException, InterruptedException {

        if (xEnd == xStart) {
            if (yStart > yEnd) {
                int tmp = yStart;
                yStart = yEnd;
                yEnd = tmp;
            }
            for (int y = yStart; y <= yEnd; y++) {
                // [xStart, y]
                bufferRoadLon(xStart, y, laneNum, pidstr, builderMap);
            }
            return;
        }

        int tDeltaX = xEnd - xStart;
        int tDeltaY = yEnd - yStart;
        // 斜率
        double k = tDeltaY / (double) tDeltaX;
        if (k > 1 || k < -1) {
            if (yStart > yEnd) {
                int tmp = yStart;
                yStart = yEnd;
                yEnd = tmp;

                xEnd = xStart;
            }
            for (int y = yStart; y <= yEnd; y++) {
                int x = Integer.parseInt(String.valueOf(Math.round(xEnd - (yEnd - y) / k)));
                bufferRoadLon(x, y, laneNum, pidstr, builderMap);
            }
        } else {
            if (xStart > xEnd) {
                int tmp = xStart;
                xStart = xEnd;
                xEnd = tmp;

                yEnd = yStart;
            }
            for (int x = xStart; x <= xEnd; x++) {
                int y = Integer.parseInt(String.valueOf(Math.round(yEnd - (xEnd - x) * k)));
                bufferRoadLat(x, y, laneNum, pidstr, builderMap);
            }
        }
        return;
    }


    private void bufferRoadLon(int x, int y, int bufferLength, String pid, Map<String, StringBuilder> builderMap) throws IOException, InterruptedException {
        for (int i = 0; i < bufferLength; i++) {
            int bufferLen = Math.abs(bufferLength / 2 - i);
            // buffer
            if (i < bufferLength / 2) {
                IntCoordinate pixelCoordinate = new IntCoordinate(x - bufferLen, y);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, bufferLen);
            } else if (i > bufferLength / 2) {
                IntCoordinate pixelCoordinate = new IntCoordinate(x + bufferLen, y);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, bufferLen);
            } else {
                // 核心路网
                IntCoordinate pixelCoordinate = new IntCoordinate(x, y);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, 0);
            }
        }
    }

    private void bufferRoadLat(int x, int y, int bufferLength, String pid, Map<String, StringBuilder> builderMap) throws IOException, InterruptedException {
        for (int i = 0; i < bufferLength; i++) {
            int bufferLen = Math.abs(bufferLength / 2 - i);
            // buffer
            if (i < bufferLength / 2) {
                IntCoordinate pixelCoordinate = new IntCoordinate(x, y - bufferLen);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, bufferLen);
            } else if (i > bufferLength / 2) {
                IntCoordinate pixelCoordinate = new IntCoordinate(x, y + bufferLen);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, bufferLen);
            } else {
                // 核心路网
                IntCoordinate pixelCoordinate = new IntCoordinate(x, y);
                IntCoordinate coordinate = mercatorUtil.pixelsInTile(pixelCoordinate);
                String mCode = mercatorUtil.pixels2MCode(pixelCoordinate);
                setSbuilderMap(mCode, builderMap, coordinate, pid, 0);
            }
        }
    }


    public void setSbuilderMap(String mCode, Map<String, StringBuilder> builderMap, IntCoordinate coordinate, String pid, int bufferLen) {
        String tileKey = new StringBuilder(mCode).reverse().toString();
        StringBuilder sBuilder = builderMap.get(tileKey);
//        BigInteger xInt = new BigInteger(String.valueOf(coordinate.x));
//        String x = xInt.toString(36);
        String x = map.get(coordinate.x);

//        BigInteger yInt = new BigInteger(String.valueOf(coordinate.y));
//        String y = yInt.toString(36);
        String y = map.get(coordinate.y);
        if (sBuilder != null) {
//            sBuilder.append(x).append(",").append(y).append(",").append(pid).append("_").append(bufferLen).append("|");
            sBuilder.append(x).append(",").append(y).append(",").append(bufferLen).append("|");
        } else {
            sBuilder = new StringBuilder();
//            sBuilder.append(x).append(",").append(y).append(",").append(pid).append("_").append(bufferLen).append("|");
            sBuilder.append(x).append(",").append(y).append(",").append(bufferLen).append("|");
            builderMap.put(tileKey, sBuilder);
        }
    }
}
