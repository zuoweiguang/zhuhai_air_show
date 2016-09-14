package com.navinfo.mapspotter.process.topic.roaddetect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiliang on 2016/2/23.
 */
public class Tools {

    public static List<String> ReadFileToList(String filename){
        {
            List<String> list = new ArrayList<>();
            BufferedReader br = null;
            try{
                File file = new File(filename);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(
                        file)));
                String line = null;
                while((line = br.readLine()) != null){
                    if(line!=null&&!"".equals(line.trim()))
                        list.add(line.trim());
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return list;
        }
    }

    public static Map<String,String> ReadFileToMap(String filename){
        {
            Map<String,String> map = new HashMap<>();
            BufferedReader br = null;
            try{
                File file = new File(filename);
                br = new BufferedReader(new InputStreamReader(new FileInputStream(
                        file)));
                String line = null;
                while((line = br.readLine()) != null){
                    if(line!=null&&!"".equals(line.trim()))
                        map.put(line.trim(),"1");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return map;
        }
    }

    public static void main(String[] args){
        Map<String,String> beijingMap = Tools.ReadFileToMap("E:\\fusion\\road\\beijing.txt");
        List<String> outputList = Tools.ReadFileToList("C:\\Users\\cuiliang.NAVINFO\\Desktop\\sogou_output_20160223\\part-r-00000");

        for(String output:outputList){
            boolean flag = false;
            String exist = beijingMap.get(output);
            if(exist != null){
                System.out.println(output);
            }
        }
    }

}
