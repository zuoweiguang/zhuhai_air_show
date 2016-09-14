package com.navinfo.mapspotter.process.topic.restriction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geojson.LineString;
import org.geojson.LngLatAlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by SongHuiXing on 2016/1/18.
 */
public class Link extends org.geojson.Feature {

    private static GeometryFactory gfact = new GeometryFactory();

    public Link(org.geojson.Feature ft){
        this.setProperties(ft.getProperties());
        this.setGeometry(ft.getGeometry());
    }

    @JsonIgnore
    public int getPid(){
        return getProperty("PID");
    }

    @JsonIgnore
    public LineString getLine(){
        return ((LineString) getGeometry());
    }

    @JsonIgnore
    public LngLatAlt getStartPoint(){
        LineString line = getLine();

        return line.getCoordinates().get(0);
    }

    @JsonIgnore
    public LngLatAlt getEndPoint(){
        LineString line = getLine();

        List<LngLatAlt> coords = line.getCoordinates();

        return coords.get(coords.size() - 1);
    }

    @JsonIgnore
    public List<Integer> getChainlinks(){
        return getProperty("Chain");
    }

    /**
     * 获取Link几何
     * @param isFollow  通行方向是否是顺向
     * @return
     */
    @JsonIgnore
    public com.vividsolutions.jts.geom.LineString getLineString(boolean isFollow){
        List<LngLatAlt> line = getLine().getCoordinates();

        if(!isFollow){
            Collections.reverse(line);
        }

        Coordinate[] coords = new Coordinate[line.size()];

        for (int i = 0; i < line.size(); i++) {
            LngLatAlt pt = line.get(i);
            coords[i] = new Coordinate(pt.getLongitude(), pt.getLatitude());
        }

        return gfact.createLineString(coords);
    }

    public static List<Link> convert(List<org.geojson.Feature> fts){
        ArrayList<Link> links = new ArrayList<>();

        for(org.geojson.Feature ft : fts){
            links.add(new Link(ft));
        }

        return links;
    }
}
