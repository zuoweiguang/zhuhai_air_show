package com.navinfo.mapspotter.process.topic.construction;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ZhangJin1207 on 2016/1/28.
 */
public class ConstructionBase {

    public static Configuration getHBaseConf(String strConf){
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum",strConf); //"datanode01:2181,datanode02:2181,datanode03:2181");
        //conf.set("hbase.zookeeper.quorum" , "Master.Hadoop:2181");
        //conf.set("hbase.master","Slave3.Hadoop");
        //conf.set("hbase.master.port" , "6000");
        //conf.set("hbase.zookeeper.property.clientPort","2181");
        //conf.set("hbase.client.scanner.timeout.period","60000");
        //conf.set("hbase.client.write.buffer","8388608");
        return conf;
    }

    public static void DrawImage(String strFile , int[][] maxtri){
        if (maxtri.length == 0){
            return;
        }
        try {
            BufferedImage bimage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_BGR);

            for (int i = 0; i < maxtri.length; i++) {
                for (int j = 0; j < maxtri.length; j++) {
                    if (maxtri[i][j] != 0) {
                        bimage.setRGB(j , i , 200);
                    } else {
                        bimage.setRGB(j , i , 0);
                    }
                }
            }

            ImageIO.write(bimage, "jpg", new File(strFile));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void exportTXT(String fileName , int[][] maxtri){
        try{
            File file = new File(fileName);
            if (!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file , true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0 ; i < maxtri.length ; i++){
                String strLine = "";
                for (int j = 0 ; j < maxtri[0].length ; j++){
                    strLine += maxtri[i][j];
                    if (j < maxtri.length - 1){
                        strLine += "\t";
                    }
                }
                strLine += "\n";
                bufferedWriter.write(strLine);
            }

            bufferedWriter.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
