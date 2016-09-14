package com.navinfo.mapspotter.process.topic.restriction.io.display;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.geojson.Point;

import java.util.List;

/**
 * 交限路口
 * Created by SongHuiXing on 2016/3/16.
 */
public class RestricCross extends org.geojson.Feature {
    public void setMainPoint(Point pt){
        setGeometry(pt);
    }

    public void setProvinceId(int id){
        setProperty("provinceid", id);
    }

    @JsonIgnore
    public int getProvinceId(){
        return getProperty("provinceid");
    }

    public void setTileCode(String tileCode, int level){
        setProperty("tile", String.format("%s_%d", tileCode, level));
    }

    public void setTiles(List<String> tiles){
        setProperty("tiles", tiles);
    }

    public void setCrossId(long crossId){
        setProperty("crossid", crossId);
    }

    public void setDeleteCount(int count){
        setProperty("delcount", count);
    }
    @JsonIgnore
    public int getDelCount(){
        return getProperty("delcount");
    }

    public void setNewCount(int count){
        setProperty("newcount", count);
    }
    @JsonIgnore
    public int getNewCount(){
        return getProperty("newcount");
    }
}
