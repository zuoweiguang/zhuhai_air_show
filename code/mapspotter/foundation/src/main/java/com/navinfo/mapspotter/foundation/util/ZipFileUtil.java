package com.navinfo.mapspotter.foundation.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by SongHuiXing on 7/1 0001.
 */
public class ZipFileUtil {
    public static void zip(String zipFileName, final InputStream in, String filename) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));

        out.putNextEntry(new ZipEntry(filename));

        BufferedOutputStream bo = new BufferedOutputStream(out);

        BufferedInputStream bi = new BufferedInputStream(in);
        int b;
        while ((b = bi.read()) != -1) {
            bo.write(b);
        }
        bi.close();

        bo.close();
        out.close();
    }

    public static void zip(String zipFileName, File inputFile) throws Exception {
        System.out.println("压缩中...");

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));

        BufferedOutputStream bo = new BufferedOutputStream(out);

        zip(out, inputFile, inputFile.getName(), bo);

        bo.close();
        out.close();

        System.out.println("压缩完成");
    }

    private static void zip(ZipOutputStream out, File f, String base,
                     BufferedOutputStream bo) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            if (fl.length == 0) {
                out.putNextEntry(new ZipEntry(base + "/")); // 创建zip压缩进入点base
                System.out.println(base + "/");
            }

            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + "/" + fl[i].getName(), bo); // 递归遍历子文件夹
            }
        } else {
            out.putNextEntry(new ZipEntry(base)); // 创建zip压缩进入点base

            System.out.println(base);
            FileInputStream in = new FileInputStream(f);
            BufferedInputStream bi = new BufferedInputStream(in);
            int b;
            while ((b = bi.read()) != -1) {
                bo.write(b); // 将字节流写入当前zip目录
            }
            bi.close();
            in.close(); // 输入流关闭
        }
    }

    public static boolean upzip(String zipfile, String outputPath){
        try {
            ZipInputStream Zin=new ZipInputStream(new FileInputStream(zipfile));//输入源zip路径
            BufferedInputStream Bin=new BufferedInputStream(Zin);

            File fOut=null;
            ZipEntry entry;
            try {
                while((entry = Zin.getNextEntry())!=null && !entry.isDirectory()){
                    fOut=new File(outputPath,entry.getName());
                    if(!fOut.exists()){
                        (new File(fOut.getParent())).mkdirs();
                    }
                    FileOutputStream out=new FileOutputStream(fOut);
                    BufferedOutputStream Bout=new BufferedOutputStream(out);
                    int b;
                    while((b=Bin.read())!=-1){
                        Bout.write(b);
                    }
                    Bout.close();
                    out.close();
                    System.out.println(fOut+"解压成功");
                }
                Bin.close();
                Zin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
