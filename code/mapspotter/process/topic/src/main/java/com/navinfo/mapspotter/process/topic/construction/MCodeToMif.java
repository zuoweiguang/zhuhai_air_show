package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.vividsolutions.jts.geom.Envelope;

import java.io.*;

/**
 * Created by ZhangJin1207 on 2016/3/23.
 */
public class MCodeToMif {

    public int ConvertPath(String inPath , String outPath , int iLevel){
        if (inPath.isEmpty() || outPath.isEmpty()){
            return -1;
        }

        try{
            File ifile = new File(inPath);
            if (!ifile.exists()){
                return -1;
            }
            File[] files = ifile.listFiles();
            for (File file : files){
                if (file.isDirectory()){
                    ConvertPath(file.getAbsolutePath() , outPath , iLevel);
                }else{
                    ConvertFile(file.getAbsolutePath() , outPath , iLevel);
                }
            }
            return 0;

        }catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }
    public int ConvertFile(String inputFile , String outPath , int iLevel){
        if (inputFile.isEmpty() || outPath.isEmpty())
        {
            return -1;
        }
        try {

            File ifile = new File(inputFile);
            if (!ifile.exists()){
            return -1;
            }

            String  sName = ifile.getName();
            sName = sName.substring(0 , sName.lastIndexOf("."));
            String strMif = outPath + sName + ".mif";
            String strMid = outPath + sName + ".mid";

            File miffile = new File(strMif);
            if (!miffile.exists()){
            miffile.createNewFile();
            }

            BufferedWriter mifWriter = new BufferedWriter(new FileWriter(miffile , true));

            mifWriter.write("Version 300\n");
            mifWriter.write("Charset \"WindowsSimpChinese\"\n");
            mifWriter.write("Delimiter \",\"\n");
            mifWriter.write("Index 1 CoordSys Earth Projection 1, 0\n");
            mifWriter.write("Columns 1\n");
            mifWriter.write("MeshID char(30)\n");
            mifWriter.write("Data\n\n");

            File midfile = new File(strMid);
            if (!midfile.exists()){
                midfile.createNewFile();
            }

            BufferedWriter midWriter = new BufferedWriter(new FileWriter(midfile , true));

            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(ifile));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String strLine = null;
            while((strLine = bufferedReader.readLine()) != null){
                strLine = strLine.trim();
                String[] datas = strLine.split(",");
                String mcode = datas[0];
                Envelope env = MercatorUtil.mercatorBound(mcode , iLevel);
                String strout = "Rect " + env.getMinX() + " " + env.getMinY() + " " + env.getMaxX() + " " + env.getMaxY() + "\n";
                mifWriter.write(strout);
                mifWriter.write("    Pen (1,2,0)\n");
                mifWriter.write("    Brush (2,16777215,16777215) \n");

                midWriter.write(mcode + "\n");
            }

            bufferedReader.close();
            mifWriter.close();
            midWriter.close();

            return 0;
        }catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    public static void main(String[] args) throws Exception{
        MCodeToMif mCodeToMif = new MCodeToMif();
        mCodeToMif.ConvertFile("D:\\mapspotter\\source\\MapSpotter\\code\\mapspotter\\out\\artifacts\\tile_list\\xiningshi.txt","D:\\mapspotter\\source\\MapSpotter\\code\\mapspotter\\out\\artifacts\\tile_list\\",12);
    }
}
