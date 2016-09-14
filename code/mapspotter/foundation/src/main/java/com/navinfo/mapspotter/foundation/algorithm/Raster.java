package com.navinfo.mapspotter.foundation.algorithm;

import java.util.*;

/**
 * Created by SongHuiXing on 2016/1/3.
 */
public class Raster {

    public static class RasterCoordCompartor implements Comparator<int[]> {

        @Override
        public int compare(int[] o1, int[] o2) {
            if(o1[1] != o2[1])
                return o1[1] - o2[1];

            return o1[0] - o2[0];
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;

            RasterCoordCompartor that = (RasterCoordCompartor)obj;

            if(null == that)
                return false;

            return true;
        }
    }

    /**
     * 通过Bresenham方法绘制栅格直线 @link https://de.wikipedia.org/wiki/Bresenham-Algorithmus
     * @param x0 起始点所在列
     * @param y0 起始点所在行
     * @param x1 终止点所在列
     * @param y1 终止点所在行
     * @return 获取[起点,终点)区间的栅格坐标List<列号,行号>
     */
    public static List<int[]> getBresenhamline(int x0, int y0, int x1, int y1){
        List<int[]> coords = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x1 > x0 ? 1 : -1;
        int sy = y1 > y0 ? 1 : -1;

        boolean isVerticalLike = false;

        if(dy > dx){
            int temp = dy;
            dy = dx;
            dx = temp;

            isVerticalLike = true;
        }

        int eps = 2 * dy - dx;
        int loopx = x0, loopy = y0;
        for(int i=0; i<dx; i++){

            coords.add(new int[]{loopx, loopy});

            if(eps > 0){
                if(isVerticalLike){
                    loopx = loopx + sx;
                } else{
                    loopy = loopy + sy;
                }

                eps = eps - 2 * dx;
            }

            if(isVerticalLike){
                loopy = loopy + sy;
            } else{
                loopx = loopx + sx;
            }

            eps = eps + 2 * dy;
        }

        return coords;
    }

    /**
     * 结合两步画线法及对称方法实现的4点Bresenham栅格直线
     * @param x0 起始点所在列
     * @param y0 起始点所在行
     * @param x1 终止点所在列
     * @param y1 终止点所在行
     * @return 获取[起点,终点)区间的栅格坐标List<列号,行号>
     */
    public static List<int[]> getAdvanceBresenhamline(int x0, int y0,int x1, int y1){
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int stepX = x1 > x0 ? 1 : -1;
        int stepY = y1 > y0 ? 1 : -1;

        boolean isVerticalLike = false;

        if(dy > dx){
            int temp = dy;
            dy = dx;
            dx = temp;
            isVerticalLike = true;
        }

        BresenhamPointCollection ptCollector = new BresenhamPointCollection(x0,y0,x1,y1,stepX,stepY,!isVerticalLike);

        //从起始端方向需要生成的点数
        int startDriectDrawCount = (int)Math.ceil(dx / 2.0);

        //循环计算次数
        int totalLoop = (int)Math.ceil(startDriectDrawCount /2.0);
        startDriectDrawCount = totalLoop * 2;

        //终止端比起始端少的点数
        int endDirectLessThan = 2 * startDriectDrawCount - dx;

        int eps = 3*dx - 4*dy;
        for(int i=0; i < totalLoop; i++){

            if(eps < 0){
                ptCollector.collectDiagPoints();
            } else if(eps >= 0 && eps < dx){
                ptCollector.collectHighPoints();
            } else if(eps >= dx && eps < 2*dx){
                ptCollector.collectLowPoints();
            } else if(eps >= 2*dx){
                ptCollector.collectBottomPoints();
            }

            if(eps < 0)
                eps = eps + 4*dx - 4*dy;
            else if(eps >=0 && eps < 2*dx)
                eps = eps + 2*dx - 4*dy;
            else if(eps >= 2*dx)
                eps = eps - 4*dy;
        }

        List<int[]> sCoords = ptCollector.getStartPoints();

        Stack<int[]> eCoords = ptCollector.getEndPoints();

        while(endDirectLessThan >0 && !eCoords.isEmpty()){
            eCoords.pop();
            endDirectLessThan--;
        }

        sCoords.addAll(eCoords);

        return sCoords;
    }


    /**
     * 绘制椭圆
     * @param centerx   圆心x
     * @param centery   圆心y
     * @param a         长半轴
     * @param b         短半轴
     * @return 坐标List<列号,行号>
     */
    public static List<int[]> getBresenhamEllipse(int centerx, int centery, int a, int b){
        ArrayList<int[]> ellipsePts = new ArrayList<>();

        int powerA = a * a;
        int powerB = b * b;

        int x=0, y=b;
        int d = 2 * powerB - 2 * b * powerA + powerA;
        ellipsePts.addAll(draw4PtsOfEllipse(centerx, centery, x, y));

        int p_x = (int)Math.round((double)powerA / Math.sqrt((double)(powerA+powerB)));

        while (x <= p_x){   //绘制上半部分
            if(d >= 0){
                d -= 4 * powerA * (y -1);
                y--;
            }

            d += 2 * powerB * (2*x + 3);
            x++;

            ellipsePts.addAll(draw4PtsOfEllipse(centerx, centery, x, y));
        }

        d = powerB * (x*x + x) + powerA * (y*y + y) - powerA*powerB;
        while (y >= 0){     //绘制下半部分
            ellipsePts.addAll(draw4PtsOfEllipse(centerx, centery, x, y));

            y--;
            d = d - powerA * (2 * y - 1);

            if(d < 0){
                x++;
                d = d + 2 * powerB * (x + 1);
            }
        }

        Collections.sort(ellipsePts, new Raster.RasterCoordCompartor());

        return ellipsePts;
    }

    /**
     * 绘制椭圆上的4个点(如果点在顶点上的话只有两个)
     * @param centerx   圆心x
     * @param centery   圆心y
     * @param x         以原点为圆心的椭圆上一点的x坐标
     * @param y         以原点为圆心的椭圆上一点的y坐标
     * @return 坐标List<列号,行号>
     */
    private static List<int[]> draw4PtsOfEllipse(int centerx, int centery, int x, int y){
        ArrayList<int[]> pts = new ArrayList<>();

        pts.add(new int[]{centerx + x, centery + y});

        if(0 != x){
            pts.add(new int[]{centerx - x, centery + y});

            if(0 != y){
                pts.add(new int[]{centerx + x, centery - y});
                pts.add(new int[]{centerx - x, centery - y});
            }
        } else {
            pts.add(new int[]{centerx + x, centery - y});
        }

        return pts;
    }
}

class BresenhamPointCollection{

    private int sCursor_x;
    private int sCursor_y;

    private int eCursor_x;
    private int eCursor_y;

    private int stepX;
    private int stepY;

    private boolean isHorizen = false;

    List<int[]> startPoints;
    public List<int[]> getStartPoints() {
        return startPoints;
    }

    Stack<int[]> endPoints;
    public Stack<int[]> getEndPoints() {
        return endPoints;
    }

    BresenhamPointCollection(int sx, int sy, int ex, int ey,
                             int stepx, int stepy,
                             boolean horizen) {
        startPoints = new ArrayList<>();
        endPoints = new Stack<>();

        sCursor_x = sx;
        sCursor_y = sy;

        eCursor_x = ex;
        eCursor_y = ey;

        stepX = stepx;
        stepY = stepy;

        this.isHorizen = horizen;
    }

    /**
     *    x......x......o
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    x......o......x
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    o......x......x
     */
    public void collectDiagPoints(){
        startPoints.add(new int[]{sCursor_x+stepX, sCursor_y+stepY});
        startPoints.add(new int[]{sCursor_x+2*stepX, sCursor_y+2*stepY});
        sCursor_x += 2*stepX;
        sCursor_y += 2*stepY;

        endPoints.push(new int[]{eCursor_x-stepX, eCursor_y-stepY});
        endPoints.push(new int[]{eCursor_x-2*stepX, eCursor_y-2*stepY});
        eCursor_x -= 2*stepX;
        eCursor_y -= 2*stepY;
    }

    /**
     *    x......x......x
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    x......o......o
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    o......x......x
     */
    public void collectHighPoints(){
        if(isHorizen){
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y+stepY});
            startPoints.add(new int[]{sCursor_x+2*stepX, sCursor_y+stepY});
            sCursor_x += 2*stepX;
            sCursor_y += stepY;

            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y-stepY});
            endPoints.push(new int[]{eCursor_x-2*stepX, eCursor_y-stepY});
            eCursor_x -= 2*stepX;
            eCursor_y -= stepY;
        }else{
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y+stepY});
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y+2*stepY});
            sCursor_x += stepX;
            sCursor_y += 2*stepY;

            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y-stepY});
            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y-2*stepY});
            eCursor_x -= stepX;
            eCursor_y -= 2*stepY;
        }
    }

    /**
     *    x......x......x
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    x......x......o
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    o......o......x
     */
    public void collectLowPoints(){
        if(isHorizen){
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y});
            startPoints.add(new int[]{sCursor_x+2*stepX, sCursor_y+stepY});
            sCursor_x += 2*stepX;
            sCursor_y += stepY;

            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y});
            endPoints.push(new int[]{eCursor_x-2*stepX, eCursor_y-stepY});
            eCursor_x -= 2*stepX;
            eCursor_y -= stepY;
        }else{
            startPoints.add(new int[]{sCursor_x, sCursor_y+stepY});
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y+2*stepY});
            sCursor_x += stepX;
            sCursor_y += 2*stepY;

            endPoints.push(new int[]{eCursor_x, eCursor_y-stepY});
            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y-2*stepY});
            eCursor_x -= stepX;
            eCursor_y -= 2*stepY;
        }
    }

    /**
     *    x......x......x
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    x......x......x
     *    .      .      .
     *    .      .      .
     *    .      .      .
     *    o......o......o
     */
    public void collectBottomPoints(){
        if(isHorizen){
            startPoints.add(new int[]{sCursor_x+stepX, sCursor_y});
            startPoints.add(new int[]{sCursor_x+2*stepX, sCursor_y});
            sCursor_x += 2*stepX;

            endPoints.push(new int[]{eCursor_x-stepX, eCursor_y});
            endPoints.push(new int[]{eCursor_x-2*stepX, eCursor_y});
            eCursor_x -= 2*stepX;
        }else{
            startPoints.add(new int[]{sCursor_x, sCursor_y+stepY});
            startPoints.add(new int[]{sCursor_x, sCursor_y+2*stepY});
            sCursor_y += 2*stepY;

            endPoints.push(new int[]{eCursor_x, eCursor_y-stepY});
            endPoints.push(new int[]{eCursor_x, eCursor_y-2*stepY});
            eCursor_y -= 2*stepY;
        }
    }
}
