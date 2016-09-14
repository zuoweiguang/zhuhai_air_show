package com;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;
import pool.DBPool;

import com.mercator.TileUtils;
import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class DataQuery {

	
private Connection conn;
	
	public DataQuery(){
		this.conn = DBPool.getInstance().getConnection();
	}
	
	
	public byte[] road(String wkt,int x,int y,int z){
		try{
			
			VectorTileEncoder e = new VectorTileEncoder(4096, 16, false);
			
			String sql = "select link_pid,geometry,name from link_beijing where sdo_relate(geometry,sdo_geometry(:1,8307),'MASK=ANYINTERACT') = 'TRUE'"; 
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, wkt);
			
			ResultSet resultSet = pstmt.executeQuery();
			
			resultSet.setFetchSize(300);
			
			while(resultSet.next()){
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				JGeometry geom = JGeometry.load(struct);

				String w = new String(new WKT().fromJGeometry(geom));

				WKTReader reader = new WKTReader();
				
				Geometry linkGeom = reader.read(w);
				
				
				Map<String,Object> attr = new HashMap<String,Object>();

//				attr.put("name_cn", resultSet.getString("name"));
				
				attr.put("name_cn", "#ff0000");
				
				attr.put("mycolor", "#ff0000");
				
				int pid = resultSet.getInt("link_pid");
				
				attr.put("pid", pid % 3);
				
				TileUtils.convert2Piexl(x, y, z, linkGeom);
				
				e.addFeature("road", attr, linkGeom);
				
				
			}
			
			resultSet.close();
			
			pstmt.close();
			return e.encode();
		}catch(Exception e){
			e.printStackTrace();
			
			return null;
		}finally{
			try{
				conn.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	

}
