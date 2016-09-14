package com.navinfo.mapspotter.process.convert.ora2pg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.PatternFilenameFilter;
import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.PostGISDatabase;
import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import com.vividsolutions.jts.geom.Polygon;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 将历史台帐导入PostGIS
 * Created by SongHuiXing on 7/21 0021.
 */
public class Block2PostGIS {

    private final DataSourceParams pgParams;

    private final String insert_block = "INSERT INTO " +
                "block_history(block_id, version, name, province, city, county, geom) " +
                "VALUES(?,?,?,?,?,?,st_geomfromewkb(?))";

    public Block2PostGIS(String pgHost, int pgPort, String pgDb,
                         String pgUser, String pgPwd){
        pgParams = IOUtil.makePostGISParam(pgHost, pgPort, pgDb, pgUser, pgPwd);
    }

    private PostGISDatabase db = null;

    private PreparedStatement insert_stmt = null;

    public boolean open(){
        if(null != db)
            return true;

        db = (PostGISDatabase) DataSource.getDataSource(pgParams);

        try {
            insert_stmt = db.prepare(insert_block);
        } catch (SQLException e) {
            e.printStackTrace();
            db.close();
            db = null;
        }

        return null != db;
    }

    public void close(){
        if(null != db) {
            db.close();

            db = null;
        }

        if(null != insert_stmt){
            try {
                insert_stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            insert_stmt = null;
        }
    }

    public int importFromGeojson(File path){

        if(null == db)
            return 0;

        int totalCount = 0;

        if(path.isDirectory()){
            File[] jsonFiles = path.listFiles();

            for (File geojsonFile : jsonFiles){
                totalCount += importFromGeojson(geojsonFile);
            }
        } else if(path.getName().endsWith("geojson")) {
            totalCount = importGeojsonFile(path);
        }

        return totalCount;
    }

    private int importGeojsonFile(File geojsonFile){

        StringBuilder contentBuilder = new StringBuilder();

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(geojsonFile);

            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = null;
            while (null != (line = reader.readLine())){
                contentBuilder.append(line);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != fileInputStream){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String filename = geojsonFile.getName();
        filename = filename.substring(0, filename.lastIndexOf('.'));

        int totalCount = 0;
        try {
            String json = contentBuilder.toString();
            Map<String, Object> ftColl =
                    JsonUtil.getInstance().readMap(json);

            List<Object> fts = (List<Object>) ftColl.get("features");

            for (Object ft : fts){
                if(insertFeature(ft, filename)){
                    totalCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalCount;
    }

    private boolean insertFeature(Object ft, String version){
        Map<String, Object> ftMap = (Map<String, Object>)ft;

        if(null == ftMap)
            return false;

        try {
            Map<String, Object> props = (Map<String, Object>)ftMap.get("properties");

            String geojsonStr = JsonUtil.getInstance().write2String(ftMap.get("geometry"));

            com.vividsolutions.jts.geom.Geometry geo = GeoUtil.readGeojson(geojsonStr);

            Polygon polygon = (Polygon)geo;

            if(null == polygon)
                return false;

            String name = (String) props.get("name");
            if(null == name)
                name = (String) props.get("Name");

            String county = (String) props.get("county");
            if(null == name)
                county = (String) props.get("County");

            db.excute(insert_stmt, StringUtil.lessUUID(), version,
                    name,
                    props.get("province"),
                    props.get("city"),
                    county,
                    GeoUtil.geometry2WKB(polygon));

        } catch (IllegalArgumentException e){
            try {
                System.err.print(JsonUtil.getInstance().write2String(ft));
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
