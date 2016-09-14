package com.navinfo.mapspotter.process.topic.restriction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 路口栅格信息
 * Created by SongHuiXing on 2016/1/12.
 */
public class CrossRaster {

    private long pid;
    @JsonProperty("Pid")
    public long getPid(){
        return pid;
    }
    public void setPid(long pid){
        this.pid = pid;
    }

    private double[] crossenv;
    @JsonProperty("CrossEnvelope")
    public double[] getCrossEnvelope(){
        return crossenv;
    }
    public void setCrossEnvelope(double[] crossEnvelope){
        crossenv = crossEnvelope;
    }

    private double[] pageenv;
    @JsonProperty("PageEnvelope")
    public double[] getPageEnvelope(){
        return pageenv;
    }
    public void setPageEnvelope(double[] pageEnvelope){
        pageenv = pageEnvelope;
    }

    private int[][] sparse_raster;
    @JsonProperty("Buffer")
    public int[][] getSparseRaster(){
        return sparse_raster;
    }
    public void setSparseRaster(int[][] raster){
        sparse_raster = raster;
    }

    private int raster_colcount;
    @JsonProperty("RasterColCount")
    public int getRasterColCount(){
        return raster_colcount;
    }
    public void setRasterColCount(int colCount){
        raster_colcount = colCount;
    }

    //记录该栅格4个角点的tile号码，及其在全幅墨卡托的pixel位置
    //以lb, lt, rt, rb的顺序存储
    private ArrayList<Map.Entry<String, IntCoordinate>> corner_tile_pos = null;
    @JsonIgnore
    public ArrayList<Map.Entry<String, IntCoordinate>> getCornerTilePos(){
        return corner_tile_pos;
    }
    public void setCorner_tile_pos(ArrayList<Map.Entry<String, IntCoordinate>> tileinfo){
        corner_tile_pos = tileinfo;
    }

    @JsonIgnore
    public int[][] getDenseRaster(){
        DoubleMatrix mx = getMatrix();
        return mx.toIntArray2();
    }

    @JsonIgnore
    public DoubleMatrix getMatrix(){
        return DoubleMatrix.fromSparse(sparse_raster[0], sparse_raster[1], sparse_raster[2], raster_colcount);
    }
}
