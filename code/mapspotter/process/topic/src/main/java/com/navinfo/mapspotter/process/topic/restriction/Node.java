package com.navinfo.mapspotter.process.topic.restriction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.geojson.GeoJsonObject;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by SongHuiXing on 2016/1/18.
 */
public class Node extends org.geojson.Feature{

    public Node(org.geojson.Feature ft){
        this.setProperties(ft.getProperties());
        this.setGeometry(ft.getGeometry());
    }

    @JsonIgnore
    public int getPid(){
        return getProperty("PID");
    }

    @JsonIgnore
    public Point getPoint(){
        return ((Point) getGeometry());
    }

    public static List<Node> convert(List<org.geojson.Feature> fts){
        ArrayList<Node> nodes = new ArrayList<>();

        for(org.geojson.Feature ft : fts){
            nodes.add(new Node(ft));
        }

        return nodes;
    }
}
