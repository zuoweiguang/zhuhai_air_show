package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.algorithm.ConnectedAlgorithm;
import com.navinfo.mapspotter.foundation.algorithm.ImageAlgorithm;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.vividsolutions.jts.geom.Envelope;
import java.util.*;

/**
 * Created by ZhangJin1207 on 2016/1/20.
 */
public class RoadAnalysisInfo {
    private static final Logger logger = Logger.getLogger(RoadAnalysisInfo.class);
    private int Pid = 0;
    public int getPid(){return  Pid;}
    public void setPid(int pid){Pid = pid;}

    private List<Integer> pids;
    public void setPids(List<Integer> pids){
        this.pids = pids;
    }
    private int Function_class = 0;
    public int getFunction_class(){return Function_class;}
    public void setFunction_class(int function_class){Function_class = function_class;}

    private Map<String , int[]> LinkMCodes = null;
    public Map<String , int[]> getLinkMCodes(){return LinkMCodes;}
    public void setLinkMCodes(Map<String , int[]> mcodes){LinkMCodes = mcodes;}

    private List<int[][]> OriginalMaxtris = null;
    public List<int[][]> getOriginalMaxtris(){return OriginalMaxtris;}
    public void setOriginalMaxtris(List<int[][]> maxtris){OriginalMaxtris = maxtris;}

    private List<Integer[][]> TrackMaxtris = null;
    public List<Integer[][]> getTrackMaxtris(){return  TrackMaxtris;}
    public void setTrackMaxtris(List<Integer[][]> maxtris){TrackMaxtris = maxtris;}

    private List<int[][]> AnalysisResult = null;
    public List<int[][]> getAnalysisResult(){return AnalysisResult;}
    public void setAnalysisResult(List<int[][]> maxtris){AnalysisResult = maxtris;}

    private long nearOverArea = 0;
    public long getNearOverArea(){return nearOverArea;}
    public void setNearOverArea(long neaoverarea){nearOverArea = neaoverarea;}

    private long farOverArea = 0;
    public long getFarOverArea(){return farOverArea;}
    public void setFarOverArea(long faroverarea){farOverArea = faroverarea;}

    private long LinkOverArea = 0;
    public long getLinkOverArea(){return LinkOverArea;}
    public void setLinkOverArea(long linkoverarea) {LinkOverArea = linkoverarea;}

    private long TrackHitArea = 0;
    public long getTrackHitArea(){return TrackHitArea;}
    public void setTrackHitArea(Long trackhitarea){TrackHitArea = trackhitarea;}

    private long nearHitArea = 0;
    public long getNearHitArea(){return nearHitArea;}
    public void setNearHitArea(long nearhitarea) {nearHitArea = nearhitarea;}

    private long farHitArea = 0;
    public long getFarHitArea(){return farHitArea;}
    public void setFarHitArea(long farhitarea){farHitArea = farhitarea;}

    private long hitTrackPoints = 0;
    public long getHitTrackPoints(){return hitTrackPoints;}
    public void setHitTrackPoints(long hitTrackPoints){this.hitTrackPoints = hitTrackPoints;}

    private long hitFarTrackPoints = 0;

    public void setHitFarTrackPoints(long hitFarTrackPoints) {
        this.hitFarTrackPoints = hitFarTrackPoints;
    }
    public long getHitFarTrackPoints() {

        return hitFarTrackPoints;
    }

    private long hitNearTrackPoints = 0;
    public long getHitNearTrackPoints() {
        return hitNearTrackPoints;
    }

    public void setHitNearTrackPoints(long hitNearTrackPoints) {
        this.hitNearTrackPoints = hitNearTrackPoints;
    }

    private double tileIndensity = 0.0;

    public double getTileIndensity() {
        return tileIndensity;
    }

    public void setTileIndensity(double tileIndensity) {
        this.tileIndensity = tileIndensity;
    }

    private double dHitRatio = 0.0;
    public double getdHitRatio(){
        if (LinkOverArea == 0){
            return 0.0;
        }
        return (double)TrackHitArea / (double)LinkOverArea;}
    private double dnearHitRatio = 0.0;
    public double getDnearHitRatio(){
        if (nearOverArea == 0){
            return 0.0;
        }
        return (double)nearHitArea / (double)nearOverArea;
    }

    private double dfarHitRatio = 0.0;
    public double getDfarHitRatio(){
        if (farOverArea == 0){
            return 0.0;
        }
        return (double)farHitArea / (double)farOverArea;
    }

    public double getFarIndensity(){
        if (farHitArea == 0){
            return 0.0;
        }
        return (double)hitFarTrackPoints / farHitArea;
    }

    public double getNearIndensity(){
        if (nearHitArea == 0){
            return 0.0;
        }
        return (double)hitNearTrackPoints / nearHitArea;
    }

    public double getHitIndensity(){
        if ((farHitArea + nearHitArea) == 0){
            return 0.0;
        }
        return (double) (hitFarTrackPoints + hitNearTrackPoints) / (farHitArea + nearHitArea);
    }

    public String toOutput(){
        String strRet = "" + LinkOverArea + "\t" + TrackHitArea + "\t" + getdHitRatio() + "\t" + getHitIndensity() + "\t" +
                farOverArea + "\t" + farHitArea + "\t" + getDfarHitRatio() + "\t" + getFarIndensity() + "\t" +
                nearOverArea + "\t" + nearHitArea + "\t" + getDnearHitRatio() + "\t" + getNearIndensity() + "\t" + tileIndensity;
        return strRet;
    }

    private List<ConstructionResultInfo> tilelinks = new ArrayList<>();
    public  List<ConstructionResultInfo> getTilelinks(){
        return tilelinks;}

    public void analysis(int value , String sourceType) {
        if (getOriginalMaxtris().isEmpty()) {
            return;
        }

        if (getOriginalMaxtris().size() != getTrackMaxtris().size()) {
            return;
        }
        double averageSum = 0.0;
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        List<int[][]> results = new ArrayList<int[][]>();
        for (int i = 0; i < getOriginalMaxtris().size(); i++) {
            int[][] roadMaxtri = getOriginalMaxtris().get(i);
            Integer[][] trackMaxtri = getTrackMaxtris().get(i);

            double averageval = imageAlgorithm.martixAverage(trackMaxtri);
            averageSum += averageval;
            Envelope envelope = getEnvelopByPID(roadMaxtri , getPid() , 3);
            if (Math.abs(envelope.getMinX() - envelope.getMaxX()) > roadMaxtri.length){
                continue;
            }
            int filterVal = value;
            int[][] roadMaxtriN = getMaxtriByEnvelope(roadMaxtri , envelope);
            if (sourceType.equals("baidu")){
                filterVal = (int)Math.floor(averageval) > value ? (int)Math.floor(averageval) : value;
                //Integer[][] btrack = imageAlgorithm.filterStepOne(trackMaxtri , value);
                //Integer[][] trackMaxtriN = getMaxtriByEnvelope(btrack , envelope);
                //useMaxtri = imageAlgorithm.medianFilter(trackMaxtriN, 3);
            }// else{
            //Integer[][] trackMaxtriN = getMaxtriByEnvelope(trackMaxtri , envelope);
            //Integer[][] filterMaxtri = imageAlgorithm.medianFilter(trackMaxtriN, 3);
            //useMaxtri = imageAlgorithm.filterLessThanPara(filterMaxtri, value);
            //}
            Integer[][] trackMaxtriN = getMaxtriByEnvelope(trackMaxtri , envelope);
            Integer[][] filterMaxtri = imageAlgorithm.medianFilter(trackMaxtriN, 3);
            Integer[][] useMaxtri = imageAlgorithm.filterLessThanPara(filterMaxtri, filterVal);
            int[][] FilterRoad = FilterMaxtri(roadMaxtriN, getPid(), 3);
            int[][] result = getSameMaxtri(FilterRoad, useMaxtri, getPid());
            results.add(result);
        }

        if (getOriginalMaxtris().size() > 0){
            tileIndensity = averageSum / getOriginalMaxtris().size();
        }else{
            tileIndensity = 0.0;
        }
    }

    public Integer[][] getMaxtriByEnvelope(Integer[][] maxtri , Envelope envelope){
        Integer[][] newMaxtri = new Integer[(int)(envelope.getMaxX()-envelope.getMinX()) + 1][(int)(envelope.getMaxY()-envelope.getMinY()) + 1];
        int row = 0;
        for (int i = (int)envelope.getMinX() ; i <= (int)envelope.getMaxX() ; i++){
            int col = 0;
            for (int j = (int)envelope.getMinY(); j <= (int)envelope.getMaxY() ; j++){
                newMaxtri[row][col] = maxtri[i][j];
                col++;
            }
            row++;
        }
        return newMaxtri;
    }

    public int[][] getMaxtriByEnvelope(int[][] maxtri , Envelope envelope){
        int[][] newMaxtri = new int[(int)(envelope.getMaxX()-envelope.getMinX()) + 1][(int)(envelope.getMaxY()-envelope.getMinY()) + 1];
        int row = 0;
        for (int i = (int)envelope.getMinX() ; i <= (int)envelope.getMaxX() ; i++){
            int col = 0;
            for (int j = (int)envelope.getMinY(); j <= (int)envelope.getMaxY() ; j++){
                newMaxtri[row][col] = maxtri[i][j];
                col++;
            }
            row++;
        }
        return newMaxtri;
    }


    public Envelope getEnvelopByPID(int[][] maxtri , int pid , int level){
        int mini = Integer.MAX_VALUE;
        int minj = Integer.MAX_VALUE;
        int maxi = Integer.MIN_VALUE;
        int maxj = Integer.MIN_VALUE;

        for (int i = 0 ; i < maxtri.length; i++){
            int[] dataArray = maxtri[i];
            for (int j = 0 ; j < dataArray.length; j++){
                int data = dataArray[j];
                if (data == pid){
                    mini = mini > i ? i : mini;
                    minj = minj > j ? j : minj;
                    maxi = maxi > i ? maxi : i;
                    maxj = maxj > j ? maxj : j;
                }
            }
        }

        mini = mini - level < 0 ? 0 : mini - level;
        minj = minj - level < 0 ? 0 : minj - level;
        maxi = maxi + level > maxtri.length - 1 ? maxtri.length - 1 : maxi + level;
        maxj = maxj + level > maxtri[0].length -1  ? maxtri[0].length -1  : maxj + level;

        return new Envelope(mini , maxi , minj , maxj);
    }

    public int getMaxtriHitCount(int[][] maxtri){
        int iHitCount = 0;
        for (int i = 0 ; i < maxtri.length ; i ++){
            int[] dataArray = maxtri[i];
            for (int j = 0 ; j < dataArray.length ; j++){
                if (dataArray[j] != 0){
                    iHitCount++;
                }
            }
        }

        return iHitCount;
    }

    public int[][] GetMaxtriByPID(int[][] maxtri , int pid){
        int maxtrilen = maxtri.length;
        if (maxtrilen == 0){
            return null;
        }
        int[][] newMaxtri = new int[maxtrilen][maxtri[0].length];
        for (int i = 0 ; i < maxtrilen ; i++){
            int[] dataArray = maxtri[i];
            for (int j = 0 ; j < dataArray.length ; j++){
                if (dataArray[j] == pid){
                    newMaxtri[i][j] = pid;
                }
            }
        }
        return newMaxtri;
    }

    public int[][] getSameMaxtri(int[][] baseroad , Integer[][] tracks , int pid){
        int row = baseroad.length;
        int col = baseroad[0].length;
        int[][] result = new int[row][col];

        if (baseroad.length != tracks.length){
            return result;
        }
        for (int i = 0 ; i < row ; i++){
            for (int j = 0 ; j < col ;j ++){
                int ivalue = baseroad[i][j];
                if (ivalue == pid){
                    if (tracks[i][j] != null && tracks[i][j] > 0){
                        result[i][j] = 1;
                        farHitArea++;
                        TrackHitArea++;
                        hitFarTrackPoints += tracks[i][j];
                    }
                    farOverArea++;
                    LinkOverArea++;
                }else if (ivalue == -1 * pid){
                    if (tracks[i][j] != null && tracks[i][j] > 0){
                        result[i][j] = -1;
                        nearHitArea ++ ;
                        TrackHitArea++;
                        hitNearTrackPoints += tracks[i][j];
                    }
                    nearOverArea++;
                    LinkOverArea++;
                }else{
                    result[i][j] = 0;
                }
            }
        }
        return result;
    }

    public int[][] FilterMaxtri(int[][] sourceMaxtri , int pid , int nLevel){
        int[][] newMaxtri = new int[sourceMaxtri.length][sourceMaxtri[0].length];
        for (int i = 0 ; i < sourceMaxtri.length ; i++){
            int[] dataArray = sourceMaxtri[i];
            for (int j = 0 ; j < dataArray.length; j++){
                if (dataArray[j] == pid){
                    int[][] subMaxtri = GetSubMaxtri(sourceMaxtri , i , j , nLevel);
                    newMaxtri[i][j] = pid * GetFlagByPid(subMaxtri , pid);
                }
                else{
                    newMaxtri[i][j] = 0;
                }
            }
        }
        return newMaxtri;
    }

    public int[][] GetSubMaxtri(int[][] sourceMaxtri , int row , int col , int nLevel){
        int[][] subMaxtri = new int[nLevel][nLevel];
        int mrow = sourceMaxtri.length;
        int mcol = sourceMaxtri[0].length;
        for (int i = 0 ; i < nLevel ; i++){
            for (int j = 0 ; j < nLevel; j++){
                int m = row - nLevel / 2 + i;
                int n = col - nLevel / 2 + j;
                if (m >= 0 && m < mrow && n >= 0 && n < mcol){
                    subMaxtri[i][j] = sourceMaxtri[m][n];
                }else{
                    subMaxtri[i][j] = 0;
                }
            }
        }
        return subMaxtri;
    }

    public int GetFlagByPid(int[][] sourceMaxtri , int pid){
        for (int i = 0 ; i < sourceMaxtri.length ; i++){
            int[] dataArray = sourceMaxtri[i];
            for (int j = 0 ; j < dataArray.length ; j++){
                int ivalue = dataArray[j];
                if (ivalue != 0 && ivalue != pid){
                    return -1;
                }
            }
        }
        return 1;
    }

    public int[][] filterMatrixByLinks(int[][] matrix , List<Integer> links){
        if (matrix == null){
            return null;
        }
        int[][] retMatrix = new int[matrix.length][matrix[0].length];
        for (int row = 0 ; row < matrix.length ; row++){
            int[] rowData = matrix[row];
            for (int col = 0 ; col < rowData.length ; col++){
                int data = rowData[col];
                if (data > 0 && links.contains(data)){
                    retMatrix[row][col] = data;
                }
            }
        }
        return retMatrix;
    }

    public Integer[][] intersectMatrix(Integer[][] rmatrix , Integer[][] tmatrix , Map<Integer , int[]> linksinfo){
        if (rmatrix == null || tmatrix == null || rmatrix.length != tmatrix.length){
            return null;
        }
        Integer[][] retmatrix = new Integer[tmatrix.length][tmatrix[0].length];

        for (int row = 0 ; row < tmatrix.length ; row++){
            Integer[] rowData = tmatrix[row];
            for (int col = 0 ; col < rowData.length ; col++){
                int data = (rowData[col] == null ? 0 : rowData[col]);
                int rdata = (rmatrix[row][col] == null ? 0 : rmatrix[row][col]);
                if (rdata != 0){
                    retmatrix[row][col] = data;
                }else{
                    continue;
                }
                int[] linkinfo = linksinfo.get(Math.abs(rdata));
                if (linkinfo == null){
                    linkinfo = new int[9];
                }
                linkinfo[0]++;
                if (rdata > 0){
                    linkinfo[3]++;
                    if (data > 0 ){
                        linkinfo[4]++;
                        linkinfo[1]++;
                        linkinfo[2] += data;
                        linkinfo[5] += data;
                    }
                }else{
                    linkinfo[6]++;
                    if (data > 0){
                        linkinfo[7]++;
                        linkinfo[1]++;
                        linkinfo[2] += data;
                        linkinfo[8] += data;
                    }
                }

                linksinfo.put(Math.abs(rdata) ,linkinfo);
            }
        }

        return retmatrix;
    }

    public int getFlagByLinks(int[][] matrix , List<Integer> links){
        if (matrix == null){
            return 1;
        }
        for (int row = 0 ; row < matrix.length ; row++){
            int[] rowData = matrix[row];
            for (int col = 0 ; col < rowData.length ; col++){
                int data = rowData[col];
                if (data > 0 && !links.contains(data)){
                    return -1;
                }
            }
        }
        return 1;
    }

    public int[][] convertMatrix(int[][] matrix , int level , List<Integer> links){
        int[][] newMaxtri = new int[matrix.length][matrix[0].length];
        for (int i = 0 ; i < matrix.length ; i++){
            int[] dataArray = matrix[i];
            for (int j = 0 ; j < dataArray.length; j++){
                int data = dataArray[j];
                if (links.contains(data)){
                    int[][] subMaxtri = GetSubMaxtri(matrix , i , j , level);
                    newMaxtri[i][j] = data * getFlagByLinks(subMaxtri , links);
                }
            }
        }
        return newMaxtri;
    }

    public Integer[][] convertMatrix_obj(int[][] matrix , int level , List<Integer> links){
        Integer[][] newMaxtri = new Integer[matrix.length][matrix[0].length];
        for (int i = 0 ; i < matrix.length ; i++){
            int[] dataArray = matrix[i];
            for (int j = 0 ; j < dataArray.length; j++){
                int data = dataArray[j];
                if (data > 0 && links.contains(data)){
                    int[][] subMaxtri = GetSubMaxtri(matrix , i , j , level);
                    newMaxtri[i][j] = data * getFlagByLinks(subMaxtri , links);
                }
            }
        }
        return newMaxtri;
    }

    public Map<String , Map<Integer , int[]>> annlysisregionstrack(Map<String , List<Integer[]>> regions , Integer[][] rmatrix , Integer[][] tmatrix){
        if (regions == null){
            return null;
        }
        Map<String , Map<Integer , int[]>> retmap = new HashMap<>();
        Iterator itr = regions.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry entry = (Map.Entry) itr.next();
            String key = (String) entry.getKey();
            List<Integer[]> values = (List<Integer[]>) entry.getValue();
            for (Integer[] value : values){
                int row = value[0];
                int col = value[1];

                int rdata = (rmatrix[row][col] == null ? 0 : rmatrix[row][col]);
                int tdata = (tmatrix[row][col] == null ? 0 : tmatrix[row][col]);
                if (rdata == 0){
                    continue;
                }

                Map<Integer , int[]> linkinfo = retmap.get(key);
                if (linkinfo == null){
                    linkinfo = new HashMap<>();
                }

                int[] info = linkinfo.get(Math.abs(rdata));
                if (info == null){
                    info = new int[9];
                }

                info[0]++;
                if (rdata > 0){
                    info[3]++;
                    if (tdata > 0 ){
                        info[4]++;
                        info[1]++;
                        info[2] += tdata;
                        info[5] += tdata;
                    }
                }else{
                    info[6]++;
                    if (tdata > 0){
                        info[7]++;
                        info[1]++;
                        info[2] += tdata;
                        info[8] += tdata;
                    }
                }
                linkinfo.put(Math.abs(rdata) , info);
                retmap.put(key , linkinfo);
            }
        }
        return retmap;
    }
    public Map<Integer , Map<String , int[]>> analysisregions(Map<String , List<Integer[]>> regions , Integer[][] rmatrix , Integer[][] tmatrix){
        if (regions == null){
            return null;
        }
        Map<Integer , Map<String , int[]>> retmap = new HashMap<>();
        Iterator itr = regions.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry entry = (Map.Entry) itr.next();
            String key = (String) entry.getKey();
            List<Integer[]> values = (List<Integer[]>) entry.getValue();
            for (Integer[] value : values){
                int row = value[0];
                int col = value[1];

                int rdata = (rmatrix[row][col] == null ? 0 : rmatrix[row][col]);
                int tdata = (tmatrix[row][col] == null ? 0 : tmatrix[row][col]);
                if (rdata == 0){
                    continue;
                }

                Map<String , int[]> linkinfo = retmap.get(Math.abs(rdata));
                if (linkinfo == null){
                    linkinfo = new HashMap<>();
                }

                int[] info = linkinfo.get(key);
                if (info == null){
                    info = new int[9];
                }

                info[0]++;
                if (rdata > 0){
                    info[3]++;
                    if (tdata > 0 ){
                        info[4]++;
                        info[1]++;
                        info[2] += tdata;
                        info[5] += tdata;
                    }
                }else{
                    info[6]++;
                    if (tdata > 0){
                        info[7]++;
                        info[1]++;
                        info[2] += tdata;
                        info[8] += tdata;
                    }
                }
                linkinfo.put(key , info);
                retmap.put(Math.abs(rdata) , linkinfo);
            }
        }
        return retmap;
    }

    public void analysisTile(int value , String sourceType , List<Integer> links , String mcode){
        if (getOriginalMaxtris().size() != getTrackMaxtris().size() || getOriginalMaxtris().size() != 1){
            return;
        }
        ImageAlgorithm imageAlgorithm = new ImageAlgorithm();
        ConnectedAlgorithm connectedAlgorithm = new ConnectedAlgorithm();
        int[][] roadmatrix = getOriginalMaxtris().get(0);
        Integer[][] trackmatrix = getTrackMaxtris().get(0);

        int valueFilter = value;
        //////值过滤
        double averageMatrix = imageAlgorithm.martixAverage(trackmatrix);
        if (sourceType.equals("baidu")){
            valueFilter = (int)Math.floor(averageMatrix) > value ? (int)Math.floor(averageMatrix) : value;
        }
        Integer[][] valueFiltermatrix = imageAlgorithm.filterLessThanPara(trackmatrix , valueFilter);
        /////中值过滤
        Integer[][] medianFiltermatrix = imageAlgorithm.medianFilter(valueFiltermatrix , 3);
        /////提取施工Link
        Integer[][] linksmatrix = convertMatrix_obj(roadmatrix , 3 , links);
        /////link 分区域
        Integer[][] linkinit = imageAlgorithm.initFloodFill(linksmatrix);
        Integer[][] linksregion = connectedAlgorithm.doLabel(linkinit);
        Map<String , List<Integer[]>> linksregions = imageAlgorithm.matrixToRegion(linksregion , 0);
        Map<Integer , Map<String , int[]>> linksregioninfo = analysisregions(linksregions ,linksmatrix , medianFiltermatrix);
        Map<Integer , int[]> linksinfo = new HashMap<>();
        /////提取轨迹点
        Integer[][] linkstrackmatrix = intersectMatrix(linksmatrix , medianFiltermatrix , linksinfo);

        /////分区域
        Integer[][] linkstrackInit = imageAlgorithm.initFloodFill(linkstrackmatrix);
        Integer[][] linkstrackregion = connectedAlgorithm.doLabel(linkstrackInit);
        Map<String , List<Integer[]>> trackregions = imageAlgorithm.matrixToRegion(linkstrackregion , 0);

        Map<Integer , Map<String , int[]>> trackregioninfo = analysisregions(trackregions , linksmatrix , medianFiltermatrix);
        Map<String , Map<Integer , int[]>> regiontrackinfo = annlysisregionstrack(trackregions , linksmatrix , medianFiltermatrix);
        ////// link 分析
        for (Integer link : links){
            //ConstructionResultInfo constructionResultInfo = analysislink(link , linksregioninfo , trackregioninfo , regiontrackinfo);
            double dweight = 0.0;
            double tracksplitratio = 1.0;
            double linksplitratio = 1.0;
            ConstructionResultInfo constructionResultInfo = new ConstructionResultInfo();
            int link_pid = (link == null ? 0 : link);
            constructionResultInfo.setLink_pid(link_pid);
            Map<String , int[]> linkinfo = linksregioninfo.get(link_pid);
            Map<String , int[]> trackinfo = trackregioninfo.get(link_pid);
            if (linkinfo != null && trackinfo != null){
                if (trackinfo.size() > 1){
                    tracksplitratio = 1 / (double)trackinfo.size();
                }
                if (linkinfo.size() > 1){
                    linksplitratio =  1 / (double)linkinfo.size();
                }
            }
            dweight = linksplitratio * tracksplitratio;

            int[] linkatrr = linksinfo.get(link_pid);
            if (linkatrr != null){
                constructionResultInfo.setLink_pn(linkatrr[0]);
                constructionResultInfo.setTrack_pn(linkatrr[1]);
                constructionResultInfo.setLink_indensity(linkatrr[2]);

                constructionResultInfo.setFar_link_pn(linkatrr[3]);
                constructionResultInfo.setFar_track_pn(linkatrr[4]);
                constructionResultInfo.setFar_link_indensity(linkatrr[5]);

                constructionResultInfo.setNear_link_pn(linkatrr[6]);
                constructionResultInfo.setNear_track_pn(linkatrr[7]);
                constructionResultInfo.setNear_link_indensity(linkatrr[8]);

                constructionResultInfo.setWeight(dweight);

                constructionResultInfo.setTile_indensity(averageMatrix);
                constructionResultInfo.setTile(mcode);
            }
            tilelinks.add(constructionResultInfo);
        }
    }

    public ConstructionResultInfo analysislink(int link_pid , Map<Integer , Map<String , int[]>> linkinfo ,
                                               Map<Integer , Map<String , int[]>> linktrackinfo ,Map<String , Map<Integer , int[]>> trackinfo){
        ConstructionResultInfo constructionResultInfo = new ConstructionResultInfo();
        constructionResultInfo.setLink_pid(link_pid);

        Map<String , int[]> linki = linkinfo.get(link_pid);

        if (linki == null){
            return constructionResultInfo;
        }
        int[] info = new int[9];
        Iterator itr = linki.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry entry = (Map.Entry)itr.next();
            int[] infol = (int[]) entry.getValue();
            for (int i = 0 ; i < 9 ; i++){
                info[i] += infol[i];
            }
        }
        float[] ratios = new float[9];
        Map<String , int[]> linktrack = linktrackinfo.get(link_pid);
        if (linktrack != null) {
            Iterator itr1 = linktrack.entrySet().iterator();
            while (itr1.hasNext()) {// link分割
                Map.Entry entry = (Map.Entry) itr1.next();
                String key = (String) entry.getKey();
                Map<Integer, int[]> tracki = trackinfo.get(key);
                int[] regioni = new int[9];
                Iterator itr2 = linki.entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry entry1 = (Map.Entry) itr2.next();
                    int[] info2 = (int[]) entry1.getValue();
                    for (int i = 0; i < 9; i++) {
                        regioni[i] += info2[i];
                    }
                }
                int[] regionl = tracki.get(link_pid);

                for (int j = 0 ; j < 9 ; j++){
                    ratios[j] += (regioni[j] == 0 ? 0.0 : regionl[j] / (float)regioni[j]);
                }
            }
        }else{
            for (int j = 0 ; j < 9 ; j ++){
                ratios[j] = 1.0f;
            }
        }

        constructionResultInfo.setLink_pn(info[0]);
        constructionResultInfo.setTrack_pn(info[1]);
        constructionResultInfo.setLink_indensity(info[2]);

        constructionResultInfo.setFar_link_pn(info[3]);
        constructionResultInfo.setFar_track_pn(ratios[4] / 3 > 1 ? info[4] : Math.round(info[4] * ratios[4] / 3));
        constructionResultInfo.setFar_link_indensity(info[5]);

        constructionResultInfo.setNear_link_pn(info[6]);
        constructionResultInfo.setNear_track_pn(ratios[7] / 3 > 1 ? info[7] : Math.round(info[7] * ratios[7] / 3));
        constructionResultInfo.setNear_link_indensity(info[8]);

        return constructionResultInfo;
    }
}
