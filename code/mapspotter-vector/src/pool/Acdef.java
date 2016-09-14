package pool;

import java.util.ArrayList;
import java.util.List;

import com.mercator.MercatorProjection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class Acdef {
	
	private static GeometryFactory gc = new GeometryFactory();

	
	public static void main(String[] args) {

//		double tilex = MercatorProjection.longitudeToTileX(116.3972282409668, (byte)12);
//	
//		double tiley = MercatorProjection.latitudeToTileY(39.90960456049752, (byte)12);
//	
//		System.out.println(tilex+" "+tiley);
		
//		System.out.println(getTiles(3371,1552,12,13));
		
		
//		System.out.println(MercatorProjection.tileXToLongitude(3371, (byte) 12));
//		
//		System.out.println(MercatorProjection.tileYToLatitude(1552, (byte) 12));
		
		for(int i=0;i<10000;i++){
		
		 getGeoms(3371,1552,12,16);
		}
	}
	
	public static List<Geometry> getGeoms(int x,int y,int s,int e){
		
		List<Geometry> geoms = new ArrayList<Geometry>();
		
		List<Integer[]> list = getTiles(x,y,s,e);
		
		byte zoom = (byte)e;
		
		for(Integer[] xy:list){
			int tx = xy[0];
			
			int ty = xy[1];
			
			Coordinate[] cs = new Coordinate[5];
			
			cs[0] = new Coordinate(MercatorProjection.tileXToLongitude(tx, zoom),MercatorProjection.tileYToLatitude(ty, zoom));
			
			cs[1] = new Coordinate(MercatorProjection.tileXToLongitude(tx + 1, zoom),MercatorProjection.tileYToLatitude(ty, zoom));
			
			cs[2] = new Coordinate(MercatorProjection.tileXToLongitude(tx + 1, zoom),MercatorProjection.tileYToLatitude(ty + 1, zoom));
			
			cs[3] = new Coordinate(MercatorProjection.tileXToLongitude(tx, zoom),MercatorProjection.tileYToLatitude(ty + 1, zoom));
			
			cs[4] = new Coordinate(MercatorProjection.tileXToLongitude(tx, zoom),MercatorProjection.tileYToLatitude(ty, zoom));
			
			Polygon p = gc.createPolygon(cs);
			
			geoms.add(p);
		}
		
		return geoms;
		
	}
	
	public static List<Integer[]> getTiles(int x,int y,int s,int e){
		
		List<Integer[]> list = new ArrayList<Integer[]>();
		
		List<Integer> listx = new ArrayList<Integer>();
		
		listx.add(x);
		
		List<Integer> listy = new ArrayList<Integer>();
		
		listy.add(y);
		
		for(int i=0;i<(e-s);i++){
			listx = getNextLevelXY(listx);
			listy = getNextLevelXY(listy);
		}
		
		for(int i=0;i<listx.size();i++){
			for(int j=0;j<listy.size();j++){
				list.add(new Integer[]{listx.get(i),listy.get(j)});
			}
		}
		
		return list;
		
	}
	
	public static List<Integer> getNextLevelXY(List<Integer> listSour){
		List<Integer> listDes = new ArrayList<Integer>();
		
		for(int v: listSour){
			listDes.add(v * 2);
			
			listDes.add(v * 2 + 1);
		}
		
		return listDes;
	}

}
