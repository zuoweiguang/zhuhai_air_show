package com.navinfo.mapspotter.process.topic.construction;

import java.io.*;
import java.util.*;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.*;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.operation.valid.SimpleNestedRingTester;
import org.apache.hadoop.hbase.client.*;

/**
 * Created by ZhangJin1207 on 2016/1/24.
 */
public class ConstructionAnalysis {
    private final Logger logger = Logger.getLogger(ConstructionAnalysis.class);
    private MercatorUtil mutil = new MercatorUtil(256, 12);
    private RoadAnalysisInfo roadinfo = null;
    private Table hTable = null;
    private  byte[] ROAD_FAMILY = "road".getBytes();
    private  byte[] ROAD_QUALIFIER = "data".getBytes();
    private  byte[] SOURCE_FAMILY = "source".getBytes();
    private  byte[] SOURCE_QUALIFIER = "didi".getBytes();
    private SerializeUtil<int[][]> Su = new SerializeUtil<int[][]>();
    private SerializeUtil<Integer[][]> SuI = new SerializeUtil<>();
    private String tableName = "road_detect_12";
    private int iTileLevel = 12;
    private int iPixelSize = 1024;
    private List<RoadAnalysisInfo> AnalysisResults = new ArrayList<RoadAnalysisInfo>();
    public ConstructionAnalysis(){}

    private long LinkHitCount = 0;
    private BufferedWriter bufferedWriter = null;
    private String resultfile = "D:\\mapspotter\\data\\Result_sogou_Q.txt";

    private Map<String , List<Integer>> mcode2link = new HashMap<>();

    public void setROAD_FAMILY(String family){ROAD_FAMILY = family.getBytes();}
    public void setROAD_QUALIFIER(String qualifier){ROAD_QUALIFIER = qualifier.getBytes();}
    public void setTableName(String tableName){this.tableName = tableName;}
    public boolean connHTable(String strTableName , String strConf){
        try{
            Hbase hbase = Hbase.createWithConfiguration(ConstructionBase.getHBaseConf(strConf));
            hTable = hbase.getTable(strTableName);
        }catch(Exception e){
            logger.error("ConnHTable Error -->" , e);
            return false;
        }
        return true;
    }

    public Result queryHBase(Table table , String rowkey , byte[] family , byte[] qualifier){
        Result result = null;
        try{
            Get get = new Get(rowkey.getBytes());
            get.addColumn(family , qualifier);
            result = table.get(get);
        }catch (Exception e){
            logger.error("queryHBase Error -->" , e);
            return null;
        }

        return result;
    }

    public Result queryHBase(String rowkey){
        Result result = null;
        try{
            Get get = new Get(rowkey.getBytes());
            get.addColumn(ROAD_FAMILY ,ROAD_QUALIFIER);
            get.addColumn(SOURCE_FAMILY , SOURCE_QUALIFIER);
            result = hTable.get(get);
        }catch (IOException e){
            logger.error("queryHBase Error -->" , e);
        }
        return result;
    }
    public int dealBasePath(String strFilePath){
        try{
            if (!connHTable(tableName,"Master.Hadoop:2181")){
                logger.error("HBase Connect Error");
                return 0;
            }

            File root = new File(strFilePath);
            File[] files = root.listFiles();
            for (File file : files){
                if (file.isDirectory()) {
                    dealBasePath(file.getAbsolutePath());
                }else{
                    readBaseRoad(file.getAbsolutePath());
                }
            }
        }catch (Exception e){
            logger.error("dealBasePath Error -->" , e);
            return 0;
        }
        return 1;
    }
    public int readBaseRoad(String strFileName){
        InputStreamReader fileReader = null;
        //if (!connHTable(tableName)){
        //    return 0;
        //}
        try{
            File brfile = new File(strFileName);
            if (!brfile.exists() || !brfile.isFile()){
                logger.error(strFileName + "文件不存在或文件非法!");
                return 0;
            }
            System.out.print("开始处理文件-->" + strFileName + "\n");

            fileReader = new InputStreamReader(new FileInputStream(brfile));
            BufferedReader bReader = new BufferedReader(fileReader);
            String strLine = null;
            while((strLine = bReader.readLine()) != null){
                if (0 == praseLine(strLine , iTileLevel , iPixelSize)){
                    logger.error(strLine + "Prase Error");
                }
            }
        }catch (IOException e){
            logger.error("readBaseRoad Error -->" , e);
            return 0;
        }finally {
            try {
                fileReader.close();
            }catch (IOException e){
                logger.error("readBaseRoad Error -->" , e);
                return 0;
            }
        }
        return 1;
    }

    public int praseLine(String strLine , int nLevel , int nTitleSize){
        if (null == strLine || strLine.isEmpty()){
            return 0;
        }

        //BaseRoadModle.TestParseJson(strLine);
        //BaseRoadModle baseRoadModle = BaseRoadModle.PraseJsonStr(strLine);
        BaseRoadJsonModle baseModle = BaseRoadJsonModle.PraseRoadJson(strLine);
        double[] GeoCoordinates = baseModle.getCoordinates();
        Map<String , int[]> mcode2index = new HashMap<>();
        roadinfo = new RoadAnalysisInfo();
        roadinfo.setPid(baseModle.getPID());
        roadinfo.setFunction_class(baseModle.getFuntion_Class());
        for (int i = 0; i < GeoCoordinates.length; i += 2){
            double dLon = GeoCoordinates[i];
            double dLat = GeoCoordinates[i+1];

            IntCoordinate indexpixel = mutil.lonLat2Pixels(new Coordinate(dLon , dLat) , nLevel + (int)Math.sqrt(nTitleSize/256));

            int[] index = new int[]{indexpixel.x % nTitleSize , indexpixel.y % nTitleSize};

            String mcode = mutil.lonLat2MCode(new Coordinate(dLon , dLat));
            StringBuffer sbuffer = new StringBuffer(mcode);
            StringBuffer nsbuffer = sbuffer.reverse();
            String mrcode = nsbuffer.toString();
            if (mcode2link.containsKey(mcode)){
                List<Integer> list = mcode2link.get(mcode);
                if (!list.contains(baseModle.getPID())){
                    list.add(baseModle.getPID());
                }
            }
            else{
                List<Integer> list = new ArrayList<>();
                list.add(baseModle.getPID());
                mcode2link.put(mcode , list);
            }
            if (mcode2index.containsKey(mrcode)){
                int[] values = mcode2index.get(mrcode);
                values[0] = Math.min(values[0] , index[0]);
                values[1] = Math.min(values[1] , index[1]);
                values[2] = Math.max(values[2] , index[0]);
                values[3] = Math.max(values[3] , index[1]);
            }
            else{
                int[] indexs = new int[]{index[0] , index[1] , index[0] , index[1]};
                mcode2index.put(mrcode , indexs);
            }
        }
        roadinfo.setLinkMCodes(mcode2index);
        return analisysData(mcode2index , baseModle.getPID());
    }

    public int analisysData(Map<String , int[]> mcode2Pid , int linkPid){
        if (mcode2Pid.isEmpty()){
            return 0;
        }
        try {
            List<int[][]> linkMaxtris = new ArrayList<int[][]>();
            List<Integer[][]> trackMaxtris = new ArrayList<>();
            int i = 0;
            for (Map.Entry<String, int[]> entry : mcode2Pid.entrySet()) {
                String key = entry.getKey();
                StringBuilder sBuilder = new StringBuilder(key);
                String code = sBuilder.reverse().toString();
                int[] index = entry.getValue().clone();

                Result rt = queryHBase(key);
                if (null == rt || rt.isEmpty()) {
                    continue;
                }

                byte[] roads = rt.getValue(ROAD_FAMILY, ROAD_QUALIFIER);
                byte[] tracks = rt.getValue(SOURCE_FAMILY, SOURCE_QUALIFIER);

                int[][] iroad = Su.deserialize(roads);
                DoubleMatrix dMatrix = new DoubleMatrix(iroad[0], iroad[1], iroad[2]);
                int[][] roadM = dMatrix.toIntArray2();

                Integer[][] trackMaxtri = null; //convertTxt2Maxtri("D:\\mapspotter\\data\\didi\\" + code + ".txt");
                if (tracks == null || tracks.length == 0) {
                    trackMaxtri = new Integer[1024][1024];
                }else {
                    trackMaxtri = SuI.deserialize(tracks);
                }
                int[][] blinkMaxtri = roadinfo.GetMaxtriByPID(roadM, linkPid);
                linkMaxtris.add(blinkMaxtri);
                trackMaxtris.add(trackMaxtri);
            }

            roadinfo.setOriginalMaxtris(linkMaxtris);
            roadinfo.setTrackMaxtris(trackMaxtris);

            roadinfo.analysis(5 , "sogou");

            String strR = null;
            String strRatio = String.format("%.3f",roadinfo.getdHitRatio());
            if ((roadinfo.getLinkOverArea() <= 5 && roadinfo.getdHitRatio() == 1) ||
                    (roadinfo.getLinkOverArea() > 5 && roadinfo.getdHitRatio() > 2.0/3.0)){
                //疑是开通
                strR = "" + roadinfo.getPid() + "\t" + strRatio + "\t" + 1 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\n";
            }else if (roadinfo.getLinkOverArea() == 0){
                //基础路网数据中没有
                strR = "" + roadinfo.getPid() + "\t" + strRatio + "\t" + -1 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\n";
            }else{
                //还处于施工中
                strR = "" + roadinfo.getPid() + "\t" + strRatio + "\t" + 0 + "\t" + roadinfo.getLinkOverArea() + "\t"  + roadinfo.getTrackHitArea() + "\n";
            }

            bufferedWriter.write(strR);

            //if ((roadinfo.getLinkOverArea() < 5 && roadinfo.getdHitRatio() == 1) ||
            //   (roadinfo.getLinkOverArea() >= 5 && roadinfo.getdHitRatio() > 0.5))
            //{
            //    strR = "" + roadinfo.getPid() + "\t" + roadinfo.getLinkOverArea() + "\t" + roadinfo.getTrackHitArea() + "\t" + roadinfo.getdHitRatio() + "\r\n";
            //    bufferedWriter.write(strR);
            //}
        }catch (IOException e){
            e.printStackTrace();
        }
        return 1;
    }

    public Integer[][] MaxtriTranspose(Integer[][] maxtri){
        Integer[][] rMaxtri = new Integer[maxtri.length][maxtri.length];
        for (int i = 0 ; i < maxtri.length ; i++){
            for (int j = 0 ; j < maxtri.length ; j++){
                rMaxtri[j][i] = maxtri[i][j];
            }
        }

        return rMaxtri;
    }
    public Integer[][] MaxtriExtend(Integer[][] maxtri , int iextends){
        Integer[][] rmaxtri = new Integer[maxtri.length][maxtri.length];
        for (int i = 0 ; i < maxtri.length ; i ++){
            for (int j = 0 ; j < maxtri.length ; j ++){
                if (maxtri[i][j] != null){
                    rmaxtri[i][j] = maxtri[i][j];
                    fillMaxtri(rmaxtri , i , j , iextends);
                }
            }
        }
        return rmaxtri;
    }

    public void fillMaxtri(Integer[][] maxtri , int i , int j , int iextends){
        Integer value = maxtri[i][j];
        for (int a = i - iextends ; a <= i + iextends ; a++){
            for (int b = j - iextends ; b <= j + iextends; b++)
            {
                if (a >= 0 && a < 1024 && b >= 0 && b < 1024){
                    maxtri[a][b] = value;
                }
            }
        }
    }

    public Map<String , String> praseLine(String strLine){
        if (null == strLine || strLine.isEmpty()){
            return null;
        }

        BaseRoadJsonModle baseModle = BaseRoadJsonModle.PraseRoadJson(strLine);
        double[] GeoCoordinates = baseModle.getCoordinates();
        Map<String , String> mcode2Pid= new HashMap<>();
        for (int i = 0; i < GeoCoordinates.length; i += 2){
            double dLon = GeoCoordinates[i];
            double dLat = GeoCoordinates[i+1];

            String mcode = mutil.lonLat2MCode(new Coordinate(dLon , dLat));
            StringBuffer sbuffer = new StringBuffer(mcode);
            StringBuffer nsbuffer = sbuffer.reverse();
            String mrcode = nsbuffer.toString();
            mcode2Pid.put(mrcode , String.valueOf(baseModle.getPID()));
        }
        return mcode2Pid;
    }

    public void createResult(){
        try{
            File file = new File(resultfile);
            if (!file.exists()){
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file , true);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("Link_PID\tOverRatio\tState\tLink_PN\tTrack_PN\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeResult(){
        try{
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void disconnHbase(){
        try{
        if (hTable != null){
            hTable.close();
        }}catch (IOException e){
            e.printStackTrace();
        }
    }

    public  void outputMap(){
        if (mcode2link.isEmpty()){
            return;
        }
        try {
            String strPath = "D:\\mapspotter\\data\\code2link.txt";
            File file = new File(strPath);
            if (!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(strPath, true);
            BufferedWriter bWrite = new BufferedWriter(fileWriter);

            for (Map.Entry<String, List<Integer>> val : mcode2link.entrySet()) {
                String mcode = val.getKey();
                List<Integer> list = val.getValue();
                String sLine = mcode + "\t";
                for (int i = 0; i < list.size(); i++) {
                    sLine += list.get(i);
                    if (i < list.size() - 1) {
                        sLine += ",";
                    }
                }

                bWrite.write(sLine + "\n");
            }
            bWrite.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void outPutTextResult(){
        if (mcode2link.isEmpty()){
            return;
        }
        String strPath = "D:\\mapspotter\\data";
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        for (Map.Entry<String , List<Integer>> val : mcode2link.entrySet()){
            String mcode = val.getKey();
            List<Integer> pids = val.getValue();
            StringBuffer sbuffer = new StringBuffer(mcode);
            Result rt = queryHBase(sbuffer.reverse().toString());

            byte[] roads = rt.getValue(ROAD_FAMILY , ROAD_QUALIFIER);
            byte[] tracks = rt.getValue(SOURCE_FAMILY , SOURCE_QUALIFIER);

            int[][] iroad = Su.deserialize(roads);
            DoubleMatrix dMaxtri = new DoubleMatrix(iroad[0] , iroad[1] , iroad[2]);
            int[][] droad = dMaxtri.toIntArray2();
            ConstructionBase.DrawImage(strPath + "\\road_img_Q\\" + mcode + ".jpg" , droad);

            ConstructionBase.exportTXT(strPath + "\\road_Q\\" + mcode + ".txt", droad);

            int[][] filterMaxtr = filterMaxtri(droad , pids);
            ConstructionBase.exportTXT(strPath + "\\road_pid_Q\\" + mcode + ".txt" , filterMaxtr);
            ConstructionBase.DrawImage(strPath + "\\road_imgF_Q\\" + mcode + ".jpg" , filterMaxtr);
            if (tracks != null) {
                Integer[][] trackMaxtri = SuI.deserialize(tracks);

                Integer[][] FileterM = imageAlgorithm.medianFilter(trackMaxtri , 3);
                Integer[][] UseMaxtri = imageAlgorithm.filterLessThanPara(FileterM , 5);

                int[][] trackm = new int[1024][1024];
                int[][] trackF = new int[1024][1024];
                for (int m = 0; m < trackMaxtri.length; m++) {
                    for (int n = 0; n < trackMaxtri.length; n++) {
                        if (UseMaxtri[m][n] == null) {
                            trackm[m][n] = 0;
                        } else {
                            trackm[m][n] = UseMaxtri[m][n];
                        }

                        if (trackMaxtri[m][n] == null){
                            trackF[m][n] = 0;
                        }else{
                            trackF[m][n] = trackMaxtri[m][n];
                        }
                    }
                }
                ConstructionBase.DrawImage(strPath + "\\track_img_Q\\" + mcode + ".jpg" , trackF);
                ConstructionBase.DrawImage(strPath + "\\track_imgF_Q\\" + mcode + ".jpg" , trackm);
                ConstructionBase.exportTXT(strPath + "\\track_Q\\" + mcode + ".txt" , trackm);
            }
        }
    }

    public int[][] filterMaxtri(int[][] maxtri , List<Integer> linkPids){
        if (maxtri.length == 0){
            return null;
        }

        int[][] result = new int[maxtri.length][maxtri[0].length];

        for (int i = 0 ; i < maxtri.length ; i++){
            for (int j = 0 ; j < maxtri.length ; j++){
                if (linkPids.contains(maxtri[i][j])){
                    result[i][j] = maxtri[i][j];
                }else{
                    result[i][j] = 0;
                }
            }
        }
        return result;
    }
    public void testHBase(){
        try {
            connHTable("road_raster" , "Master.Hadoop:2181");
            Result tt = queryHBase(hTable , "6085_19041" , "16sum03".getBytes() , "road".getBytes());
            byte[] tracks = tt.getValue("16sum03".getBytes(), "road".getBytes());

            connHTable("road_construction_12" , "Master.Hadoop:2181");
            Result tp = queryHBase(hTable , "4551_9633" , ROAD_FAMILY , ROAD_QUALIFIER);
            byte[] roadx = tp.getValue(ROAD_FAMILY , ROAD_QUALIFIER);


            Scan scan = new Scan();
            scan.setCaching(2000);
            scan.setCacheBlocks(true);
            ResultScanner results = hTable.getScanner(scan);
            SerializeUtil<int[][]> su = new SerializeUtil<>();
            long tilecount = 0;
            for (Result rt : results){
                byte[] road = rt.getValue(ROAD_FAMILY, ROAD_QUALIFIER);
                byte[] source = rt.getValue(SOURCE_FAMILY , SOURCE_QUALIFIER);
                tilecount++;
                if (road == null || source == null)
                {
                    continue;
                }

                int[][] rmaxtri = su.deserialize(road);
                DoubleMatrix drmaxtri = new DoubleMatrix(rmaxtri[0] , rmaxtri[1] , rmaxtri[2]);
                int[][] lmaxtri = drmaxtri.toIntArray2();

                int[][] smaxtri = su.deserialize(source);

            }

            System.out.println(tilecount);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Integer[][] convertTxt2Maxtri(String strFile){
        if (strFile.isEmpty()){
            return  null;
        }
        Integer[][] maxtri = new Integer[1024][1024];
        try {
            File brfile = new File(strFile);
            if (!brfile.exists() || !brfile.isFile()) {
                System.out.println(strFile + "文件不存在或文件非法!");
                return null;
            }
            System.out.print("开始处理文件-->" + strFile + "\n");

            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(brfile));
            BufferedReader bReader = new BufferedReader(fileReader);
            String strLine = null;
            int i = 0;
            while ((strLine = bReader.readLine()) != null) {
                if (strLine == null || strLine == ""){
                    continue;
                }
                String[] strs = strLine.split("\\t");
                if (strs.length < 1024){
                    continue;
                }
                for (int j = 0 ; j < strs.length; j++){

                    if (strs[j] == null){
                        maxtri[i][j] = null;
                    }else{
                        int value = Integer.parseInt(strs[j]);
                        if (value == 0){
                            maxtri[i][j] = null;
                        }else {
                            maxtri[i][j] = value;
                        }
                    }
                }
                i++;
            }
        }catch (IOException e){
            System.out.println(e);
        }

        return maxtri;
    }

    public void outputinfo(String strFile , String strpath){
        if (strFile== null || strFile.isEmpty()){
            return;
        }
        Map<String , List<Integer>> mcode2link = new HashMap<String , List<Integer>>();
        try {
            File file = new File(strFile);
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String strLine = "";

            while((strLine = bufferedReader.readLine()) != null){
                String strTmp = strLine.replace("\"" , "");
                String[] strS = strTmp.split("\t");
                if (strS.length != 2){
                    continue;
                }
                String strLink = strS[0];
                int link_pid = Integer.valueOf(strLink).intValue();
                String[] strM = strS[1].split("\\|");
                for (String mcode : strM){
                    if (mcode2link.containsKey(mcode)){
                        mcode2link.get(mcode).add(link_pid);
                    }
                    else{
                        List<Integer> list = new ArrayList<>();
                        list.add(link_pid);
                        mcode2link.put(mcode , list);
                    }
                }
            }

            bufferedReader.close();

            for (Map.Entry val : mcode2link.entrySet()){
                String mcode = val.getKey().toString();
                List<Integer> pids = (List<Integer>) val.getValue();
                StringBuffer sbuffer = new StringBuffer(mcode);
                export_mcode_info(strpath , sbuffer.reverse().toString() , pids);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ConvertTxt(String strPath){
        File root = new File(strPath);
        File[] files = root.listFiles();
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        for (File file : files){
            if (file.isDirectory()) {
                ConvertTxt(file.getAbsolutePath());
            }else{
                Integer[][] maxtri = convertTxt2Maxtri(file.getAbsolutePath());
                Integer[][] mfmaxtri = imageAlgorithm.medianFilter(maxtri , 3);
                Integer[][] fmaxtri = imageAlgorithm.filterLessThanPara(maxtri , 5);
                int[][] umaxtri = new int[fmaxtri.length][fmaxtri.length];
                for (int i = 0 ; i < fmaxtri.length; i++){
                    for (int j = 0 ; j < fmaxtri.length; j++){
                        if (fmaxtri[i][j] == null){
                            umaxtri[i][j] = 0;
                        }else{
                            umaxtri[i][j] = fmaxtri[i][j];
                        }
                    }
                }
                //ConstructionBase.exportTXT("D:\\mapspotter\\data\\didi\\" + file.getName()  , maxtri);
                ConstructionBase.DrawImage("D:\\mapspotter\\data\\didi_imgF\\" + file.getName().substring(0,file.getName().indexOf('.')) + ".jpg",umaxtri);
            }
        }
    }

    public void export_mcodes(String strPath , String mcodes , String pidList) throws Exception{
        List<Integer> pids = new ArrayList<>();

        System.out.println("pidList----" + pidList);
        System.out.println("mcodes----" + mcodes);
        //File file = new File(pidList);
        //if (!file.exists()){
        //    System.out.println(pidList + "  pidlist is not exists!");
        //    return;
        //}

        //InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file));
        //BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strLine = null;

        //while ((strLine = bufferedReader.readLine()) != null){
        //   strLine = strLine.trim();
        //    int pid = Integer.parseInt(strLine);
        //    pids.add(pid);
        //}

        //bufferedReader.close();


        File mfile = new File(mcodes);
        if (!mfile.exists()){
            System.out.println(mcodes + "  mcodes is not exists!");
            return;
        }

        InputStreamReader inputStreamReader1 = new InputStreamReader(new FileInputStream(mfile));
        BufferedReader bufferedReader1 = new BufferedReader(inputStreamReader1);

        while ((strLine = bufferedReader1.readLine()) != null){
            strLine = strLine.trim();
            System.out.println("export " + strLine + "start");
            export_mcode_info(strPath , strLine , pids);
        }

        bufferedReader1.close();
    }
    public void export_mcode_info(String strPath , String mcode , List<Integer> pids){
        if (mcode == null || mcode.isEmpty()){
            return;
        }
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        StringBuilder stringBuilder = new StringBuilder(mcode);
        String rmcode = stringBuilder.reverse().toString();
        Result rt = queryHBase(hTable , rmcode , ROAD_FAMILY , ROAD_QUALIFIER);
        if (rt == null){
            return;
        }
        byte[] roads = rt.getValue(ROAD_FAMILY , ROAD_QUALIFIER);
        if (roads == null){
            return;
        }
        int[][] iroad = Su.deserialize(roads);
        DoubleMatrix dMaxtri = new DoubleMatrix(iroad[0] , iroad[1] , iroad[2]);
        int[][] droad = dMaxtri.toIntArray2();
        //ConstructionBase.DrawImage(strPath + "\\road_img\\" + mcode + ".jpg" , droad);
        //ConstructionBase.exportTXT(strPath + "\\road\\" + mcode + ".txt", droad);
        //int[][] filterMaxtr = filterMaxtri(droad , pids);
        //ConstructionBase.DrawImage(strPath + "\\road_img_F\\" + mcode + ".jpg" , filterMaxtr);
        ConstructionBase.exportTXT(strPath + mcode + ".txt", droad);
        //if (tracks != null) {
        //    int[][] trackMaxtriT = Su.deserialize(tracks);
        //    DoubleMatrix trackM = new DoubleMatrix(trackMaxtriT[0] , trackMaxtriT[1] , trackMaxtriT[2]);
        //    Integer[][] trackMaxtri = trackM.toIntegerArray2();
            //Integer[][] FileterM = imageAlgorithm.medianFilter(trackMaxtri , 3);
            //Integer[][] UseMaxtri = imageAlgorithm.filterLessThanPara(FileterM , 5);

        //    int[][] trackm = new int[1024][1024];
        //    int[][] trackF = new int[1024][1024];
        //    for (int m = 0; m < trackMaxtri.length; m++) {
        //        for (int n = 0; n < trackMaxtri.length; n++) {
                    //if (UseMaxtri[m][n] == null) {
                    //    trackm[m][n] = 0;
                    //} else {
                    //    trackm[m][n] = UseMaxtri[m][n];
                    //}

        //            if (trackMaxtri[m][n] == null){
        //                trackF[m][n] = 0;
        //            }else{
        //                trackF[m][n] = trackMaxtri[m][n];
        //            }
        //        }
        //    }
            //ConstructionBase.DrawImage(strPath + "\\track_img\\" + mcode + ".jpg" , trackF);
            //ConstructionBase.DrawImage(strPath + "\\track_img_F\\" + mcode + ".jpg" , trackm);
        //    ConstructionBase.exportTXT(strPath + "\\track\\" + mcode + ".txt" , trackF);
            //ConstructionBase.exportTXT(strPath + "\\track_F\\" + mcode + ".txt" , trackm);
        //}
    }
    public static void main(String[] args) throws Exception{
        //MercatorUtil mercatorUtil = new MercatorUtil(256 , 12);

        //String mcode = mercatorUtil.lonLat2MCode(new Coordinate(116.254073 , 39.939949));

        //MCodeToMif mCodeToMif = new MCodeToMif();
        //mCodeToMif.ConvertFile("D:\\mapspotter\\北京市.txt","D:\\mapspotter\\output\\",12);
        //mCodeToMif.ConvertFile("D:\\mapspotter\\江苏省.txt","D:\\mapspotter\\output\\",12);
        //mCodeToMif.ConvertFile("D:\\mapspotter\\青海省.txt","D:\\mapspotter\\output\\",12);
        //mCodeToMif.ConvertFile("D:\\mapspotter\\宁夏回族自治区.txt","D:\\mapspotter\\output\\",12);

        //String inputfile = "D:\\mapspotter\\source\\DataFusion\\code\\road\\constructionroad\\datan\\";

        //String testFile = "D:\\mapspotter\\source\\MapSpotter\\code\\mapspotter\\out\\artifacts\\topic_jar\\Link\\test\\";
        ConstructionAnalysis extracttool = new ConstructionAnalysis();

        //extracttool.testHBase();

        //String zookeeperHost = "Master.hadoop:2181"; //args[0];
        //String tablename = "road_raster_201605"; //args[1];
        //String outpath = "D:\\mapspotter\\data\\direct_analysis\\test_data\\road\\" ; //args[4];
        //String tile_list =  "D:\\mapspotter\\data\\direct_analysis\\test_data\\tile_list1.txt";//args[5];
        //String pid_list = "D:\\mapspotter\\yukang\\pids.txt";//args[6];
        //String family = "16sum03";
        //String qualifier = "road";


        String zookeeperHost = args[0];
        String tablename = args[1];
        String outpath = args[4];
        String tile_list =  args[5];
        String pid_list = args[6];
        String family = args[2];
        String qualifier = args[3];

        System.out.println("zookeeperhost--" + zookeeperHost);
        System.out.println("tablename--" + tablename);
        System.out.println("family--" + family);
        System.out.println("qualifier--" + qualifier);
        System.out.println("outpath--" + outpath);
        System.out.println("tile_list--" + tile_list);
        System.out.println("pid_list--" + pid_list);

        extracttool.setROAD_FAMILY(family);
        extracttool.setROAD_QUALIFIER(qualifier);

        //List<Integer> pids = new ArrayList<>();
        //pids.add(24402874);
        //pids.add(17193134);
        //pids.add(17193133);
        //pids.add(17193128);
        extracttool.connHTable(tablename , zookeeperHost);
        extracttool.export_mcodes(outpath , tile_list , pid_list);
        //"D:\\mapspotter\\data\\confidence\\pids.txt"
        //extracttool.export_mcodes(outpath , tile_list ,pid_list);
        //extracttool.export_mcode_info("D:\\mapspotter\\data\\source_data\\test_didi" , "3340_1625" , pids);
        //extracttool.export_mcode_info("D:\\mapspotter\\data\\source_data\\test_didi" , "3431_1675" , pids);
        //extracttool.export_mcode_info("D:\\mapspotter\\data\\source_data\\test_didi" , "3429_1674" , pids);

        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\beijing\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\beijing");
        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\guangdong\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\guangdong");
        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\zhejiang\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\zhejiang");
        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\jiangsu\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\jiangsu");
        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\hebei\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\hebei");
        //extracttool.outputinfo("D:\\mapspotter\\data\\source_data\\shandong\\link2mcode.csv","D:\\mapspotter\\data\\source_data\\shandong");
        //System.out.println("处理完成");
        //return;
        //extracttool.ConvertTxt("D:\\mapspotter\\data\\da");

        //extracttool.createResult();
        //extracttool.dealBasePath(inputfile);
        //extracttool.testHBase();
        //extracttool.dealBasePath(inputfile);
        //extracttool.outputMap();
        //extracttool.outPutTextResult();
        //extracttool.closeResult();
        //extracttool.disconnHbase();
        System.out.println("处理完成");
    }
}
