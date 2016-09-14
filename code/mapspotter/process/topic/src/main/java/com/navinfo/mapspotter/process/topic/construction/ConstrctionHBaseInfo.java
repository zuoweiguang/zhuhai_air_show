package com.navinfo.mapspotter.process.topic.construction;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.io.Hbase;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangJin1207 on 2016/3/2.
 */
public class ConstrctionHBaseInfo {
    private final Logger logger = Logger.getLogger(ConstrctionHBaseInfo.class);

    String strHBase = null;
    public void setHBase(String strhbase){
        strHBase = strhbase;
    }

    String roadTableName = null;
    public void setroadTableName(String tableName){
        roadTableName = tableName;
    }
    String trackTableName = null;
    public void setTrackTableName(String trackTableName){this.roadTableName = roadTableName;}

    byte[] road_family = null;
    public void setRoad_family(String strRoadFamily){
        road_family = strRoadFamily.getBytes();
    }

    byte[] road_qaulifier = null;
    public void setRoad_qaulifier(String strRoadQualifier){
        road_qaulifier = strRoadQualifier.getBytes();
    }

    byte[] source_family = null;
    public void setSource_family(String strSourceFamily){
        source_family = strSourceFamily.getBytes();
    }

    byte[] source_qualifier = null;
    String sourcequalifier;
    public void setSource_qualifier(String strSourceQualifier){
        sourcequalifier = strSourceQualifier;
        source_qualifier = strSourceQualifier.getBytes();
    }

    private Hbase hbase = null;
    private Table roadTable = null;
    private Table trackTable = null;

    public boolean PrepareHBase(String strHBase , String roadTableName , String trackTableName , String strRoadFamily ,
                                String strRoadQualifier , String strSourceFamily , String strSQualifier){

        setHBase(strHBase);
        setroadTableName(roadTableName);
        setTrackTableName(trackTableName);
        setRoad_family(strRoadFamily);
        setRoad_qaulifier(strRoadQualifier);
        setSource_family(strSourceFamily);
        setSource_qualifier(strSQualifier);
        if (hbase == null) {
            hbase = Hbase.createWithConfiguration(ConstructionBase.getHBaseConf(strHBase));
        }
        roadTable = hbase.getTable(roadTableName);
        trackTable = hbase.getTable(trackTableName);

        if (hbase == null || roadTable == null || trackTable == null){
            logger.info("roadTable = " + roadTableName + " trackTable = " + trackTableName + "get table error");
        }
        return true;
    }

    public Result query(Table table ,String rowkey , byte[] family , byte[] qaulifier){
        if (table == null){
            return null;
        }
        try {
            Get get = new Get(rowkey.getBytes());
            get.addColumn(family, qaulifier);
            Result result = table.get(get);
            return result;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
    public RoadAnalysisInfo GetInfo(String mcode , List<Integer> links , int value){
        List<int[][]> LinkMaxtris = new ArrayList<>();
        List<Integer[][]> TrackMaxtris = new ArrayList<>();
        SerializeUtil<int[][]> su = new SerializeUtil<int[][]>();
        RoadAnalysisInfo raInfo = new RoadAnalysisInfo();

        try{
            StringBuilder stringBuilder = new StringBuilder(mcode);
            String rmcode = stringBuilder.reverse().toString();
            Result roadResult = query(roadTable , rmcode , road_family , road_qaulifier);
            Result trackResult = query(trackTable , rmcode , source_family , source_qualifier);
            if (roadResult == null || roadResult.isEmpty()) {
                return null;
            }

            byte[] roads = roadResult.getValue(road_family, road_qaulifier);
            byte[] tracks = null;
            if (trackResult != null){
                tracks = trackResult.getValue(source_family, source_qualifier);
            }
            if (roads == null){
                return null;
            }
            int[][] iroad = su.deserialize(roads);
            DoubleMatrix dMaxtri = new DoubleMatrix(iroad[0], iroad[1], iroad[2]);
            int[][] roadM = dMaxtri.toIntArray2();

            Integer[][] trackMaxtri = null;
            if (tracks == null) {
                trackMaxtri = new Integer[roadM.length][roadM.length];
            } else {
                    //DIDI轨迹存储是稀疏矩阵
                int[][] itracks = su.deserialize(tracks);
                DoubleMatrix dTracks = new DoubleMatrix(itracks[0] , itracks[1] , itracks[2]);
                trackMaxtri = dTracks.toIntegerArray2();
                    //SOGOU轨迹存储Integer[][]
                    //trackMaxtri = suI.deserialize(tracks);
            }
                //int[][] linkMaxtri = raInfo.GetMaxtriByPID(roadM, Pid);
            LinkMaxtris.add(roadM);
            TrackMaxtris.add(trackMaxtri);

            raInfo.setPids(links);
            raInfo.setOriginalMaxtris(LinkMaxtris);
            raInfo.setTrackMaxtris(TrackMaxtris);

            raInfo.analysisTile(value , sourcequalifier , links , mcode);
            //raInfo.analysis(value , sourcequalifier);
        }catch (Exception e){
            e.printStackTrace();
        }
        return raInfo;
    }

    public RoadAnalysisInfo GetInfo(List<String> mcodes , int Pid , int value){
        List<int[][]> LinkMaxtris = new ArrayList<>();
        List<Integer[][]> TrackMaxtris = new ArrayList<>();
        SerializeUtil<int[][]> su = new SerializeUtil<int[][]>();
        SerializeUtil<Integer[][]> suI = new SerializeUtil<>();
        RoadAnalysisInfo raInfo = new RoadAnalysisInfo();
        try{
            for (String val : mcodes) {
                Result roadResult = query(roadTable , val , road_family , road_qaulifier);
                Result trackResult = query(trackTable , val , source_family , source_qualifier);

                if (roadResult == null || roadResult.isEmpty()) {
                    continue;
                }

                byte[] roads = roadResult.getValue(road_family, road_qaulifier);
                byte[] tracks = null ;
                if (trackResult != null){
                    tracks = trackResult.getValue(source_family, source_qualifier);}
                if (roads == null){
                    continue;
                }
                int[][] iroad = su.deserialize(roads);
                DoubleMatrix dMaxtri = new DoubleMatrix(iroad[0], iroad[1], iroad[2]);
                int[][] roadM = dMaxtri.toIntArray2();

                Integer[][] trackMaxtri = null;
                if (tracks == null) {
                    trackMaxtri = new Integer[roadM.length][roadM.length];
                } else {
                    //DIDI轨迹存储是稀疏矩阵
                    int[][] itracks = su.deserialize(tracks);
                    DoubleMatrix dTracks = new DoubleMatrix(itracks[0] , itracks[1] , itracks[2]);
                    trackMaxtri = dTracks.toIntegerArray2();
                    //SOGOU轨迹存储Integer[][]
                    //trackMaxtri = suI.deserialize(tracks);
                }
                //int[][] linkMaxtri = raInfo.GetMaxtriByPID(roadM, Pid);
                LinkMaxtris.add(roadM);
                TrackMaxtris.add(trackMaxtri);
            }
            raInfo.setPid(Pid);
            raInfo.setOriginalMaxtris(LinkMaxtris);
            raInfo.setTrackMaxtris(TrackMaxtris);

            raInfo.analysis(value , sourcequalifier);
        }catch (Exception e){
            e.printStackTrace();
        }
        return raInfo;
    }
}
