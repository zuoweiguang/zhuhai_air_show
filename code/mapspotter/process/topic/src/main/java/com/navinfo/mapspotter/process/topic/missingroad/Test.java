package com.navinfo.mapspotter.process.topic.missingroad;

import com.navinfo.mapspotter.foundation.util.MatrixUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cuiliang on 2016/7/12.
 */
public class Test {

    public static void main(String[] args){

        File file = new File("E:\\fusion\\road\\yinchuanshi1\\didi\\3254_1573.txt");
        //File file = new File("E:\\fusion\\road\\data\\chengdu\\didi\\3233_1679.txt");


        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            List<List<String>> l = new ArrayList();
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                String[] array = tempString.split("\t");

                List<String> list = Arrays.asList(array);
                l.add(list);
            }
            reader.close();
            MatrixUtil.array2Image(l, "E:\\fusion\\32541573.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }


    }
}
