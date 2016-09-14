package com.navinfo.mapspotter.process.convert.road.export;

import com.navinfo.mapspotter.foundation.io.OracleDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 为了加快速度提供的preparedstmt集合
 * Created by SongHuiXing on 2016/4/9.
 */
public class OracleStatementContainer implements AutoCloseable {
    private static final String query_samecross = "select distinct(pid) from rd_cross_node " +
                                                    "where node_pid in (?, ?)";

    private static final String query_linknodes = "select S_NODE_PID, E_NODE_PID from RD_LINK " +
                                                "where LINK_PID=? or LINK_PID=?";

    private static final String query_crossnode = "select NODE_PID from RD_CROSS_NODE where PID=?";

    private static final String query_nodegeo = "select GEOMETRY from RD_NODE where NODE_PID=%d";

    private static final String query_sconnlink = "select t1.LINK_PID, t1.DIRECT from RD_LINK t1 " +
                                                    "where t1.S_NODE_PID = ? and " +
                                                    "not exists(select 1 from RD_LINK_FORM t2 " +
                                                    "where t1.LINK_PID=t2.LINK_PID and t2.FORM_OF_WAY=50)";

    private static final String query_econnlink = "select t1.LINK_PID, t1.DIRECT from RD_LINK t1 " +
                                                "where t1.E_NODE_PID = ? and " +
                                                    "(select count(1) from RD_LINK_FORM t2 " +
                                                    "where t1.LINK_PID=t2.LINK_PID and t2.FORM_OF_WAY=50)=0";

    private static final String query_linkgeo = "select LENGTH, GEOMETRY from RD_LINK " +
                                                "where LINK_PID = %d";

    private static final String query_restric =
                    "select t2.DETAIL_ID, t2.OUT_LINK_PID, t2.RESTRIC_INFO, t2.FLAG from" +
                    " (select PID from RD_RESTRICTION where IN_LINK_PID=? and NODE_PID=?) t1 " +
                    "  left join" +
                    " (select DETAIL_ID, RESTRIC_PID, OUT_LINK_PID, RESTRIC_INFO, FLAG " +
                    "         from RD_RESTRICTION_DETAIL where RELATIONSHIP_TYPE = 1) t2 " +
                    "  on t1.PID = t2.RESTRIC_PID";

    private static final String query_vehicle = "select VEHICLE from RD_RESTRICTION_CONDITION " +
                                                "where RES_OUT=0 and DETAIL_ID=?";

    private static final String query_linksnode = "select S_NODE_PID from RD_LINK where LINK_PID=?";
    private static final String query_linkenode = "select E_NODE_PID from RD_LINK where LINK_PID=?";

    private static final String query_link4s = "select LINK_PID, Direct, Geometry from RD_LINK " +
                                                 "where S_NODE_PID =? and LINK_PID <> ?";

    private static final String query_link4e = "select LINK_PID, Direct, Geometry from RD_LINK " +
                                                "where E_NODE_PID =? and LINK_PID <> ?";

    private final OracleDatabase oraDb;

    private PreparedStatement stmt_samecross = null;
    private PreparedStatement stmt_linknodes = null;
    private PreparedStatement stmt_crossnode = null;
    //private PreparedStatement stmt_nodegeo = null;
    private PreparedStatement stmt_sconnlink = null;
    private PreparedStatement stmt_econnlink = null;
    //private PreparedStatement stmt_linkgeo = null;
    private PreparedStatement stmt_restric = null;
    private PreparedStatement stmt_vehicle = null;
    private PreparedStatement stmt_linksnode = null;
    private PreparedStatement stmt_linkenode = null;
    private PreparedStatement stmt_link4s = null;
    private PreparedStatement stmt_link4e = null;

    public OracleStatementContainer(OracleDatabase oracle){
        oraDb = oracle;

        prepareStmt();
    }

    private void prepareStmt(){
        try{
            stmt_samecross = oraDb.prepare(query_samecross);
            stmt_linknodes = oraDb.prepare(query_linknodes);
            stmt_crossnode = oraDb.prepare(query_crossnode);
            //stmt_nodegeo = oraDb.prepare(query_nodegeo);
            stmt_sconnlink = oraDb.prepare(query_sconnlink);
            stmt_econnlink = oraDb.prepare(query_econnlink);
            //stmt_linkgeo = oraDb.prepare(query_linkgeo);
            stmt_restric = oraDb.prepare(query_restric);
            stmt_vehicle = oraDb.prepare(query_vehicle);
            stmt_linksnode = oraDb.prepare(query_linksnode);
            stmt_linkenode = oraDb.prepare(query_linkenode);
            stmt_link4s = oraDb.prepare(query_link4s);
            stmt_link4e = oraDb.prepare(query_link4e);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public SqlCursor querySameCross(Integer node1, Integer node2) throws SQLException {
        return oraDb.query(stmt_samecross, node1, node2);
    }

    public SqlCursor queryLinkNodes(Integer link1, Integer link2) throws SQLException {
        return oraDb.query(stmt_linknodes, link1, link2);
    }

    public SqlCursor queryCrossNodes(Integer crossid) throws SQLException {
        return oraDb.query(stmt_crossnode, crossid);
    }

    public SqlCursor querySNodeConnLinks(Integer nodeid) throws SQLException {
        return oraDb.query(stmt_sconnlink, nodeid);
    }

    public SqlCursor queryENodeConnLinks(Integer nodeid) throws SQLException {
        return oraDb.query(stmt_econnlink, nodeid);
    }

    public SqlCursor queryNodeGeo(Integer nodeid) throws SQLException {
        //return oraDb.query(stmt_nodegeo, nodeid);
        return oraDb.query(String.format(query_nodegeo, nodeid));
    }

    public SqlCursor queryLinkGeo(Integer linkid) throws SQLException {
        //return oraDb.query(stmt_linkgeo, linkid);
        return oraDb.query(String.format(query_linkgeo, linkid));
    }

    public SqlCursor queryLinkRestrictions(Integer inlinkid, Integer nodeid) throws SQLException {
        return oraDb.query(stmt_restric, inlinkid, nodeid);
    }

    public SqlCursor queryRestricVehicle(Integer resDetailId) throws SQLException {
        return oraDb.query(stmt_vehicle, resDetailId);
    }

    public SqlCursor queryLinkFromSNode(Integer nodeid, Integer exceptlinkid) throws SQLException {
        return oraDb.query(query_link4s, nodeid, exceptlinkid);
    }

    public SqlCursor queryLinkFromENode(Integer nodeid, Integer exceptlinkid) throws SQLException {
        return oraDb.query(query_link4e, nodeid, exceptlinkid);
    }

    public SqlCursor querySNode(Integer linkid) throws SQLException {
        return oraDb.query(query_linksnode, linkid);
    }

    public SqlCursor queryENode(Integer linkid) throws SQLException {
        return oraDb.query(query_linkenode, linkid);
    }

    @Override
    public void close() throws Exception {
        stmt_samecross.close();
        stmt_linknodes.close();
        stmt_crossnode.close();
        //stmt_nodegeo.close();
        stmt_sconnlink.close();
        stmt_econnlink.close();
        //stmt_linkgeo.close();
        stmt_restric.close();
        stmt_vehicle.close();
        stmt_linksnode.close();
        stmt_linkenode.close();
        stmt_link4s.close();
        stmt_link4e.close();
    }
}
