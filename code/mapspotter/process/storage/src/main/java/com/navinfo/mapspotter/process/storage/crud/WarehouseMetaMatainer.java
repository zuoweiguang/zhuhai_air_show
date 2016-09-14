package com.navinfo.mapspotter.process.storage.crud;

import com.google.common.io.PatternFilenameFilter;
import com.navinfo.mapspotter.foundation.io.MongoDB;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.process.convert.WarehouseDataType;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据仓库的元数据维护器
 * Created by SongHuiXing on 7/8 0008.
 */
public class WarehouseMetaMatainer {
    protected final MongoDB db;
    private final String style_collection = "defaultstyles";

    public WarehouseMetaMatainer(MongoDB database){
        db = database;
    }

    /**
     * 更新数据仓库中数据源的源数据信息
     * @param sourceType    数据源的类型
     * @param minzoom       最小缩放级别
     * @param maxzoom       最大缩放级别
     * @param sourceDate    数据源的更新时间
     * @return
     */
    public boolean updateSourceMetadata(WarehouseDataType.SourceType sourceType,
                                        int minzoom,
                                        int maxzoom,
                                        Date sourceDate){

        return true;
    }

    /**
     * 初始化数据仓库中的默认样式，包括：
     * ①对7类渲染方式的默认样式支持，不对应任何已有数据源
     * ②数据仓库中数据源的默认样式
     * @param metadataConfigPath    数据仓库元数据配置文件路径
     * @return
     */
    public boolean initDefaultStyle(String metadataConfigPath){
        File metaFilePath = new File(metadataConfigPath);

        if(metaFilePath.isDirectory()){
            PatternFilenameFilter filter = new PatternFilenameFilter("^*.json$");

            File[] styleFiles = metaFilePath.listFiles();

            for (File styleFile : styleFiles){
                writeNewStyles(styleFile);
            }
        } else {
            writeNewStyles(metaFilePath);
        }

        return true;
    }

    private boolean writeNewStyles(File styleFile){

        StringBuilder filecontentBuilder = new StringBuilder();

        FileInputStream fileInputStream = null;
        BufferedReader reader = null;
        try {
            fileInputStream = new FileInputStream(styleFile);

            reader = new BufferedReader(new InputStreamReader(fileInputStream));

            String line = null;
            while (null != (line = reader.readLine())){
                filecontentBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }

                if (null != fileInputStream) {
                    fileInputStream.close();
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        if(filecontentBuilder.length() == 0){
            return false;
        }

        try {

            Object jsonObj = JsonUtil.getInstance().readValue(filecontentBuilder.toString(),
                                                            Object.class);

            List<Map<String, Object>> styles = (List<Map<String, Object>>)jsonObj;

            for (Map<String, Object> style : styles){
                db.insert(style_collection, style);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
