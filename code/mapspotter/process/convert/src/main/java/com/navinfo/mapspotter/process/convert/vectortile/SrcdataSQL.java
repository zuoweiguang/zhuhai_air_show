package com.navinfo.mapspotter.process.convert.vectortile;

/**
 * Created by SongHuiXing on 6/7 0007.
 */
public class SrcdataSQL {
    public final static String road_main =
            "SELECT pid, kind, form, direct, functionclass, name, ST_AsBinary(geom) " +
                    "FROM (select pid, kind, direct, functionclass, geom from road " +
                            "where %s ST_Intersects(geom, ST_geomFromWKB(?))) tile_road " +
                    "LEFT JOIN (select link_pid, name from road_name where nameclass=1) form_name " +
                    "ON tile_road.pid = form_name.link_pid " +
                    "LEFT JOIN (select link_pid, form from road_form) r_form ON tile_road.pid=r_form.link_pid";

    public final static String railyway_main =
            "SELECT tile_line.lineid, kind, name, ST_AsBinary(geom) " +
                    "FROM (select lineid, fid, kind, geom from railway " +
                    "where ST_Intersects(geom, ST_geomFromWKB(?))) tile_line " +
                    "LEFT JOIN railway_name " +
                    "ON tile_line.lineid = railway_name.lineid";

    public final static String admin_main =
                    "select region.regionid, region.admin_type, ST_AsBinary(region.geom), adname.name from " +
                        "(select region_flagpoint.regionid regionid, admin_type, region_face.geom geom from region_flagpoint " +
                        " left join region_face on region_flagpoint.regionid=region_face.regionid " +
                        " where region_flagpoint.admin_type >= 4 and region_flagpoint.admin_type < 5) region " +
                    "left join (select regionid, name from region_name where nameclass=1 and langcode='CHI') adname " +
                    "on region.regionid = adname.regionid " +
                    "where ST_Intersects(region.geom, ST_geomFromWKB(?))";

    public final static String admin_boundary =
                    "select kind, ST_AsBinary(geom) from region_link " +
                    "where kind in (1,2,3,6) and ST_Intersects(geom, ST_geomFromWKB(?))";

    public final static String admin_flag =
            "select region.adminid, region.admin_type, ST_AsBinary(region.geom), adname.name from " +
            "(select regionid, adminid, admin_type, geom from region_flagpoint where admin_type < 7) region " +
            "left join (select regionid, name from region_name where nameclass=1 and langcode='CHI') adname " +
            "on region.regionid = adname.regionid " +
            "where ST_Intersects(region.geom, ST_geomFromWKB(?))";

    public final static String lc_main =
                    "select landcover.id, landcover.kind, chiname.name, ST_AsBinary(landcover.geom) from landcover " +
                    "left join (select id, name from landcover_name where langcode='CHI') chiname " +
                    "on landcover.id = chiname.id " +
                    "where %s ST_Intersects(landcover.geom, ST_geomFromWKB(?))";

    public final static String lu_main =
                    "SELECT LU.ID, LU.KIND, N.NAME, ST_AsBinary(LU.GEOM) " +
                      "FROM LANDUSE LU, LANDUSE_NAME N " +
                     "WHERE LU.ID = N.ID AND LU.KIND NOT IN (0,7,21,22,23,24,30,35,36,39,40) " +
                       "AND N.LANGCODE = 'CHI' " +
                       "AND %s ST_INTERSECTS(LU.GEOM, ST_GEOMFROMWKB(?))";

    public final static String city_model =
                    "SELECT FACE.FACEID,FACE.HEIGHT,MODEL.KIND,NAME.BASENAME,ST_AsBinary(FACE.GEOM) " +
                      "FROM CITY_MODEL_FACE FACE,CITY_MODEL MODEL,CITY_MODEL_NAME NAME " +
                     "WHERE FACE.CM_ID = MODEL.CM_ID AND MODEL.CM_ID = NAME.CM_ID AND NAME.LANGCODE='CHI' " +
                       "AND %s ST_INTERSECTS(FACE.GEOM, ST_GEOMFROMWKB(?))";

    public final static String block_history =
            "select name, province, city, version, ST_AsBinary(geom) from " +
            "block_history where ST_Intersects(geom, ST_geomFromWKB(?))";

    public final static String traffic =
            "select direct, status, ST_AsBinary(geom) from traffic_link " +
            "where timestamp>=? and timestamp<=? and %s ST_Intersects(geom, ST_geomFromWKB(?))";
}
