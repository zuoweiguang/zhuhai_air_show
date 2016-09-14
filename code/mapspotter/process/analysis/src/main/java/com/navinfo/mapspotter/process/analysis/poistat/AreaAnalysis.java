package com.navinfo.mapspotter.process.analysis.poistat;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.OracleDatabase;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.model.AdminBlock;
import com.navinfo.mapspotter.foundation.util.GeoUtil;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.foundation.util.PropertiesUtil;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

/**
 * 行政区划、全国台账等区域相关分析类
 *
 * Created by gaojian on 2016/2/2.
 */
public class AreaAnalysis {
    private static final Logger logger = Logger.getLogger(AreaAnalysis.class);

    private OracleDatabase oracleDB = null;
    private String proc = "";
    private Map<String, AdminBlock> areaMap = null;

    public int initialize() {
        try {
            oracleDB = (OracleDatabase) DataSource.getDataSource(
                    IOUtil.makeOracleParams(
                            PropertiesUtil.getValue("AreaCount.host"),
                            Integer.parseInt(PropertiesUtil.getValue("AreaCount.port")),
                            PropertiesUtil.getValue("AreaCount.db"),
                            PropertiesUtil.getValue("AreaCount.user"),
                            PropertiesUtil.getValue("AreaCount.password")
                    ));
            proc = PropertiesUtil.getValue("AreaCount.proc");
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }

        if (oracleDB == null || !StringUtil.isEmpty(proc)) {
            return -1;
        }

        return 0;
    }

    public int prepareAreaMap() {
        areaMap = new HashMap<>();

        String sql = PropertiesUtil.getValue("AreaCount.sql");
        logger.info(sql);

        try {
            SqlCursor cursor = oracleDB.query(sql);
            while (cursor.next()) {
                String code = cursor.getString(1);
                String town = cursor.getString(2);
                String city = cursor.getString(3);
                String prov = cursor.getString(4);
                AdminBlock area = new AdminBlock(code, town, city, prov);
                areaMap.put(code, area);
            }
            cursor.close();
        } catch (Exception e) {
            logger.error(e);
            return -1;
        }

        return 0;
    }

    public AdminBlock getAreaInfo(String areaId) {
        return areaMap.get(areaId);
    }

    public String locateArea(Geometry geometry) {
        String wkt = geometry.toText();
        String areaId = oracleDB.callProcedure(proc, wkt);
        return areaId;
    }

    public void destroy() {
        oracleDB.close();
    }

    public static void main(String[] args) {
        AreaAnalysis areaAnalysis = new AreaAnalysis();
        areaAnalysis.initialize();

        String areaid = areaAnalysis.locateArea(GeoUtil.createPoint(116.36115, 39.931176));
        System.out.println(areaid);
        areaAnalysis.destroy();
    }
}
