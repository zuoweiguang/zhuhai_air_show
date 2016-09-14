package com.navinfo.mapspotter.foundation.util;

import com.navinfo.mapspotter.foundation.io.Hdfs;
import com.navinfo.mapspotter.foundation.io.IOUtil;
import com.navinfo.mapspotter.foundation.io.SqlCursor;
import com.navinfo.mapspotter.foundation.io.SqlDatabase;
import com.vividsolutions.jts.geom.Envelope;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.util.*;

/**
 * 处理省和图幅、瓦片之间的对应关系
 *
 * Created by gaojian on 2016/1/27.
 */
public class ProvinceUtil {
    private Map< String, List<String> > provinceMeshes = null;

    /**
     * 初始化省和图幅的关系，从数据库中读取分省图幅表
     */
    public void initProvinceMeshes() {
        provinceMeshes = new HashMap<>(31);

        try {
            SqlDatabase db = (SqlDatabase) IOUtil.getDataSourceFromProperties("ProvinceDB");
            //String sql = "SELECT MESH_STR,ADMIN_NAME FROM NI_MESHLIST_FOR_DN WHERE SCALE=2.5 AND ADMIN_ID<800000";
            //String sql = "select t.mesh , t.province from META_16SUM.Cp_Meshlist t where t.scale = '2.5' and t.admincode < 810000";
            String sql = "select mesh_id , city from shd_xiningshi_mesh";
            SqlCursor cursor = db.query(sql);
            while (cursor.next()) {
                String mesh = cursor.getString(1);
                String province = cursor.getString(2);
                List<String> meshes = provinceMeshes.get(province);
                if (meshes != null) {
                    meshes.add(mesh);
                } else {
                    meshes = new ArrayList<>();
                    meshes.add(mesh);
                    provinceMeshes.put(province, meshes);
                }
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Logger.getLogger(ProvinceUtil.class).error(e);
        }
    }
    /**
     * 初始化省和图幅的关系，从文件中读取分省图幅表
     */
    public void initProvinceMeshes(String fileName , int systemType){
        if (fileName.isEmpty()){
            return;
        }
        provinceMeshes = new HashMap<>();
        InputStream inputStream = null;
        try {
            if (systemType == 0) {
                File file = new File(fileName);
                if (!file.exists() || !file.isFile()) {
                    return;
                }
                inputStream = new FileInputStream(file);
            } else {
                Configuration conf = new Configuration();
                inputStream = Hdfs.readFile(conf, fileName);
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String strLine = null;
            while ((strLine = bufferedReader.readLine()) != null){
                strLine = strLine.trim();
                String[] strings = strLine.split(",");
                List<String> meshs = provinceMeshes.get(strings[0]);
                if (meshs == null){
                    meshs = new ArrayList<>();
                    meshs.add(strings[1]);
                    provinceMeshes.put(strings[0] , meshs);
                }else{
                    meshs.add(strings[1]);
                }
            }
            inputStream.close();
        }catch (Exception e){
            Logger.getLogger(ProvinceUtil.class).error(e);
        }
    }

    public Collection<String> getProvinces() {
        return provinceMeshes.keySet();
    }

    public Collection<String> getProvinceMeshes(String province) {
        return provinceMeshes.get(province);
    }

    /**
     * 图幅号转瓦片号
     *
     * @param mesh 2.5万图幅号
     * @param level 墨卡托瓦片等级
     * @return 瓦片号列表
     */
    public static Collection<String> mesh2tile(String mesh, int level) {
        Envelope meshBound = MeshUtil.getMeshBound(mesh);

        MercatorUtil mercatorUtil = new MercatorUtil(256, level);

        return mercatorUtil.bound2MCode(meshBound);
    }

    /**
     * 瓦片号转图幅号
     *
     * @param tile 瓦片号
     * @param level 墨卡托瓦片等级
     * @return 图幅号列表
     */
    public static Collection<String> tile2mesh(String tile, int level) {
        MercatorUtil mercatorUtil = new MercatorUtil(256, level);

        Envelope bound = mercatorUtil.mercatorBound(tile);

        return mercatorUtil.bound2MCode(bound);
    }

    /**
     * 省名获取瓦片
     *
     * @param province 省名
     * @param level 墨卡托瓦片等级
     * @return 瓦片号列表
     */
    public Collection<String> provinceTiles(String province, int level) {
        Set<String> tiles = new HashSet<>();

        List<String> meshes = provinceMeshes.get(province);

        for (String mesh : meshes) {
            tiles.addAll(mesh2tile(mesh, level));
        }

        return tiles;
    }

    public String getProvinceByMesh(String mesh) {
        for (Map.Entry< String, List<String> > entry : provinceMeshes.entrySet()) {
            if (entry.getValue().contains(mesh)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public Collection<String> getProvinceByTile(String tile, int level) {
        Set<String> provinces = new HashSet<>();

        Collection<String> meshes = tile2mesh(tile, level);
        for (String mesh : meshes) {
            String province = getProvinceByMesh(mesh);
            if (!StringUtil.isEmpty(province)) {
                provinces.add(province);
            }
        }

        return provinces;
    }

    public static void main(String[] args) throws IOException {
        ProvinceUtil provinceUtil = new ProvinceUtil();
        provinceUtil.initProvinceMeshes();

        Collection<String> provinces = provinceUtil.getProvinces();

        for (String province : provinces) {
            Collection<String> tiles = provinceUtil.provinceTiles(province, 12);

            try (FileWriter fw = new FileWriter("D:\\mapspotter\\data\\province_list\\chengdu\\"+province+".txt")) {
                for (String tile : tiles) {
                    fw.write(province + "," + tile + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
