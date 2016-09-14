package com.navinfo.mapspotter.warehouse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.vector.tile.VectorTileEncoder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SongHuiXing on 5/14 0014.
 */
@Path("/json")
    public class JsonResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGeojson(){
        return "{\"type\":\"Point\"; \"geometry\":\"[119.1232, 39.23423]\"}";
    }

    @GET @Path("/point/{x}/{y}")
    @Produces("text/plain;charset=UTF-8")
    public String getPoint(@PathParam("x") String x,
                           @PathParam("y") String y){
        return String.format("{\"type\":\"Point\"; \"geometry\":\"[%s, %s]\"}", x, y);
    }

    @GET @Path("/line")
    @Produces("text/plain;charset=UTF-8")
    public String getLine(@QueryParam("from") String f,
                          @QueryParam("to") String t){
        return String.format("{\"type\":\"LineString\"; \"geometry\":\"[%s, %s]\"}", f, t);
    }
    
    @GET @Path("/pbf/{z}/{x}/{y}")
    @Produces("application/x-protobuf")
    public byte[] getPbf(@PathParam("x") String x,
                         @PathParam("y") String y,
                         @PathParam("z") String z){
        GeometryFactory gf = new GeometryFactory();

        VectorTileEncoder vtm = new VectorTileEncoder(256);
        Geometry geometry = gf.createPoint(new Coordinate(3, 6));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "北京站");
        attributes.put("key2", Integer.valueOf(123));
        attributes.put("key3", Float.valueOf(234.1f));
        attributes.put("key4", Double.valueOf(567.123d));
        attributes.put("key5", Long.valueOf(-123));
        attributes.put("key6", "value6");

        vtm.addFeature("POI", attributes, geometry);

        byte[] encoded = vtm.encode();
        return encoded;
    }
}
