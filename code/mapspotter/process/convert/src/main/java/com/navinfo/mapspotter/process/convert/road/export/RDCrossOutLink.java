package com.navinfo.mapspotter.process.convert.road.export;

import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import org.geojson.Feature;
import org.geojson.LngLatAlt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 路口外Link
 * Created by SongHuiXing on 2016/4/5.
 */
public class RDCrossOutLink {

    private RDCrossOutLink(int linkpid, int dir, int crossnodeid, boolean isStart){
        link_pid = linkpid;
        direction = dir;
        crossnode_id = crossnodeid;
        crossnode_is_linkstart = isStart;
    }

    private final int link_pid;
    public int getLinkPid(){
        return link_pid;
    }

    //direct：Link相对于路口的通行方向 1 双方向 2 进入 3 退出
    private final int direction;
    public int getDirection(){
        return direction;
    }

    private final int crossnode_id;
    public int getCrossNode(){
        return crossnode_id;
    }

    private final boolean crossnode_is_linkstart;
    public boolean getCrossNodeIsStart(){
        return crossnode_is_linkstart;
    }

    private LineString geometry = null;

    private double length = 0;
    public double getLength(){
        return length;
    }

    private HashMap<Integer, Integer> restrictions = null;
    public HashMap<Integer, Integer> getRestrictions(){
        return restrictions;
    }

    private List<Integer> chains = null;
    public List<Integer> getChain(){
        return chains;
    }

    public double[] getEnvelope(){
        Envelope env = geometry.getEnvelopeInternal();

        return new double[]{env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY()};
    }

    public Feature toJsonFeature(){
        Feature ft = new Feature();
        org.geojson.LineString line = new org.geojson.LineString();
        for(Coordinate coord : geometry.getCoordinates()){
            line.add(new LngLatAlt(coord.x, coord.y));
        }
        ft.setGeometry(line);
        ft.setProperty("PID", link_pid);
        ft.setProperty("Chain", chains);
        ft.setProperty("Length", length);

        return ft;
    }

    public static RDCrossOutLink getLink(int linkpid, int dir,
                                         int crossnodeid,
                                         boolean isStart,
                                         boolean getBranch,
                                         OracleStatementContainer oracle){
        RDCrossOutLink link = new RDCrossOutLink(linkpid, dir, crossnodeid, isStart);

        try(SqlCursor cursor = oracle.queryLinkGeo(linkpid)){
            if(!cursor.next())
                return null;

            link.length = cursor.getDouble(1);
            link.geometry = (LineString) cursor.getWKBGeometry(2);

        } catch (Exception e){
            return null;
        }

        link.restrictions = getRestrictionsOfInlink(linkpid, crossnodeid, oracle);

        LinkChain chain = new LinkChain(linkpid, link.geometry, dir, isStart);

        link.chains = chain.getLinkChain(getBranch, oracle);

        return link;
    }

    private static HashMap<Integer, Integer> getRestrictionsOfInlink(int inlinkid,
                                                                     int nodeid,
                                                                     OracleStatementContainer oracle){
        HashMap<Integer, Integer> restrictions = new HashMap<>();

        try(SqlCursor cursor = oracle.queryLinkRestrictions(inlinkid, nodeid)) {

            while (cursor.next()){
                int detailid = cursor.getInteger(1);
                int outlinkid = cursor.getInteger(2);
                int res_info = cursor.getInteger(3);
                int flag = cursor.getInteger(4);

                ArrayList<Integer> forbiddenVehicles = new ArrayList<>();
                try(SqlCursor vehicleCursor = oracle.queryRestricVehicle(detailid)){
                    while (vehicleCursor.next()){
                        forbiddenVehicles.add(vehicleCursor.getInteger(1));
                    }
                } catch (SQLException e){}

                if(!isForbidden4Car(forbiddenVehicles))
                    continue;

                restrictions.put(outlinkid, flag*10 + res_info);
            }
        } catch (SQLException e){

        }

        return restrictions;
    }

    private static boolean isForbidden4Car(List<Integer> vehicles){
        if(vehicles.size() == 0)
            return true;

        int motor_mask = 1 | (1 << 8) | (1 << 11) | (1 << 12) | (1 << 24);
        int flag_mask = 1 << 31;

        for(Integer vehicle : vehicles){
            int motor_type = vehicle & motor_mask;
            int flag = vehicle & flag_mask;

            if((vehicle & flag) != 0 ){     //允许通行
                if(motor_type > 0)
                    return true;
            } else if(motor_type > 0) {     //禁止通行
                return true;
            }
        }

        return false;
    }
}
