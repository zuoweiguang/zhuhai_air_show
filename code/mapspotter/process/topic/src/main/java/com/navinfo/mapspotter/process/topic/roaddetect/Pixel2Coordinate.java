package com.navinfo.mapspotter.process.topic.roaddetect;

import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by cuiliang on 2016/2/24.
 * 瓦片内像素坐标转成实际坐标
 */
public class Pixel2Coordinate {

    String inputRoot;
    String outputRoot;

    public Pixel2Coordinate(String inputRoot, String outputRoot) {
        this.inputRoot = inputRoot;
        this.outputRoot = outputRoot;
    }

    public List<String> getFileList() {
        List<String> list = new ArrayList<String>();
        File rootFile = new File(inputRoot);
        this.getFile(rootFile, list);
        return list;
    }

    /**
     * 读瓦片文件，转换坐标，写坐标文件
     * @param fileName
     * @throws Exception
     */
    public void readAndWriteFile(String fileName) throws Exception {
        File file = new File(fileName);
        String code = file.getName().split("\\.")[0];
        FileReader fr = new FileReader(fileName);
        BufferedReader bufferedreader = new BufferedReader(fr);

        String outPath = file.getAbsolutePath().replace(inputRoot, "")
                .replace(file.getName(), "");

        File outputDir = new File(outputRoot + outPath);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String outputFile = outputRoot + outPath + file.getName();
        FileWriter fw = new FileWriter(new File(outputFile));
        BufferedWriter bw = new BufferedWriter(fw);

        String line;
        int y = 0;
        while ((line = bufferedreader.readLine()) != null) {
            if (0 != line.trim().length()) {
                String[] pixel = line.split("\t");
                for (int x = 0; x < pixel.length; x++) {
                    if (!"0".equals(pixel[x])) {
                        //System.out.println(code + " " + x + " " + y);
                        String coord = pixel2CoordinateStr(code, x, y);
                        bw.write(coord);
                        bw.newLine();
                    }
                }
            }
            y++;
        }
        bw.close();
        fw.close();
        fr.close();
    }

    int level = 12;
    MercatorUtil mkt = new MercatorUtil(1024, level);

    public String pixel2CoordinateStr(String code, int x, int y) {
        IntCoordinate pixel = mkt.inTile2Pixels(x, y, code);
        Coordinate coord = mkt.pixels2LonLat(pixel);

        DecimalFormat df = new DecimalFormat("#.#####");
        return df.format(coord.x) + "," + df.format(coord.y);
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
//            Pixel2Coordinate p2c = new Pixel2Coordinate(
//                    "E:\\fusion\\road\\emergency\\baidu_output_20160318\\detect",
//                    "E:\\fusion\\road\\emergency\\baidu_output_20160318\\

            String input = args[0];
            String output = args[1];
            int thread_num = Integer.parseInt(args[2]);
            Pixel2Coordinate p2c = new Pixel2Coordinate(input, output);

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
        public MultThreadCallable(Pixel2Coordinate p2c, String file){
            this.p2c = p2c;
            this.file = file;
        }

        public String call() throws IOException  {
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
