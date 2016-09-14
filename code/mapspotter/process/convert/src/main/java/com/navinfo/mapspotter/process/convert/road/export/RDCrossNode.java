package com.navinfo.mapspotter.process.convert.road.export;

import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.vividsolutions.jts.geom.Point;
import org.geojson.Feature;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SongHuiXing on 2016/4/5.
 */
public class RDCrossNode {

    private RDCrossNode(int pid){
        node_pid = pid;
    }

    private final int node_pid;
    public int getPID(){
        return node_pid;
    }

    private Point geometry = null;
    public Point getGeometry(){
        return geometry;
    }

    //lid：路口外Link的PID
    //direct：Link相对于路口的通行方向 1 双方向 2 进入 3 退出
    private HashMap<Integer, Integer> s_connect_links = null;
    public HashMap<Integer, Integer> getStartConnectLinks(){
        return s_connect_links;
    }

    private HashMap<Integer, Integer> e_connect_links = null;
    public HashMap<Integer, Integer> getEndConnectLinks(){
        return e_connect_links;
    }

    public Feature toJsonFeature(){
        Feature ft = new Feature();
        ft.setGeometry(new org.geojson.Point(geometry.getX(), geometry.getY()));
        ft.setProperty("PID", node_pid);

        ArrayList<Integer> linkids = new ArrayList<>(s_connect_links.keySet());
        linkids.addAll(e_connect_links.keySet());
        ft.setProperty("ConnectLinks", linkids);

        return ft;
    }

    public static RDCrossNode getNode(int pid, OracleStatementContainer oracle){
        RDCrossNode node;

        try(SqlCursor cursor = oracle.queryNodeGeo(pid)){
            if(!cursor.next())
                return null;

            node = new RDCrossNode(pid);
            node.geometry = (Point) cursor.getWKBGeometry(1);

        } catch (Exception e){
            return null;
        }

        //lid：路口外Link的PID
        //direct：Link相对于路口的通行方向 1 双方向 2 进入 3 退出
        HashMap<Integer, Integer> s_links = new HashMap<>();

        //作为起点挂接的link
        try(SqlCursor cursor = oracle.querySNodeConnLinks(pid)){
            while (cursor.next()){
                int relativeDir = cursor.getInteger(2);
                if(relativeDir == 2){
                    relativeDir = 3;
                } else if(relativeDir == 3){
                    relativeDir = 2;
                }

                s_links.put(cursor.getInteger(1), relativeDir);
            }
        } catch (SQLException e){

        }

        node.s_connect_links = s_links;

        //作为终点挂接的link
        HashMap<Integer, Integer> e_links = new HashMap<>();
        try(SqlCursor cursor = oracle.queryENodeConnLinks(pid)){
            while (cursor.next()){
                e_links.put(cursor.getInteger(1), cursor.getInteger(2));
            }
        } catch (SQLException e){}

        node.e_connect_links = e_links;

        return node;
    }
}