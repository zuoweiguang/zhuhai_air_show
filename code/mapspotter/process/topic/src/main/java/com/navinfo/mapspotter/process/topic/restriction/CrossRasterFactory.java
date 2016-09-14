package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.Raster;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.MercatorUtil;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.vividsolutions.jts.geom.Coordinate;
import org.geojson.LngLatAlt;

import java.util.*;

/**
 * 路口buffer的工厂
 * Created by SongHuiXing on 2016/1/26.
 */
public class CrossRasterFactory {
    private final static int lnglat2meter_factor = 100000;
    private final static double mini_crossarea_radius = 10.0 / lnglat2meter_factor;
    private final static double pixel_width = 2.5;
    private final static int mini_crossarea_pixel = (int)Math.ceil(10.0 / pixel_width);

    private MercatorUtil mercator = null;

    private RoadRasterSupplier roadRasterVistor = null;

    private int tilesize = 1024;

    private int crossarea_time = 2;    //crossarea = crossenv * 2
    private int page_time = 10;         //page = crossarea * 10

    public CrossRasterFactory(int tilelevel,
                              int tilesize,
                              RoadRasterSupplier roadRasterSupplier){
        this(tilelevel, tilesize, roadRasterSupplier, 2, 10);
    }

    public CrossRasterFactory(int tilelevel,
                              int tilesize,
                              RoadRasterSupplier roadRasterSupplier,
                              int cross_time,
                              int page_time){

        roadRasterVistor = roadRasterSupplier;
        mercator = new MercatorUtil(tilesize, tilelevel);
        this.tilesize = tilesize;

        this.crossarea_time = cross_time;
        this.page_time = page_time;
    }

    public boolean prepare(){
        return roadRasterVistor.prepare();
    }

    public void shutdown(){
        roadRasterVistor.shutdown();
    }

    private long current_cross_pid = 0;

    /**
     * 建立路口栅格图
     * 栅格图的y坐标从上至下递增
     * @param pid       路口pid
     * @param links     路口外link
     * @param nodes     路口点
     * @return
     */
    public CrossRaster buildRaster(long pid,
                                   List<Link> links,
                                   List<Node> nodes) {

        CrossRaster raster = new CrossRaster();

        raster.setPid(pid);

        this.current_cross_pid = pid;

        try {

            List<double[]> crossAreas = getCrossAndPageArea(nodes, crossarea_time, page_time);

            double[] center = crossAreas.get(0);

            double[] crossArea = crossAreas.get(1);
            raster.setCrossEnvelope(crossArea);

            double[] pageenv = crossAreas.get(2);
            raster.setPageEnvelope(pageenv);

            //切割道路底图构建基础栅格
            ArrayList<Map.Entry<String, IntCoordinate>> tileInfos = new ArrayList<>();
            org.jblas.DoubleMatrix roadRaster = buildRoadRaster(pageenv, tileInfos);

            raster.setCorner_tile_pos(tileInfos);

            //从组合好的栅格中将不关心的噪声去除
            org.jblas.DoubleMatrix noisePos = roadRaster.ne(0);
            for (Link l : links) {
                for (int linkpid : l.getChainlinks()) {
                    noisePos = noisePos.andi(roadRaster.ne(linkpid));
                }
            }
            roadRaster = roadRaster.put(noisePos, 0);

            //更改linkpid为对应序号
            for (int i = 1; i <= links.size(); i++) {
                Link l = links.get(i - 1);

                for (int linkpid : l.getChainlinks()) {
                    roadRaster = roadRaster.put(roadRaster.eq(linkpid), i);
                }
            }

            //将路口范围打点到道路底图上
            List<int[]> bound = getCorssAreaBound(nodes, center,
                    new double[]{pageenv[0], pageenv[3]});

            DoubleMatrix mx = new DoubleMatrix(makeCrossArea(roadRaster, bound));
            raster.setRasterColCount(mx.columns);

            DoubleMatrix.SparseMatrix sparse = mx.toSparse();

            raster.setSparseRaster(new int[][]{sparse.data, sparse.indices, sparse.indptr});
        } catch (Exception e){
            throw e;
        }

        return raster;
    }

    /**
     * 构建指定范围的道路栅格
     * @param pageenv   指定的范围，经纬度坐标
     * @return
     */
    public org.jblas.DoubleMatrix buildRoadRaster(double[] pageenv,
                                                  ArrayList<Map.Entry<String, IntCoordinate>> tileInfomatrion){

        //计算16级墨卡托tile下的绝对像素坐标
        Coordinate lb = new Coordinate(pageenv[0], pageenv[1]);
        Coordinate lt = new Coordinate(pageenv[0], pageenv[3]);
        Coordinate rt = new Coordinate(pageenv[2], pageenv[3]);
        Coordinate rb = new Coordinate(pageenv[2], pageenv[1]);

        //计算该像素坐标在14级tile相关信息
        tileInfomatrion.addAll(getTileInfo(lb, lt, rt, rb));

        IntCoordinate relativeLB = tileInfomatrion.get(0).getValue();
        String lbTileCode = tileInfomatrion.get(0).getKey();

        IntCoordinate relativeLT = tileInfomatrion.get(1).getValue();
        String ltTileCode = tileInfomatrion.get(1).getKey();

        IntCoordinate relativeRT = tileInfomatrion.get(2).getValue();
        String rtTileCode = tileInfomatrion.get(2).getKey();

        IntCoordinate relativeRB = tileInfomatrion.get(3).getValue();
        String rbTileCode = tileInfomatrion.get(3).getKey();

        boolean isVerticalSame = lbTileCode.equals(ltTileCode);

        boolean isHorizenSame = ltTileCode.equals(rtTileCode);

        IntCoordinate lbPixel = mercator.lonLat2Pixels(lb);
        IntCoordinate rtPixel = mercator.lonLat2Pixels(rt);

        int rasterRowCount = lbPixel.y - rtPixel.y + 1;
        int rasterColCount = rtPixel.x - lbPixel.x + 1;

        DoubleMatrix raster;

        if(isVerticalSame && isHorizenSame){
            org.jblas.DoubleMatrix roadTile = roadRasterVistor.getRoadRaster(lbTileCode);

            raster = new DoubleMatrix(roadTile,
                                    relativeRT.y,
                                    relativeLB.y,
                                    relativeLB.x,
                                    relativeRT.x);

        } else if(isVerticalSame){
            org.jblas.DoubleMatrix leftTile = roadRasterVistor.getRoadRaster(lbTileCode);
            org.jblas.DoubleMatrix rightTile = roadRasterVistor.getRoadRaster(rtTileCode);

            raster = new DoubleMatrix(rasterRowCount, rasterColCount);

            raster = raster
                    .copyFromLeftbottom(leftTile, relativeLT.y, relativeLB.y, relativeLB.x, tilesize-1)
                    .copyFromRightbottom(rightTile, relativeRT.y, relativeRB.y, 0, relativeRB.x);

        } else if(isHorizenSame){
            org.jblas.DoubleMatrix bottomTile = roadRasterVistor.getRoadRaster(lbTileCode);
            org.jblas.DoubleMatrix topTile = roadRasterVistor.getRoadRaster(ltTileCode);

            raster = new DoubleMatrix(rasterRowCount, rasterColCount);

            raster = raster
                    .copyFromLeftbottom(bottomTile, 0, relativeLB.y, relativeLB.x, relativeRB.x)
                    .copyFromLefttop(topTile, relativeLT.y, tilesize-1, relativeLT.x, relativeRT.x);
        } else {
            org.jblas.DoubleMatrix lbTile = roadRasterVistor.getRoadRaster(lbTileCode);
            org.jblas.DoubleMatrix ltTile = roadRasterVistor.getRoadRaster(ltTileCode);
            org.jblas.DoubleMatrix rtTile = roadRasterVistor.getRoadRaster(rtTileCode);
            org.jblas.DoubleMatrix rbTile = roadRasterVistor.getRoadRaster(rbTileCode);

            raster = new DoubleMatrix(rasterRowCount, rasterColCount);

            raster = raster
                    .copyFromLeftbottom(lbTile, 0, relativeLB.y, relativeLB.x, tilesize-1)
                    .copyFromLefttop(ltTile, relativeLT.y, tilesize-1, relativeLT.x, tilesize-1)
                    .copyFromRightbottom(rbTile, 0, relativeRB.y, 0, relativeRB.x)
                    .copyFromRighttop(rtTile, relativeRT.y, tilesize-1, 0, relativeRT.x);
        }

        return raster;
    }

    /**
     * 获取4个点相关的14级tile信息
     * @param lb
     * @param lt
     * @param rt
     * @param rb
     * @return key--tile号码，value--tile内像素坐标
     */
    public ArrayList<Map.Entry<String, IntCoordinate>> getTileInfo(Coordinate lb, Coordinate lt,
                                                                   Coordinate rt, Coordinate rb){
        ArrayList<Map.Entry<String, IntCoordinate>> tileInfomatrion = new ArrayList<>(4);

        IntCoordinate relativeLB = mercator.lonLat2InTile(lb);
        String lbTileCode = mercator.lonLat2MCode(lb);
        tileInfomatrion.add(new AbstractMap.SimpleEntry<>(lbTileCode, relativeLB));

        IntCoordinate relativeLT = mercator.lonLat2InTile(lt);
        String ltTileCode = mercator.lonLat2MCode(lt);
        tileInfomatrion.add(new AbstractMap.SimpleEntry<>(ltTileCode, relativeLT));

        IntCoordinate relativeRT = mercator.lonLat2InTile(rt);
        String rtTileCode = mercator.lonLat2MCode(rt);
        tileInfomatrion.add(new AbstractMap.SimpleEntry<>(rtTileCode, relativeRT));

        IntCoordinate relativeRB = mercator.lonLat2InTile(rb);
        String rbTileCode = mercator.lonLat2MCode(rb);
        tileInfomatrion.add(new AbstractMap.SimpleEntry<>(rbTileCode, relativeRB));

        return tileInfomatrion;
    }

    /**
     * 根据路口点获取路口范围
     * @param nodes
     * @param minPt
     * @return
     */
    private List<int[]> getCorssAreaBound(List<Node> nodes,
                                          double[] center,
                                          double[] minPt){

        //路口凸包
        Coordinate[] coords = new Coordinate[nodes.size()];
        for(int i=0;i<nodes.size();i++){
            LngLatAlt lonlat = nodes.get(i).getPoint().getCoordinates();
            coords[i] = new Coordinate(lonlat.getLongitude(), lonlat.getLatitude());
        }

        Coordinate[] converxHull = SpatialUtil.getConvexHull(coords);

        IntCoordinate pageLTPixel = mercator.lonLat2Pixels(new Coordinate(minPt[0], minPt[1]));

        IntCoordinate centerPixel = mercator.lonLat2Pixels(new Coordinate(center[0], center[1]));
        centerPixel.x = centerPixel.x - pageLTPixel.x;
        centerPixel.y = centerPixel.y - pageLTPixel.y;

        List<int[]> allBoundPts = null;

        int hullPtCount = converxHull.length;
        switch (hullPtCount){
            case 1: {
                int radius = mini_crossarea_pixel * crossarea_time;
                allBoundPts = Raster.getBresenhamEllipse(centerPixel.x, centerPixel.y, radius, radius);
                break;
            }
            default: {
                IntCoordinate sPixel = null;
                IntCoordinate ePixel = null;

                int b = mini_crossarea_pixel * crossarea_time;

                if(hullPtCount > 2) {
                    Coordinate[] anchorPts = SpatialUtil.getMaxAntipode(converxHull);

                    sPixel = mercator.lonLat2Pixels(anchorPts[0]);
                    ePixel = mercator.lonLat2Pixels(anchorPts[1]);

                    double dis = SpatialUtil.getMaxPerpendicularDistance(converxHull, anchorPts);
                    dis = dis < mini_crossarea_radius ? mini_crossarea_radius : dis;
                    b = (int)Math.ceil(dis * lnglat2meter_factor / pixel_width) * crossarea_time;

                } else {
                    sPixel = mercator.lonLat2Pixels(converxHull[0]);
                    ePixel = mercator.lonLat2Pixels(converxHull[1]);
                }

                int[] s = new int[]{sPixel.x - pageLTPixel.x, sPixel.y - pageLTPixel.y};
                int[] e = new int[]{ePixel.x - pageLTPixel.x, ePixel.y - pageLTPixel.y};

                int a = (int) Math.ceil(SpatialUtil.getDistance(s[0], s[1], e[0], e[1])) * crossarea_time;

                allBoundPts = Raster.getBresenhamEllipse(centerPixel.x, centerPixel.y, a, b);

                allBoundPts = SpatialUtil.transform(allBoundPts, 0, -centerPixel.x, -centerPixel.y);

                //旋转
                double angle = 0;
                if(s[1] == e[1])
                    angle = Math.PI / 2;
                else {
                    angle = Math.atan((double) (e[1] - s[1])/(double)(e[0] - s[0]));
                }

                allBoundPts = SpatialUtil.transform(allBoundPts, angle, centerPixel.x, centerPixel.y);

                break;
            }
        }

        return allBoundPts;
    }

    /**
     * 获取凸包以center为中心放大并插值后的坐标串
     * @param hull
     * @param center
     * @return
     */
    private List<int[]> getConverxHullZoomBound(List<int[]> hull, IntCoordinate center){

        //凸包缩放
        List<int[]> trans = SpatialUtil.transform(hull, 0, -center.x, -center.y);

        List<double[]> src = new ArrayList<>();
        for (int[] c : trans) {
            src.add(new double[]{c[0], c[1]});
        }
        src = SpatialUtil.zoom(src, crossarea_time, crossarea_time);

        trans.clear();
        for (double[] c : src) {
            trans.add(new int[]{(int) Math.round(c[0]), (int) Math.round(c[1])});
        }
        trans = SpatialUtil.transform(trans, 0, center.x, center.y);

        //绘制凸包
        ArrayList<int[]> allBoundPts = new ArrayList<>();

        int ptCount = trans.size();
        int[] s = trans.get(0);
        for (int i = 1; i <= ptCount; i++) {
            int[] e = trans.get(i % ptCount);
            List<int[]> line = Raster.getAdvanceBresenhamline(s[0], s[1], e[0], e[1]);
            allBoundPts.addAll(line);
            allBoundPts.add(e);

            s = e;
        }

        return allBoundPts;
    }

    /**
     * 将路口范围内所有栅格上色
     * @param raster
     * @param bounds
     * @return
     */
    private org.jblas.DoubleMatrix makeCrossArea(org.jblas.DoubleMatrix raster,
                                                 List<int[]> bounds){
        Raster.RasterCoordCompartor compartor = new Raster.RasterCoordCompartor();
        Collections.sort(bounds, compartor);

        Collection<List<int[]>> groupRows = groupRow(bounds);

        TreeMap<Integer, int[]> min_max_perRow = new TreeMap<>();

        for(List<int[]> rowPixel : groupRows){
            int mincol = rowPixel.get(0)[0];
            int maxcol = rowPixel.get(rowPixel.size()-1)[0];
            min_max_perRow.put(rowPixel.get(0)[1], new int[]{mincol, maxcol});
        }

        int minrow = min_max_perRow.firstKey();
        int maxrow = min_max_perRow.lastKey();

        //平滑
        int smoothCount = smoothRange(min_max_perRow, minrow, maxrow);

        int[] minmaxCol = null;
        for (int row = minrow; row <= maxrow; row++) {
            if(min_max_perRow.containsKey(row)) {
                minmaxCol = min_max_perRow.get(row);
            }

            int colCount = minmaxCol[1] - minmaxCol[0] + 1;
            int[] indices = new int[colCount];
            for (int i = 0; i < colCount; i++) {
                indices[i] = minmaxCol[0] + i;
            }

            raster.put(row, indices, 255);
        }

        return raster;
    }

    /**
     * 获取路口的中心点、路口范围及路口页面范围
     * @param nodes
     * @return [路口中线点，路口范围，页面范围]
     */
    public static List<double[]> getCrossAndPageArea(List<Node> nodes, int crosstime, int pagetime){
        ArrayList<double[]> areas = new ArrayList<>(2);

        double[] crossenv = getCrossEnvelope(nodes);
        double[] crossArea = SpatialUtil.zoomEnvelopeInCenter(crossenv, crosstime, crosstime);

        double[] center = new double[]{(crossenv[0]+crossenv[2])/2,
                                        (crossenv[1]+crossenv[3])/2};

        areas.add(center);

        areas.add(crossArea);

        double[] pageenv = SpatialUtil.zoomEnvelopeInCenter(crossArea, pagetime, pagetime);
        areas.add(pageenv);

        return areas;
    }

    private static double[] getCrossEnvelope(List<Node> nodes){
        if(0 == nodes.size())
            return new double[]{0, 0, 0, 0};

        LngLatAlt lonlat = nodes.get(0).getPoint().getCoordinates();

        double[] envelope = new double[]{lonlat.getLongitude(), lonlat.getLatitude(),
                lonlat.getLongitude(), lonlat.getLatitude()};

        for(int i=1;i<nodes.size();i++){
            lonlat = nodes.get(i).getPoint().getCoordinates();
            double x = lonlat.getLongitude();
            double y = lonlat.getLatitude();

            if(envelope[0] > x)
                envelope[0] = x;
            else if(envelope[2] < x)
                envelope[2] = x;

            if(envelope[1] > y)
                envelope[1] = y;
            else if(envelope[3] < y)
                envelope[3] = y;
        }

        if(envelope[2] - envelope[0] < mini_crossarea_radius){
            double centerx = (envelope[2] + envelope[0]) /2;
            envelope[0] = centerx - mini_crossarea_radius/2;
            envelope[2] = centerx + mini_crossarea_radius/2;
        }

        if(envelope[3] - envelope[1] < mini_crossarea_radius) {
            double centery = (envelope[3] + envelope[1]) /2;
            envelope[1] = centery - mini_crossarea_radius/2;
            envelope[3] = centery + mini_crossarea_radius/2;
        }

        return envelope;
    }

    /**
     * 平滑一个范围
     * @param rangeMap  各个行的列情况
     * @param minrow    平滑起始行
     * @param maxrow    平滑结束行
     * @return  平滑处理的异常数量
     */
    private static int smoothRange(TreeMap<Integer, int[]> rangeMap, int minrow, int maxrow){
        int smoothCount = 0;

        int[] preCol = rangeMap.get(minrow);
        int preColCt = preCol[1] - preCol[0] +1;

        for (int row = minrow+1; row < maxrow; row++) {
            int[] curCol = rangeMap.get(row);

            if(null == preCol || null == curCol)
                continue;

            int curColCt = curCol[1] - curCol[0] +1;

            if(curColCt < preColCt){
                for (int i = 1; i < 3; i++) {
                    int[] nextCol = rangeMap.get(row+i);
                    if(null == nextCol)
                        continue;

                    int nextColCt = nextCol[1] - nextCol[0] + 1;
                    if (curColCt < nextColCt) {
                        curCol = preCol;
                        curColCt = preColCt;
                        rangeMap.put(row, curCol);

                        smoothCount++;
                        break;
                    }
                }
            }

            preCol = curCol;
            preColCt = curColCt;
        }

        return smoothCount;
    }

    private static Collection<List<int[]>> groupRow(List<int[]> pixels){
        SortedMap<Integer, List<int[]>> groups = new TreeMap<>();

        for(int[] pixel : pixels){
            List<int[]> rows = null;
            if(groups.containsKey(pixel[1])){
                rows = groups.get(pixel[1]);
                rows.add(pixel);
            } else {
                rows = new ArrayList<>();
                rows.add(pixel);
                groups.put(pixel[1], rows);
            }
        }

        return groups.values();
    }
}
