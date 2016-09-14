package com.navinfo.mapspotter.process.convert.tab;

import com.navinfo.mapspotter.foundation.io.*;
import com.navinfo.mapspotter.foundation.io.DataSource;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SongHuiXing on 7/20 0020.
 */
public class TabReader {
    public int convertTab2Pg(String tabFile,
                             String pgHost, int pgPort, String pgDb,
                             String pgUser, String pgPwd){

        PostGISDatabase db = (PostGISDatabase) DataSource.getDataSource(
                                            IOUtil.makePostGISParam(pgHost,
                                                    pgPort,
                                                    pgDb,
                                                    pgUser,
                                                    pgPwd));

        if(null == db)
            return -1;

        int totalCount = 0;

        try {
            ogr.RegisterAll();

            // 支持中文路径
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");

            // 使属性表字段支持中文
            gdal.SetConfigOption("SHAPE_ENCODING", "");

            //打开数据
            org.gdal.ogr.DataSource ds = ogr.Open(tabFile, 0);
            if (ds == null) {
                System.out.println("打开文件【" + tabFile + "】失败！");
                return -1;
            }

            System.out.println("打开文件【" + tabFile + "】成功！");

            int iLayerCount = ds.GetLayerCount();
            for (int i = 0; i < iLayerCount; i++) {
                Layer oLayer = ds.GetLayerByIndex(i);
                if (oLayer == null) {
                    continue;
                }

                totalCount += exportFeature2Pg(oLayer, db);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.close();
        }

        return totalCount;
    }

    private int exportFeature2Pg(Layer layer, PostGISDatabase db){
        layer.ResetReading();

        FeatureDefn oDefn = layer.GetLayerDefn();
        int iFieldCount = oDefn.GetFieldCount();

        int exportCount = 0;

        // 下面开始遍历图层中的要素
        Feature oFeature = null;
        while ((oFeature = layer.GetNextFeature()) != null) {

            for (int iField = 0; iField < iFieldCount; iField++) {
                FieldDefn oFieldDefn = oDefn.GetFieldDefn(iField);
                int type = oFieldDefn.GetFieldType();
                switch (type) {
                    case ogr.OFTString:
                        System.out.println(oFeature.GetFieldAsString(iField) + "\t");
                        break;
                    case ogr.OFTReal:
                        System.out.println(oFeature.GetFieldAsDouble(iField) + "\t");
                        break;
                    case ogr.OFTInteger:
                        System.out.println(oFeature.GetFieldAsInteger(iField) + "\t");
                        break;
                    default:
                        System.out.println(oFeature.GetFieldAsString(iField) + "\t");
                        break;
                }
            }

            // 获取要素中的几何体
            List<byte[]> geometryWkbs = convertGeo(oFeature.GetGeometryRef());

        }

        return exportCount;
    }

    private static List<byte[]> convertGeo(Geometry geo){
        ArrayList<byte[]> geowkbs = new ArrayList<>();

        int geoType = geo.GetGeometryType();
        if(geoType != ogr.wkbGeometryCollection){
            geowkbs.add(geo.ExportToWkb());
        }else {
            int childcount = geo.GetGeometryCount();
            for (int i = 0; i < childcount; i++) {
                geowkbs.addAll(convertGeo(geo.GetGeometryRef(i)));
            }
        }

        return geowkbs;
    }
}
