package com.navinfo.mapspotter.process.convert.road.export;

import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.util.SpatialUtil;
import com.vividsolutions.jts.geom.LineString;

import java.sql.SQLException;
import java.util.*;

/**
 * 路链
 * Created by SongHuiXing on 2016/4/6.
 */
public class LinkChain {
    private final double stop_find_angle = 90;
    private final int stop_find_count = 4;

    /**
     * ctor
     * @param start_link        起始linkid
     * @param link_geo          起始link几何
     * @param link_dir          link通行方向：1--双， 2--进入路口， 3--退出路口
     * @param beginFromStart    从link起点向终点方向查找??
     */
    public LinkChain(int start_link, LineString link_geo, int link_dir, boolean beginFromStart){
        startlink_id = start_link;
        startlink_dir = link_dir;
        startlink_geo = link_geo;
        findFromS2E = beginFromStart;
    }

    private final int startlink_id;

    private final int startlink_dir;

    private final boolean findFromS2E;

    private final LineString startlink_geo;

    private double totalTurnAngle = 0;

    public List<Integer> getLinkChain(boolean getBranch, OracleStatementContainer oracle){
        ArrayList<Integer> chain_ids = new ArrayList<>(stop_find_count);

        totalTurnAngle = 0;

        LinkAttr pre = new LinkAttr();
        pre.link_id = startlink_id;
        pre.link_dir = startlink_dir;
        pre.fromS2E = findFromS2E;
        pre.link_geo = startlink_geo;

        while (null != pre && chain_ids.size() < stop_find_count){
            if(chain_ids.contains(pre.link_id))
                break;

            chain_ids.add(pre.link_id);

            pre = get_next_link(pre, getBranch, oracle);
        }

        return chain_ids;
    }

    private LinkAttr get_next_link(LinkAttr startLink, boolean getBranch, OracleStatementContainer oracle){
        int next_node = get_next_nodeid(startLink.link_id, startLink.fromS2E, oracle);

        ArrayList<LinkAttr> slinks = getConnectedLinks(next_node, startLink.link_id, true, oracle);

        ArrayList<LinkAttr> elinks = getConnectedLinks(next_node, startLink.link_id, false, oracle);

        if((slinks.size() + elinks.size()) == 1){
            if(!slinks.isEmpty()){
                LinkAttr l = slinks.get(0);
                l.link_dir = startLink.link_dir;
                return l;
            } else {
                LinkAttr l = elinks.get(0);
                l.link_dir = startLink.link_dir;
                return l;
            }
        }

        if(!getBranch)
            return null;

        //过滤不符合通行方向的link
        ArrayList<LinkAttr> all = new ArrayList<>();
        switch (startLink.link_dir){
            case 1: {
                for(LinkAttr l : slinks){
                    if(l.link_dir != 1)
                        continue;

                    all.add(l);
                }
                for(LinkAttr l : elinks){
                    if(l.link_dir != 1)
                        continue;

                    all.add(l);
                }
            }
            break;
            case 2: {
                for(LinkAttr l : slinks){
                    if(l.link_dir != 3)
                        continue;
                    l.link_dir = 2;
                    all.add(l);
                }
                for(LinkAttr l : elinks){
                    if(l.link_dir != 2)
                        continue;
                    l.link_dir = 2;
                    all.add(l);
                }
            }
            break;
            case 3:{
                for(LinkAttr l : slinks){
                    if(l.link_dir != 2)
                        continue;
                    l.link_dir = 3;
                    all.add(l);
                }
                for(LinkAttr l : elinks){
                    if(l.link_dir != 3)
                        continue;
                    l.link_dir = 3;
                    all.add(l);
                }
            }
            break;
        }

        if(all.isEmpty())
            return null;

        SortedMap<Double, LinkAttr> anlgeSort = new TreeMap<>();
        for (LinkAttr l : all){
            anlgeSort.put(get_angle(l.link_geo, l.fromS2E, startLink.link_geo, startLink.fromS2E), l);
        }

        Double angle = anlgeSort.firstKey();
        if((totalTurnAngle + angle) > stop_find_angle){
            return null;
        }

        totalTurnAngle += angle;

        return anlgeSort.get(angle);
    }

    private static ArrayList<LinkAttr> getConnectedLinks(int nodeid, int exceptLink,
                                                  boolean isStart, OracleStatementContainer oracle){

        ArrayList<LinkAttr> links = new ArrayList<>();

        SqlCursor cursor;
        try {
            if(isStart){
                cursor = oracle.queryLinkFromSNode(nodeid, exceptLink);
            } else {
                cursor = oracle.queryLinkFromENode(nodeid, exceptLink);
            }
        } catch (SQLException e){
            return links;
        }

        if(null == cursor)
            return links;

        try{
            while (cursor.next()){
                LinkAttr l = new LinkAttr();
                l.fromS2E = isStart;
                l.link_id = cursor.getInteger(1);
                l.link_dir = cursor.getInteger(2);
                l.link_geo = (LineString)cursor.getWKBGeometry(3);

                links.add(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        } finally {
            try {
                cursor.close();
            } catch (SQLException e){}
        }

        return links;
    }

    private static int get_next_nodeid(int linkpid, boolean isS2E, OracleStatementContainer oracle){
        SqlCursor cursor = null;
        try {
            if (isS2E) {
                cursor = oracle.queryENode(linkpid);
            } else {
                cursor = oracle.querySNode(linkpid);
            }
        } catch (SQLException e){
            return -1;
        }

        if(null == cursor)
            return -1;

        try{
            if(!cursor.next())
                return -1;

            return cursor.getInteger(1);
        }catch (SQLException e){
            return -1;
        } finally {
            try {
                cursor.close();
            } catch (SQLException e){}
        }
    }

    private static double get_angle(LineString cmpLinkgeo, boolean cmpFromS2E,
                                    LineString baseLink, boolean baseFromS2E){
        LineString from = baseFromS2E ? baseLink : (LineString) baseLink.reverse();
        LineString to = cmpFromS2E ? cmpLinkgeo : (LineString) cmpLinkgeo.reverse();

        double angle = SpatialUtil.calculateCCTurnAngle(from, to);

        angle = angle / Math.PI * 180;

        return angle > 180 ? 360 - angle : angle;
    }
}

class LinkAttr{
    public int link_id;

    public int link_dir;

    public boolean fromS2E;

    public LineString link_geo;
}
