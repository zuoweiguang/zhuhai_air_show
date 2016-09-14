package com.navinfo.mapspotter.process.topic.restriction.io;

import com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix;
import com.navinfo.mapspotter.foundation.algorithm.rtree.SimplePointRTree;
import com.navinfo.mapspotter.foundation.util.IntCoordinate;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.SerializeUtil;
import com.navinfo.mapspotter.process.topic.restriction.CrossRaster;
import com.navinfo.mapspotter.process.topic.restriction.Link;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.geojson.Feature;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基础路网路口信息访问接口
 * Created by SongHuiXing on 2016/1/11.
 */
public class CrossInformationVistor {
    public final static String s_IndexTableName = "cross_index";
    public final static String s_InfoTableName = "cross_info";

    private static byte[] s_analyPageEnvelopeCol = null;
    private static byte[] s_analyRasterCol = null;
    private static byte[] s_analyRasterColCount = null;
    private static byte[] s_analyCrossEnvelopeCol = null;
    private static byte[] s_analyTileInfoCol = null;

    private static byte[] s_infoPidCol = null;
    private static byte[] s_infoEnvelopeCol = null;
    private static byte[] s_infoRestricCol = null;
    private static byte[] s_infoLinkDirectionCol = null;
    private static byte[] s_infoMeshIdCol = null;

    private static byte[] s_infoLinkEnvelopeCol = null;
    private static byte[] s_infoLinksCol = null;
    private static byte[] s_infoNodesCol = null;

    private static byte[] s_infoParentCrossCol = null;
    private static byte[] s_infoForbiddenTurnCol = null;
    private static byte[] s_infoChildrenCrossCol = null;

    private static SerializeUtil<int[][]> intMxUtil = new SerializeUtil<>();
    private static SerializeUtil<double[]> doubleArrayUtil = new SerializeUtil<>();
    private static SerializeUtil<long[]> longArrayUtil = new SerializeUtil<>();
    private static SerializeUtil<ArrayList<BaseCrossJsonModel.TurnFromChild>> childCrossSerializer =
            new SerializeUtil<>();

    private static SerializeUtil<ArrayList<Map.Entry<String, IntCoordinate>>> arraySerilizeUtil =
            new SerializeUtil<>();

    static{
        s_infoPidCol = Bytes.toBytes("PID");
        s_infoEnvelopeCol = Bytes.toBytes("Envelope");
        s_infoRestricCol = Bytes.toBytes("Restrictions");
        s_infoLinkDirectionCol = Bytes.toBytes("LinkDirection");
        s_infoLinkEnvelopeCol = Bytes.toBytes("LinkEnvelope");
        s_infoLinksCol = Bytes.toBytes("Links");
        s_infoNodesCol = Bytes.toBytes("Nodes");
        s_infoMeshIdCol = Bytes.toBytes("Mesh");
        s_infoParentCrossCol = Bytes.toBytes("ParentCrossID");
        s_infoForbiddenTurnCol = Bytes.toBytes("ForbiddenTurn");
        s_infoChildrenCrossCol = Bytes.toBytes("ChildrenCrosses");

        s_analyPageEnvelopeCol = Bytes.toBytes("PageEnvelope");
        s_analyCrossEnvelopeCol = Bytes.toBytes("CrossEnvelope");
        s_analyRasterCol = Bytes.toBytes("Raster");
        s_analyRasterColCount = Bytes.toBytes("ColCount");
        s_analyTileInfoCol = Bytes.toBytes("TileInfo");
    }

    public static Configuration getHBaseConfig(){
        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", "Master.Hadoop:2181");
        cfg.set("hbase.master", "Slave3.Hadoop");
        cfg.set("hbase.client.scanner.timeout.period", "60000");

        return cfg;
    }

    public final byte[] indexPageFamily;
    public final byte[] rasterColFamily;
    public final byte[] baseinfoColFamily;

    public CrossInformationVistor(String infoColFamilyName,
                                  String rasterColFamilyName,
                                  String indexColFamilyName){
        if(null != infoColFamilyName){
            baseinfoColFamily = Bytes.toBytes(infoColFamilyName);
        } else {
            baseinfoColFamily = Bytes.toBytes("Information");
        }

        if(null != rasterColFamilyName){
            rasterColFamily = Bytes.toBytes(rasterColFamilyName);
        } else {
            rasterColFamily = Bytes.toBytes("Analysis");
        }

        if(null != indexColFamilyName){
            indexPageFamily = Bytes.toBytes(indexColFamilyName);
        } else {
            indexPageFamily = Bytes.toBytes("Page1");
        }
    }

    /**
     * 构建路口索引的HBase put 信息
     * @param raster
     * @return
     */
    public Put convertCrossIndexJson2Put(CrossRaster raster){
        StringBuilder rowKey = new StringBuilder(getPidString(raster.getPid()));

        Put pt = new Put(Bytes.toBytes(rowKey.reverse().toString()));

        pt.addColumn(indexPageFamily,
                    s_analyPageEnvelopeCol,
                    doubleArrayUtil.serialize(raster.getPageEnvelope()));

        return pt;
    }

    /**
     * 构建路口基本信息的Put
     * @param crossJson
     * @return
     */
    public Put convertCrossJson2Put(BaseCrossJsonModel crossJson){
        StringBuilder rowKey = new StringBuilder(getPidString(crossJson.getPID()));

        Put pt = new Put(Bytes.toBytes(rowKey.reverse().toString()));

        pt.addColumn(baseinfoColFamily, s_infoPidCol, Bytes.toBytes(crossJson.getPID()));
        pt.addColumn(baseinfoColFamily, s_infoMeshIdCol, Bytes.toBytes(crossJson.getMesh()));
        pt.addColumn(baseinfoColFamily, s_infoRestricCol, Bytes.toBytes(crossJson.getRestriction()));
        pt.addColumn(baseinfoColFamily, s_infoLinkDirectionCol, Bytes.toBytes(crossJson.getLinkDirection()));
        pt.addColumn(baseinfoColFamily, s_infoEnvelopeCol, Bytes.toBytes(crossJson.getEnvelope()));
        pt.addColumn(baseinfoColFamily, s_infoLinkEnvelopeCol, Bytes.toBytes(crossJson.getLinkenvelope()));
        pt.addColumn(baseinfoColFamily, s_infoLinksCol, Bytes.toBytes(crossJson.getLinks()));
        pt.addColumn(baseinfoColFamily, s_infoNodesCol, Bytes.toBytes(crossJson.getNodes()));
        pt.addColumn(baseinfoColFamily, s_infoParentCrossCol, Bytes.toBytes(crossJson.getParentCrossPid()));
        pt.addColumn(baseinfoColFamily, s_infoForbiddenTurnCol, longArrayUtil.serialize(crossJson.getForbiddenUTurn()));
        pt.addColumn(baseinfoColFamily, s_infoChildrenCrossCol, childCrossSerializer.serialize(crossJson.getChildTurns()));

        return pt;
    }

    /**
     * 构建路口栅格的Put
     * @param raster
     * @return
     */
    public Put convertCrossRaster2Put(CrossRaster raster){
        StringBuilder rowKey = new StringBuilder(getPidString(raster.getPid()));

        Put pt = new Put(Bytes.toBytes(rowKey.reverse().toString()));

        int[][] sparseInt = raster.getSparseRaster();

        pt.addColumn(rasterColFamily, s_analyCrossEnvelopeCol, doubleArrayUtil.serialize(raster.getCrossEnvelope()));
        pt.addColumn(rasterColFamily, s_analyPageEnvelopeCol, doubleArrayUtil.serialize(raster.getPageEnvelope()));
        pt.addColumn(rasterColFamily, s_analyRasterCol, intMxUtil.serialize(sparseInt));
        pt.addColumn(rasterColFamily, s_analyRasterColCount, Bytes.toBytes(raster.getRasterColCount()));
        pt.addColumn(rasterColFamily, s_analyTileInfoCol, arraySerilizeUtil.serialize(raster.getCornerTilePos()));

        return pt;
    }

    public static String getPidString(long pid){
        return String.format("%08x", pid);
    }

    public static long getPid(String pidStr){
        return Long.parseLong(pidStr, 16);
    }

    private Connection m_hbaseConn = null;

    public boolean prepare(){

        if(null == m_hbaseConn || m_hbaseConn.isClosed()){
            try {
                m_hbaseConn = ConnectionFactory.createConnection(getHBaseConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(null == m_hbaseConn) {
            return false;
        } else {
            return true;
        }
    }

    public void shutdown(){
        if(null != m_hbaseConn){
            try {
                if(!m_hbaseConn.isClosed())
                    m_hbaseConn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取路口关联的Node和Link信息
     * @param crosspid
     * @return
     */
    public BaseCrossJsonModel getNodeAndLinks(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        BaseCrossJsonModel crossbasicInfo = new BaseCrossJsonModel();
        crossbasicInfo.setPID(crosspid);

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoLinksCol);
            gt.addColumn(baseinfoColFamily, s_infoNodesCol);
            gt.addColumn(baseinfoColFamily, s_infoLinkDirectionCol);

            Result re = targetTable.get(gt);

            crossbasicInfo.setLinks(Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinksCol)));
            crossbasicInfo.setNodes(Bytes.toString(re.getValue(baseinfoColFamily, s_infoNodesCol)));
            crossbasicInfo.setLinkDirection(Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinkDirectionCol)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return crossbasicInfo;
    }

    public List<Link> getLinks(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        List<Link> links = null;
        JsonUtil jsonUtil = JsonUtil.getInstance();

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoLinksCol);

            Result re = targetTable.get(gt);

            if(re.isEmpty())
                return null;

            links = Link.convert(
                        jsonUtil.readCollection(
                                    Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinksCol)),
                                    Feature.class)
                    );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }

    /**
     * 获取路口基础信息
     * @param crosspid
     * @return
     */
    public BaseCrossJsonModel getCrossInfomation(String crosspid){
        StringBuilder rowKey = new StringBuilder(crosspid);

        BaseCrossJsonModel crossbasicInfo = new BaseCrossJsonModel();
        crossbasicInfo.setPID(getPid(crosspid));

        JsonUtil jsonUtil = JsonUtil.getInstance();

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addFamily(baseinfoColFamily);

            Result re = targetTable.get(gt);

            crossbasicInfo.setMesh(Bytes.toString(re.getValue(baseinfoColFamily, s_infoMeshIdCol)));
            crossbasicInfo.setEnvelope(Bytes.toString(re.getValue(baseinfoColFamily, s_infoEnvelopeCol)));
            crossbasicInfo.setLinkenvelope(Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinkEnvelopeCol)));
            crossbasicInfo.setLinks(Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinksCol)));
            crossbasicInfo.setNodes(Bytes.toString(re.getValue(baseinfoColFamily, s_infoNodesCol)));
            crossbasicInfo.setRestriction(Bytes.toString(re.getValue(baseinfoColFamily, s_infoRestricCol)));
            crossbasicInfo.setLinkDirection(Bytes.toString(re.getValue(baseinfoColFamily, s_infoLinkDirectionCol)));
            crossbasicInfo.setParentCrossPid(Bytes.toLong(re.getValue(baseinfoColFamily, s_infoParentCrossCol)));
            crossbasicInfo.setForbiddenUTurn(longArrayUtil.deserialize(re.getValue(baseinfoColFamily, s_infoForbiddenTurnCol)));
            crossbasicInfo.setChildTurns(childCrossSerializer.deserialize(re.getValue(baseinfoColFamily, s_infoChildrenCrossCol)));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return crossbasicInfo;
    }

    /**
     * 获取路口母库的交限矩阵
     * @param crosspid
     * @return
     */
    public int[][] getOriginalRestrictionMatrix(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        int[][] resMatrix = null;

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoRestricCol);

            Result re = targetTable.get(gt);

            if(!re.isEmpty()) {

                List<int[]> matrix =
                        JsonUtil.getInstance().readIntMatrix(re.getValue(baseinfoColFamily, s_infoRestricCol));

                if (matrix.size() > 0) {
                    int rowCount = matrix.size();
                    int colCount = matrix.get(0).length;

                    resMatrix = new int[rowCount][colCount];
                    for (int i = 0; i < rowCount; i++) {
                        int[] row = matrix.get(i);
                        System.arraycopy(row, 0, resMatrix[i], 0, colCount);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resMatrix;
    }

    /**
     * 获取路口的子路口调头信息
     * @param crosspid
     * @return
     */
    public ArrayList<BaseCrossJsonModel.TurnFromChild> getChildrenTurns(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoChildrenCrossCol);

            Result re = targetTable.get(gt);

            if(re.isEmpty())
                return null;

            return childCrossSerializer.deserialize(re.getValue(baseinfoColFamily, s_infoChildrenCrossCol));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取该路口作为子路口时的理论禁调
     * @param crosspid
     * @return [进入linkid，退出linkid] 如果不是子路口则返回null
     */
    public long[] getTheoryForbiddenTurn4Child(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoParentCrossCol);
            gt.addColumn(baseinfoColFamily, s_infoForbiddenTurnCol);

            Result re = targetTable.get(gt);

            if(re.isEmpty())
                return null;

            long parentCrossid = Bytes.toLong(re.getValue(baseinfoColFamily, s_infoParentCrossCol));
            if(-1 == parentCrossid)
                return null;

            return longArrayUtil.deserialize(re.getValue(baseinfoColFamily, s_infoForbiddenTurnCol));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 通过HBase中的路口索引表格构建内存索引
     * @return
     */
    public SimplePointRTree buildMemIndex(){
        double[] indexEnv = new double[]{70, 10, 140, 60};
        short indexLevel = 9;

        SimplePointRTree tree = new SimplePointRTree(indexEnv, indexLevel);

        ResultScanner rsScanner = null;
        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_IndexTableName))){

            Scan sc = new Scan();
            sc.addFamily(indexPageFamily);
            sc.setCacheBlocks(false);
            sc.setCaching(1000);

            rsScanner = targetTable.getScanner(sc);

            StringBuilder rowkeyReverser = new StringBuilder(8);
            for(Result re : rsScanner){
                rowkeyReverser.replace(0, 8, Bytes.toString(re.getRow()));

                long id = getPid(rowkeyReverser.reverse().toString());

                double[] pageenv =
                        doubleArrayUtil.deserialize(re.getValue(indexPageFamily, s_analyPageEnvelopeCol));

                CrossPosition crossInx = new CrossPosition(pageenv,id);

                tree.insert(crossInx);
            }

            rsScanner.close();
            rsScanner = null;

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(null != rsScanner){
                rsScanner.close();
            }
        }

        return tree;
    }

    /**
     * 获取路口栅格
     * @param crosspid
     * @return
     */
    public CrossRaster getCrossesRaster(long crosspid){
        return getCrossesRaster(getPidString(crosspid));
    }

    /**
     * 获取路口栅格
     * @param crosspid
     * @return
     */
    public CrossRaster getCrossesRaster(String crosspid){
        CrossRaster raster = null;

        long pid = getPid(crosspid);

        StringBuilder rowKey = new StringBuilder(crosspid);

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){
            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addFamily(rasterColFamily);

            Result re = targetTable.get(gt);

            if(!re.isEmpty()) {
                raster = new CrossRaster();
                raster.setPid(pid);

                raster.setCrossEnvelope(doubleArrayUtil.deserialize(re.getValue(rasterColFamily, s_analyCrossEnvelopeCol)));
                raster.setPageEnvelope(doubleArrayUtil.deserialize(re.getValue(rasterColFamily, s_analyPageEnvelopeCol)));

                int[][] sparse = intMxUtil.deserialize(re.getValue(rasterColFamily, s_analyRasterCol));
                raster.setSparseRaster(sparse);

                int colCount = Bytes.toInt(re.getValue(rasterColFamily, s_analyRasterColCount));

                raster.setRasterColCount(colCount);

                raster.setCorner_tile_pos(arraySerilizeUtil.deserialize(re.getValue(rasterColFamily, s_analyTileInfoCol)));

            }
        } catch (IOException e) {
            System.err.println(String.format("Can't find raster with %d", pid));
            e.printStackTrace();
        }

        return raster;
    }

    /**
     * 查询路口的meshid
     * @param crosspid
     * @return
     */
    public int getCrossMeshID(long crosspid){
        StringBuilder rowKey = new StringBuilder(getPidString(crosspid));

        int meshid = 0;

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){

            Get gt = new Get(Bytes.toBytes(rowKey.reverse().toString()));
            gt.addColumn(baseinfoColFamily, s_infoMeshIdCol);

            Result re = targetTable.get(gt);

            if(!re.isEmpty()) {
                String mesh = Bytes.toString(re.getValue(baseinfoColFamily, s_infoMeshIdCol));

                meshid = Integer.parseInt(mesh);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return meshid;
    }

    /**
     * 将所有路口栅格以图片的形式输出到目标文件夹
     * @param outFolder
     * @return
     */
    public int exportCrossRaster(String outFolder){
        int crossCount = 0;
        ResultScanner rsScanner = null;

        try(Table targetTable = m_hbaseConn.getTable(TableName.valueOf(s_InfoTableName))){
            Scan scan = new Scan();
            scan.addColumn(rasterColFamily, s_analyRasterCol);
            scan.addColumn(rasterColFamily, s_analyRasterColCount);
            scan.setCacheBlocks(false);
            scan.setBatch(2);

            rsScanner = targetTable.getScanner(scan);

            for(Result re : rsScanner){
                StringBuilder rowkey = new StringBuilder(Bytes.toString(re.getRow()));
                long crosspid = getPid(rowkey.reverse().toString());
                int[][] sparse = intMxUtil.deserialize(re.getValue(rasterColFamily, s_analyRasterCol));
                int colCount = Bytes.toInt(re.getValue(rasterColFamily, s_analyRasterColCount));

                DoubleMatrix mx = DoubleMatrix.fromSparse(sparse[0], sparse[1], sparse[2], colCount);

                BufferedImage bimage = new BufferedImage(mx.columns, mx.rows,
                                                        BufferedImage.TYPE_INT_RGB);


                for (int i = 0; i < mx.rows; i++) {
                    for (int j = 0; j < mx.columns; j++) {
                        int v = (int)mx.get(i, j);

                        Color color = null;
                        if(v == 255){
                            color = Color.BLACK;
                        }else if(v == 0) {
                            color = new Color(255, 255, 255, 255);
                        } else {
                            int g = 0, b = 0;
                            int r = v % 3;
                            v = v / 3;
                            if(v > 0) {
                                g = v % 3;
                                v = v / 3;
                            }
                            if(v > 0){
                                b = v % 3;
                            }
                            color = new Color(255*r/3, 255*g/3, 255*b/3);
                        }

                        bimage.setRGB(j, i, color.getRGB());
                    }
                }

                File f = new File(String.format("%s\\%d.jpg", outFolder, crosspid));

                ImageIO.write(bimage, "jpg", f);

                crossCount++;
            }

            rsScanner.close();
            rsScanner = null;

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }finally {
            if(null != rsScanner){
                rsScanner.close();
            }
        }

        return crossCount;
    }
}
