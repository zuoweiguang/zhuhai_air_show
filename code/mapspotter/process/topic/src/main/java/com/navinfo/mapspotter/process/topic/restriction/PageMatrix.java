package com.navinfo.mapspotter.process.topic.restriction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navinfo.mapspotter.foundation.algorithm.Raster;
import com.navinfo.mapspotter.foundation.algorithm.string.SimpleCountCluster;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.navinfo.mapspotter.process.topic.restriction.analysis.TrackHitInfo;
import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面栅格系统，采用类似墨卡托tile的像素系统，但页面范围可定义
 * Created by SongHuiXing on 2016/1/13.
 */
public class PageMatrix {
    private double[] m_worldenvelope = new double[4];

    private int m_validRow = 0;
    public int getValidRow(){
        return m_validRow;
    }

    private int m_validCol = 0;
    public int getValidCol(){
        return m_validCol;
    }

    private int m_mercatorLevel = 14;
    private MercatorUtil mercatorUtil = null;

    private IntCoordinate m_lbPixel;
    private IntCoordinate m_rtPixel;

    /**
     * 路口raster，目前采用墨卡托栅格划分，坐标为从上到下，从左到右
     * @param pageenvelope 页面范围，经纬度坐标
     * @param tilelevel     墨卡托tile等级
     */
    public PageMatrix(double[] pageenvelope, int tilelevel){
        m_mercatorLevel = tilelevel;
        mercatorUtil = new MercatorUtil(1024, m_mercatorLevel);

        m_worldenvelope = pageenvelope;

        m_lbPixel = mercatorUtil.lonLat2Pixels(new Coordinate(pageenvelope[0], pageenvelope[1]));
        m_rtPixel = mercatorUtil.lonLat2Pixels(new Coordinate(pageenvelope[2], pageenvelope[3]));

        m_validRow = m_lbPixel.y - m_rtPixel.y + 1;
        m_validCol = m_rtPixel.x - m_lbPixel.x + 1;
    }

    /**
     * 获取线段经过的raster格子
     * 若线段在raster外则返回空
     * @param worldx0 起点经度
     * @param worldy0 起点纬度
     * @param worldx1 终点经度
     * @param worldy1 终点纬度
     * @param needEnd 是否需要线段的最后一个点
     * @return 一系列格子的标号<所在列，所在行>
     */
    public List<int[]> getValidRasterCoords(double worldx0, double worldy0,
                                            double worldx1, double worldy1,
                                            boolean needEnd){
        List<int[]> coords = new ArrayList<>();

        double[] lineEnv = new double[]{Math.min(worldx0, worldx1),
                Math.min(worldy0, worldy1),
                Math.max(worldx0, worldx1),
                Math.max(worldy0, worldy1)};

        if(!SpatialUtil.isEnvelopeIntesect(lineEnv, m_worldenvelope))
            return coords;

        int[] start = worldToPage(worldx0, worldy0);
        int[] end = worldToPage(worldx1, worldy1);

        return interpolation(start, end, needEnd);
    }

    public List<int[]> getValidRasterCoords(int pixelx0, int pixely0,
                                            int pixelx1, int pixely1,
                                            boolean needEnd) {
        int[] lineEnv = new int[]{Math.min(pixelx0, pixelx1),
                                Math.min(pixely0, pixely1),
                                Math.max(pixelx0, pixelx1),
                                Math.max(pixely0, pixely1)};

        if(!SpatialUtil.isEnvelopeIntesect(lineEnv,
                                            new int[]{m_lbPixel.x, m_rtPixel.y, m_rtPixel.x, m_lbPixel.y}))
            return new ArrayList<>();

        int[] start = worldToPage(pixelx0, pixely0);
        int[] end = worldToPage(pixelx1, pixely1);

        return interpolation(start, end, needEnd);
    }

    private List<int[]> interpolation(int[] start, int[] end, boolean needEnd){
        List<int[]> coords = new ArrayList<>();

        if(start[0] != end[0]){
            List<int[]> range = Raster.getAdvanceBresenhamline(start[0],start[1], end[0],end[1]);

            range.add(0, start);

            for(int[] coord : range){
                if(coord[0] >=0 && coord[0]<m_validCol &&
                   coord[1] >=0 && coord[1]<m_validRow){

                    if(!needEnd && coord[0]==end[0] && coord[1]==end[1])
                        continue;

                    coords.add(coord);
                }
            }

        } else{
            int col = start[0];
            if(col <0 || col >= m_validCol)
                return coords;

            int srow = start[1];
            int erow = end[1];

            if(erow > srow){
                srow = srow < 0 ? 0 : srow;
                erow = needEnd ? erow : erow-1;
                erow = erow >= m_validRow ? m_validRow-1 : erow;
                for(int row=srow; row <= erow; row++){
                    coords.add(new int[]{col, row});
                }
            } else{
                srow = srow >= m_validRow ? m_validRow-1 : srow;
                erow = needEnd ? erow : erow+1;
                erow = erow < 0 ? 0 : erow;
                for(int row=srow; row >= erow; row--){
                    coords.add(new int[]{col, row});
                }
            }
        }

        return coords;
    }

    /**
     * 经纬度坐标转换为raster内的相对坐标
     * @param worldx 经度
     * @param worldy 纬度
     * @return raster内相对坐标
     */
    public int[] worldToPage(double worldx, double worldy){
        IntCoordinate pixels = mercatorUtil.lonLat2Pixels(new Coordinate(worldx, worldy));

        return new int[]{pixels.x - m_lbPixel.x, pixels.y - m_lbPixel.y};
    }

    /**
     * 墨卡托像素坐标转换为raster内的相对坐标
     * @param pixelX 横向像素序号
     * @param pixelY 纵向像素序号
     * @return raster内相对坐标
     */
    public int[] worldToPage(int pixelX, int pixelY){
        return new int[]{pixelX - m_lbPixel.x, pixelY - m_rtPixel.y};
    }

    /**
     * 获取轨迹在路口的编码串,插值
     * @param trackPixelCoords
     * @param crossRaster
     * @return
     * @throws JsonProcessingException
     */
    public String getTrackInRasterSequence(int[] trackPixelCoords,
                                          int[][] crossRaster)
            throws JsonProcessingException {
        List<Integer> seq = new ArrayList<>();

        int segmentCount = trackPixelCoords.length / 2 -1;
        for(int i=0;i<segmentCount;i++){
            List<int[]> rasterCoords = getValidRasterCoords(trackPixelCoords[2*i],
                                                            trackPixelCoords[2*i+1],
                                                            trackPixelCoords[2*(i+1)],
                                                            trackPixelCoords[2*(i+1)+1],
                                                            i == segmentCount-1);

            for(int[] coord : rasterCoords){
                seq.add(crossRaster[coord[1]][coord[0]]);
            }
        }

        return JsonUtil.getInstance().write2String(seq);
    }


    /**
     * 获取轨迹在路口行进的二维行程编码,插值
     * @param trackPixelCoords
     * @param crossRaster
     * @return
     */
    public String getTrackCodeInRaster(int[] trackPixelCoords,
                                       short[][] crossRaster){

        SimpleCountCluster encodeUtil = new SimpleCountCluster(255);

        int segmentCount = trackPixelCoords.length/2 - 1;
        for(int i=0;i<segmentCount;i++){
            List<int[]> rasterCoords = getValidRasterCoords(trackPixelCoords[2*i],
                                                            trackPixelCoords[2*i+1],
                                                            trackPixelCoords[2*(i+1)],
                                                            trackPixelCoords[2*(i+1)+1],
                                                            i == segmentCount-1);

            for(int[] coord : rasterCoords){
                encodeUtil.insertHit(crossRaster[coord[1]][coord[0]]);
            }
        }

        return encodeUtil.get2DRunningCode();
    }

    /**
     * 获取轨迹在路口的落点,非插值
     * @param trackPixelCoords
     * @param crossRaster
     * @return
     * @throws JsonProcessingException
     */
    public String getTrackInRasterPoints(int[] trackPixelCoords,
                                        int[][] crossRaster)
            throws JsonProcessingException{
        List<Integer> seq = new ArrayList<>();

        int pagerowcount = crossRaster.length;
        int pagecolcount = crossRaster[0].length;
        for(int i=0; i<trackPixelCoords.length/2; i++){
            int[] rasterCoords = worldToPage(trackPixelCoords[2*i],
                                             trackPixelCoords[2*i+1]);

            int col = rasterCoords[0];
            int row = rasterCoords[1];

            if(col < 0 || row < 0 ||
                    col >= pagecolcount || row >= pagerowcount){
                continue;
            }

            seq.add(crossRaster[row][col]);
        }

        return JsonUtil.getInstance().write2String(seq);
    }

    /**
     * 获取轨迹经过的raster点的插值坐标串
     * @param trackPixelCoords
     * @return
     * @throws JsonProcessingException
     */
    public TrackHitInfo getTrackHittedCoords(int[] trackPixelCoords,
                                            int[][] raster)
            throws JsonProcessingException{

        TrackHitInfo hitInfo = new TrackHitInfo();

        int segmentCount = trackPixelCoords.length / 2 -1;
        for(int i=0;i<segmentCount;i++){
            List<int[]> rasterCoords = getValidRasterCoords(trackPixelCoords[2*i],
                                                            trackPixelCoords[2*i+1],
                                                            trackPixelCoords[2*(i+1)],
                                                            trackPixelCoords[2*(i+1)+1],
                                                            i == segmentCount-1);

            hitInfo.coords.addAll(rasterCoords);
            for(int[] coord : rasterCoords){
                hitInfo.values.add(raster[coord[1]][coord[0]]);
            }
        }

        return hitInfo;
    }

    public TrackHitInfo getTrackHittedPoints(int[] trackPixelCoords,
                                             int[][] raster)
            throws JsonProcessingException {

        TrackHitInfo hitInfo = new TrackHitInfo();

        int ptCount = trackPixelCoords.length / 2;

        int[] index_X = new int[ptCount];
        int[] index_Y = new int[ptCount];
        int[] val = new int[ptCount];

        double[] loc_x = new double[ptCount];
        double[] loc_y = new double[ptCount];

        int rowcount = raster.length, colcount = 0;
        if(raster.length > 0){
            colcount = raster[0].length;
        }

        for (int i = 0; i < ptCount; i++) {

            Coordinate lonlat =
                    mercatorUtil.pixels2LonLat(new IntCoordinate(trackPixelCoords[2 * i],
                                                             trackPixelCoords[2 * i + 1]));

            loc_x[i]=lonlat.x;
            loc_y[i]=lonlat.y;

            int[] rasterCoords = worldToPage(trackPixelCoords[2 * i],
                                            trackPixelCoords[2 * i + 1]);

            index_X[i]=rasterCoords[0];
            index_Y[i]=rasterCoords[1];

            if(rasterCoords[1] >= 0 && rasterCoords[1] < rowcount &&
                    rasterCoords[0] >= 0 && rasterCoords[0] < colcount){
                val[i] = raster[rasterCoords[1]][rasterCoords[0]];
            }else{
                val[i] = 0;
            }
        }

        hitInfo.index.put("x", index_X);
        hitInfo.index.put("y", index_Y);
        hitInfo.index.put("z", val);

        hitInfo.location.put("longitude", loc_x);
        hitInfo.location.put("latitude", loc_y);

        return hitInfo;
    }
}
