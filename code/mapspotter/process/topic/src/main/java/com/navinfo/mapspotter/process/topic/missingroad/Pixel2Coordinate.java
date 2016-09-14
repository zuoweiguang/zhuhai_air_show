package com.navinfo.mapspotter.process.topic.missingroad;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.navinfo.mapspotter.process.analysis.poistat.BlockInfo;
import com.navinfo.mapspotter.process.analysis.poistat.BlocksAnalysis;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by cuiliang on 2016/2/24.
 * 瓦片内像素坐标转成实际坐标
 */
public class Pixel2Coordinate {

    private String inputRoot;
    private String outputRoot;

    private BlocksAnalysis analysis;

    public Pixel2Coordinate(String inputRoot, String outputRoot, String configFile) throws IOException {
        this.inputRoot = inputRoot;
        this.outputRoot = outputRoot;

        analysis = new BlocksAnalysis();
        analysis.prepareRTree_Json(configFile, 0);

        File outputDir = new File(outputRoot);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    public List<String> getFileList() {
        List<String> list = new ArrayList<String>();
        File rootFile = new File(inputRoot);
        this.getFile(rootFile, list);
        return list;
    }

    /**
     * 读瓦片文件，转换坐标，写坐标文件
     *
     * @param fileName
     * @throws Exception
     */
    public void readAndWriteFile(String fileName) throws Exception {
        if (fileName.endsWith("txt")) {
            File file = new File(fileName);
            String code = file.getName().split("\\.")[0];

            FileReader confidence_fr = new FileReader(file.getParent() + File.separator + code + "." + "confidence");
            BufferedReader confidence_br = new BufferedReader(confidence_fr);
            String confidence_line;

            Map<String, Double> confMap = new HashMap();

            while ((confidence_line = confidence_br.readLine()) != null) {
                JSONArray array = JSONArray.parseArray(confidence_line);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = (JSONObject) array.get(i);
                    confMap.put(json.getInteger("key").toString(), json.getDouble("confidence"));
                }
            }
            confidence_br.close();
            confidence_fr.close();

            String outputFile = outputRoot + File.separator + file.getName();
            FileWriter fw = new FileWriter(new File(outputFile));

            BufferedWriter bw = new BufferedWriter(fw);

            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            String line;
            int y = 0;
            while ((line = br.readLine()) != null) {
                if (0 != line.trim().length()) {
                    String[] pixel = line.split("\t");
                    for (int x = 0; x < pixel.length; x++) {
                        if (!"0".equals(pixel[x])) {
                            String key = pixel[x];
                            String coord = pixel2CoordinateStr(code, x, y);
                            if (coord != null) {
                                bw.write(coord + "," + key + "," + confMap.get(key));
                                bw.newLine();
                            }
                        }
                    }
                }
                y++;
            }
            bw.close();
            fw.close();
            br.close();
            fr.close();
        }
    }

    int level = 12;
    MercatorUtil mkt = new MercatorUtil(1024, level);

    public String pixel2CoordinateStr(String code, int x, int y) {
        DecimalFormat df = new DecimalFormat("#.#####");
        IntCoordinate pixel = mkt.inTile2Pixels(x, y, code);
        Coordinate coord = mkt.pixels2LonLat(pixel);
        Geometry geometry = GeoUtil.createPoint(coord.x, coord.y);
        List<BlockInfo> list = analysis.Contains(coord, geometry);
        if (list != null && list.size() != 0) {
            BlockInfo info = list.get(0);
            //System.out.println("+++++++++  x:" + x + " y:" + y + " blockid:" + info.getBlockid() + " ++++++++++");
            return null;
        } else {
            return df.format(coord.x) + "," + df.format(coord.y);
        }
    }


    public void getFile(File file, List<String> list) {
        File flist[] = file.listFiles();
        if (flist == null || flist.length == 0) {
            return;
        }
        for (File f : flist) {
            if (f.isDirectory()) {
                getFile(f, list);
            } else {
                list.add(f.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        try {
            String input = args[0];
            String output = args[1];
            String config = args[2];
            int thread_num = Integer.parseInt(args[3]);
            Pixel2Coordinate p2c = new Pixel2Coordinate(input, output, config);

            ExecutorService exs = Executors.newFixedThreadPool(thread_num);
            ArrayList<Future<String>> al = new ArrayList();
            for (String fileName : p2c.getFileList()) {
                al.add(exs.submit(new MultThreadCallable(p2c, fileName)));
            }
            // 关闭启动线程
            exs.shutdown();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class MultThreadCallable implements Callable<String> {

        private Pixel2Coordinate p2c;
        private String file;

        public MultThreadCallable(Pixel2Coordinate p2c, String file) {
            this.p2c = p2c;
            this.file = file;
        }

        public String call() throws IOException {
            // TODO Auto-generated method stub
            try {
                p2c.readAndWriteFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
