package com.navinfo.mapspotter.foundation.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by SongHuiXing on 2016/1/3.
 */
public class Raster {

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

        int eps = 4*dy;

        BresenhamPointCollection ptCollector = new BresenhamPointCollection(x0,y0,x1,y1,stepX,stepY);

        //从起始端方向需要生成的点数
        int startDriectDrawCount = (int)Math.ceil((dx - 1) / 2.0);

        //循环计算次数
        int totalLoop = (int)Math.ceil(startDriectDrawCount /2.0);
        startDriectDrawCount = totalLoop * 2;

        //终止端比起始端少的点数
        int endDirectLessThan = 2 * startDriectDrawCount - dx + 1;

        for(int i=0; i < totalLoop; i++){
            if(eps <= 0)
                eps += 4*dy;
            else if(eps >0 && eps <= 2*dx)
                eps = eps + 4*dy -2*dx;
            else
                eps = eps + 4*dy -4*dx;

            if(eps < 0){
                ptCollector.collectDiagPoints();
            } else if(eps >= 0 && eps < dx){
                ptCollector.collectHighPoints(!isVerticalLike);
            } else if(eps >= dx && eps < 2*dx){
                ptCollector.collectLowPoints(!isVerticalLike);
            } else {
                ptCollector.collectBottomPoints(!isVerticalLike);
            }
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
}

class BresenhamPointCollection{

    private int sCursor_x;
    private int sCursor_y;

    private int eCursor_x;
    private int eCursor_y;

    private int stepX;
    private int stepY;

    List<int[]> startPoints;
    public List<int[]> getStartPoints() {
        return startPoints;
    }

    Stack<int[]> endPoints;
    public Stack<int[]> getEndPoints() {
        return endPoints;
    }

    BresenhamPointCollection(int sx, int sy, int ex, int ey,
                             int stepx, int stepy) {
        startPoints = new ArrayList<>();
        endPoints = new Stack<>();

        sCursor_x = sx;
        sCursor_y = sy;

        eCursor_x = ex;
        eCursor_y = ey;

        stepX = stepx;
        stepY = stepy;
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
    public void collectHighPoints(boolean isHorizen){
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
    public void collectLowPoints(boolean isHorizen){
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
    public void collectBottomPoints(boolean isHorizen){
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
