package com.navinfo.mapspotter.process.convert.road.export;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class ExportLink {
/**
 create table tmp2 as
 select
 -- parallel(a 10)
 a.link_pid,a.function_class,
 a.direct,a.kind,
 a.lane_num,a.lane_left,a.lane_right,a.length,a.mesh_id,
 b.link_limit,
 c.speed_limit,
 a.geometry
 from
 rd_link a,
 (select link_pid,listagg(type||','||limit_dir,'-') within group(order by 1) link_limit from rd_link_limit group by link_pid) b,
 (select link_pid,listagg(speed_type||','||from_speed_limit||','||to_speed_limit||','||speed_dependent,'-') within group(order by 1) speed_limit from rd_link_speedlimit group by link_pid) c
 where a.link_pid = b.link_pid(+)
 and a.link_pid = c.link_pid(+)
 ;
*/
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        String username = args[0];
        String password = args[1];
        String ip = args[2];
        int port = Integer.parseInt(args[3]);
        String serviceName = args[4];
        String outPath = args[5];
        String tableName = args[6];
        PrintWriter out = new PrintWriter(outPath);
        String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + serviceName;
        Connection conn = DriverManager.getConnection(url, username, password);
        String sql = "select link_pid,function_class,direct," +
                "kind,lane_num,lane_left,lane_right,length," +
                "mesh_id," +
                "link_limit,speed_limit," +
                "geometry" +
                " from " + tableName;
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql);
        resultSet.setFetchSize(5000);
        int num = 0;
        while (resultSet.next()) {
            organize(resultSet, out);
            num++;
            if (num % 10000 == 0) {
                System.out.println(num);
            }
        }
        out.flush();
        out.close();
    }

    //link_pid,function_class,direct,kind,lane_num,lane_left,lane_right,length,mesh_id,link_limit,speed_limit,geometry
    public static void organize(ResultSet rs, PrintWriter out) throws Exception {
        int link_pid = rs.getInt("link_pid");
        int functionClass = rs.getInt("function_class");
        int direct = rs.getInt("direct");
        int kind = rs.getInt("kind");
        int lane_num = rs.getInt("lane_num");
        int lane_left = rs.getInt("lane_left");
        int lane_right = rs.getInt("lane_right");
        double length = rs.getDouble("length");
        int mesh_id = rs.getInt("mesh_id");
        String link_limit = rs.getString("link_limit");
        String link_speed_limit = rs.getString("speed_limit");
        STRUCT struct = (STRUCT) rs.getObject("geometry");
        JGeometry geom = JGeometry.load(struct);
        StringBuilder sb = new StringBuilder("{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");
        double[] points = geom.getOrdinatesArray();
        for (int i = 0; i < points.length - 3; i++) {
            double lng = points[i];
            double lat = points[++i];
            sb.append("[" + lng + "," + lat + "],");
        }
        double lng = points[points.length - 2];
        double lat = points[points.length - 1];
        sb.append("[" + lng + "," + lat + "]]},\"type\":\"Feature\",\"properties\":{");
        sb.append("\"link_pid\":" + link_pid);
        sb.append(",\"functionclass\":" + functionClass);
        sb.append(",\"direct\":" + direct);
        sb.append(",\"kind\":" + kind);
        sb.append(",\"lane_num\":" + lane_num);
        sb.append(",\"lane_left\":" + lane_left);
        sb.append(",\"lane_right\":" + lane_right);
        sb.append(",\"length\":" + length);
        sb.append(",\"mesh_id\":" + mesh_id);
        sb.append(",\"link_limit\":[");
        if (link_limit != null && link_limit.length() > 0) {
            String[] splits = link_limit.split("\\-");
            for (int i = 0; i < splits.length; i++) {
                String split = splits[i];

                String[] sps = split.split(",");
                if (i == 0) {
                    sb.append("{\"type\":" + sps[0] + ",\"limit_dir\":" + sps[1] + "}");
                } else {
                    sb.append(",{\"type\":" + sps[0] + ",\"limit_dir\":" + sps[1] + "}");
                }
            }
        }
        sb.append("],\"link_speed_limit\":[");
        if (link_speed_limit != null && link_speed_limit.length() > 0) {
            String[] splits = link_speed_limit.split("\\-");
            for (int i = 0; i < splits.length; i++) {
                String split = splits[i];

                String[] sps = split.split(",");
                if (i == 0) {
                    sb.append("{\"speed_type\":" + sps[0] + ",\"from_speed_limit\":" + sps[1] + ",\"to_speed_limit\":" + sps[2] + ",\"speed_dependent\":" + sps[3] + "}");
                } else {
                    sb.append(",{\"speed_type\":" + sps[0] + ",\"from_speed_limit\":" + sps[1] + ",\"to_speed_limit\":" + sps[2] + ",\"speed_dependent\":" + sps[3] + "}");
                }
            }
        }
        sb.append("]}}");
        out.println(sb.toString());
    }
}
