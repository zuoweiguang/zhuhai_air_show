package com.navinfo.mapspotter.foundation.model;

import com.navinfo.mapspotter.foundation.io.DataSource;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.SqlDatabase;
import com.navinfo.mapspotter.foundation.util.Logger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * POI Kind相关数据
 * Created by gaojian on 2016/1/26.
 */
public class PoiKind {

    private Map<String, String> kindMedium; // KindCode到中分类
    private Map<String, String> mediumTop; // 中分类到大分类

    // 名称Map
    private Map<String, String> kindName;
    private Map<String, String> mediumName;
    private Map<String, String> topName;

    public Map<String, String> getKindMap() {
        return kindName;
    }

    public Map<String, String> getMediumMap() {
        return mediumName;
    }

    public Map<String, String> getTopMap() {
        return topName;
    }

    /**
     * 初始化
     *
     * @param dataSource 保存POI Kind数据的数据库
     */
    public void init(DataSource dataSource) {
        SqlDatabase db = (SqlDatabase) dataSource;
        queryKind(db);
        queryKindMedium(db);
        queryKindTop(db);
    }

    /**
     * 读取配置文件初始化，需要配置文件中有对应的值
     *
     */
    public void init() {
        DataSource db = IOUtil.getDataSourceFromProperties("PoiKindDB");
        init(db);
        db.close();
    }

    /**
     * KindCode获取名称
     *
     * @param kind KindCode
     * @return 分类名称
     */
    public String getKindName(String kind) {
        if (kind == null)
            return null;

        return kindName.get(kind);
    }

    /**
     * KindCode获取中分类代码
     *
     * @param kind KindCode
     * @return 中分类代码
     */
    public String getMediumId(String kind) {
        if (kind == null)
            return null;

        return kindMedium.get(kind);
    }

    /**
     * 中分类代码获取名称
     *
     * @param medium 中分类代码
     * @return 中分类名
     */
    public String getMediumName(String medium) {
        if (medium == null)
            return null;

        return mediumName.get(medium);
    }

    /**
     * 中分类代码获取大分类代码
     *
     * @param medium 中分类代码
     * @return 大分类代码
     */
    public String getTopId(String medium) {
        if (medium == null)
            return null;

        return mediumTop.get(medium);
    }

    /**
     * 大分类代码获取名称
     *
     * @param top 大分类代码
     * @return 大分类名
     */
    public String getTopName(String top) {
        if (top == null)
            return null;

        return topName.get(top);
    }

    /**
     * 查询所有ci_para_kind集合
     *
     * @param db 数据库
     */
    private void queryKind(SqlDatabase db) {
        kindName = new HashMap<>();
        kindMedium = new HashMap<>();
        String sql = "SELECT medium_id,kind_code,name FROM ci_para_kind";
        SqlCursor cursor = db.query(sql);
        while (cursor.next()) {
            try {
                String medium_id = cursor.getString(1);
                String kind_code = cursor.getString(2);
                String name = cursor.getString(3);
                kindName.put(kind_code, name);
                kindMedium.put(kind_code, medium_id);
            } catch (SQLException e) {
                Logger.getLogger(PoiKind.class).error(e);
            }
        }
    }

    /**
     * 查询所有ci_para_kind_medium集合
     *
     * @param db
     */
    private void queryKindMedium(SqlDatabase db) {
        mediumName = new HashMap<>();
        mediumTop = new HashMap<>();
        String sql = "SELECT id,top_id,name FROM ci_para_kind_medium";
        SqlCursor cursor = db.query(sql);
        while (cursor.next()) {
            try {
                String id = cursor.getString(1);
                String topId = cursor.getString(2);
                String name = cursor.getString(3);
                mediumName.put(id, name);
                mediumTop.put(id, topId);
            } catch (SQLException e) {
                Logger.getLogger(PoiKind.class).error(e);
            }
        }
    }

    /**
     * 查询所有ci_para_kind_top集合
     *
     * @param db
     */
    private void queryKindTop(SqlDatabase db) {
        topName = new HashMap<>();
        String sql = "SELECT id,name FROM ci_para_kind_top";
        SqlCursor cursor = db.query(sql);
        while (cursor.next()) {
            try {
                String id = cursor.getString(1);
                String name = cursor.getString(2);
                topName.put(id, name);
            } catch (SQLException e) {
                Logger.getLogger(PoiKind.class).error(e);
            }
        }
    }
}
