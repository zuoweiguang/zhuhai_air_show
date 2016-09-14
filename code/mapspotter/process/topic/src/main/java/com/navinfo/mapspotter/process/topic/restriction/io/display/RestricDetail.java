package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.vividsolutions.jts.geom.Coordinate;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * 每条交限的显示类
 * Created by SongHuiXing on 2016/3/16.
 */
public class RestricDetail extends org.geojson.Feature {

    public void setDisplayPoint(Point pt) {
        setGeometry(pt);
    }

    public void setProvinceId(int id){
        setProperty("provinceid", id);
    }

    public void setTileCode(String tileCode, int level){
        setProperty("tile", String.format("%s_%d", tileCode, level));
    }

    public void setTiles(List<String> tiles){
        setProperty("tiles", tiles);
    }

    public void setRestricDir(int dir){
        setProperty("res", dir);
    }

    public void setCrossId(long crossId){
        setProperty("crossid", crossId);
    }

    public void setIsDelete(boolean isDelete){
        if(isDelete){
            setProperty("type", 1);
        } else {
            setProperty("type", 2);
        }
    }

    public void setConfidence(int confidence){
        setProperty("confidence", confidence);
    }

    public void setInLinkId(int id){
        setProperty("linkinid", id);
    }
    public void setInLinkGeo(com.vividsolutions.jts.geom.LineString linkGeo){
        setProperty("linkin", toList(linkGeo));
    }

    public void setOutLinkId(int id){
        setProperty("linkoutid", id);
    }
    public void setOutLinkGeo(com.vividsolutions.jts.geom.LineString linkGeo){
        setProperty("linkout", toList(linkGeo));
    }

    private static ArrayList<double[]> toList(com.vividsolutions.jts.geom.LineString line){
        ArrayList<double[]> coords = new ArrayList<>();

        Coordinate[] allPts = line.getCoordinates();

        for (Coordinate co : allPts){
            coords.add(new double[]{co.x, co.y});
        }

        return coords;
    }
}
