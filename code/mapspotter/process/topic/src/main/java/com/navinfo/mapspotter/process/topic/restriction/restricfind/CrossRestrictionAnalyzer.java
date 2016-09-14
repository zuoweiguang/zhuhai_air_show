package com.navinfo.mapspotter.process.topic.restriction.restricfind;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 路口交限的分析器
 * Created by SongHuiXing on 2016/1/15.
 */
public class CrossRestrictionAnalyzer {
    private final static double s_Threshold = 0.7;
    private final static int s_minAverageTrackCount = 10;
    private final static int s_minPointDensity = 30;
    private final static int s_minTrustyTrackCount = 100;

    private static Pattern s_regex = null;

    static{
        s_regex = Pattern.compile("^(\\d+),N?,(\\d+)$");
    }

    private DoubleMatrix m_trafficMatrix;
    private DoubleMatrix m_pointCountMatrix;

    private DoubleMatrix m_originalMatrix;

    private long m_pid = 0;
    public long getPID(){
        return m_pid;
    }

    private int m_outLinkCount = 0;

    public long getRecordTrackCount(){
        return (long)m_trafficMatrix.sum();
    }

    /**
     * Constructor of CrossRestrictionAnalyzer
     * @param pid               路口PID
     * @param originalMatrix    原始交限
     */
    public CrossRestrictionAnalyzer(long pid,
                                    int[][] originalMatrix){
        m_pid = pid;
        m_outLinkCount = originalMatrix.length;
        m_trafficMatrix = DoubleMatrix.zeros(m_outLinkCount, m_outLinkCount);
        m_pointCountMatrix = DoubleMatrix.zeros(m_outLinkCount, m_outLinkCount);

        m_originalMatrix =
                new com.navinfo.mapspotter.foundation.algorithm.DoubleMatrix(originalMatrix);
    }

    /***
     * 加入一条路口通行信息
     * @param in    进入线
     * @param out   退出线
     * @param ptCount  点数量
     */
    public boolean insertTrack(short in, short out, int ptCount){
        if(in > m_outLinkCount || out > m_outLinkCount)
            return false;

        return insertTrack(String.format("%dN%d", in, out), 1);
    }

    /**
     * 添加一条轨迹
     * @param track "4N5"
     * @param ptCount 点数量
     * @return 是否是正常计算的轨迹
     */
    public boolean insertTrack(String track, int ptCount){
        Matcher mt = s_regex.matcher(track);
        if(!mt.matches())
            return false;

        String inLinkIndex = mt.group(1);
        String outLinkIndex = mt.group(2);
        if(inLinkIndex.isEmpty() || outLinkIndex.isEmpty())
            return false;

        int inIndex = Short.parseShort(inLinkIndex) -1;
        int outIndex = Short.parseShort(outLinkIndex) -1;

        long trackCount = (long)m_trafficMatrix.get(inIndex, outIndex);
        m_trafficMatrix.put(inIndex, outIndex, ++trackCount);

        long totalPointCount = (long)m_pointCountMatrix.get(inIndex, outIndex);
        m_pointCountMatrix.put(inIndex, outIndex, totalPointCount+ptCount);

        return true;
    }

    /**
     * 获取通行情况矩阵
     * @return
     */
    public DoubleMatrix getTrafficMatrix(){
        return m_trafficMatrix;
    }

    /**
     * 获取新增交限 realMatrix - originalMatrix
     * @return
     */
    public DoubleMatrix getNewRestrictions(DoubleMatrix ignore){
        DoubleMatrix restricMx = m_originalMatrix.gt(0);

        DoubleMatrix realRes = getAnalyzeRestriction();

        DoubleMatrix result = realRes.subi(restricMx).gti(0);

        return null != ignore ? result.subi(ignore).gti(0) : result;
    }

    /**
     * 获取解除交限 origin的实地交限 - realmatrix
     *
     * @return
     */
    public DoubleMatrix getReleasedRestrictions(DoubleMatrix ignore){
        DoubleMatrix restricMx = m_originalMatrix.mul(m_originalMatrix.lt(20));
        restricMx = restricMx.gti(0);

        DoubleMatrix realRes = getAnalyzeRestriction();

        return restricMx.subi(realRes).gti(0).subi(ignore);
    }

    /**
     * 获取路口的限制矩阵
     * @return
     */
    public DoubleMatrix getAnalyzeRestriction(){
        DoubleMatrix passMx = getTrafficPassMx();

        return passMx.nei(1);
    }

    /**
     * 获取路口的通行率矩阵
     * step1. 获取所有的已经记录的轨迹数量
     * step2. 根据母库的交限获取可以行车的通行方法数量
     * step3. 获取理想状态下平均每条路径通行的轨迹数
     * step4. 获取轨迹通行率矩阵
     * @return
     */
    public DoubleMatrix getThroughRateMx(){
        long totalTrackCount = getRecordTrackCount();

        int pathCount = getAllowedPathCount();

        double averageTrackCount = totalTrackCount / pathCount;

        //如果平均轨迹数量达不到最低，认为通行率为0
        if(averageTrackCount < s_minAverageTrackCount){
            return DoubleMatrix.zeros(m_outLinkCount, m_outLinkCount);
        }

        return m_trafficMatrix.div(averageTrackCount);
    }

    /**
     * 获取路口的可通行矩阵
     * step1. 获取通行率矩阵
     * step2. 实际通行率大于s_Threshold（0.7）的，认为可通行
     * @return
     */
    public DoubleMatrix getTrafficPassMx(){
        DoubleMatrix passRate = getThroughRateMx();

        //如果平均轨迹数量达不到最低，则返回母库原始可通行
        if(passRate.sum() < 0.001){
            return m_originalMatrix.eq(0);
        }

        return passRate.gei(s_Threshold);
    }

    /**
     * 获取全部通行轨迹（所有道路去除纯理论交限）
     * @return
     */
    public int getAllowedPathCount(){
        int total = m_originalMatrix.rows * m_originalMatrix.columns;

        int quitForbidden = (int)m_originalMatrix.eq(5).sum();

        int entranceForbidden = (int)m_originalMatrix.eq(6).sum();

        return total - quitForbidden - entranceForbidden;
    }

    /**
     * 获取交限解除权重矩阵
     * r -- 路径上栅格数量
     * S -- 栅格总数量
     * ∑PointCount -- 通过该路径的所有点数量
     * C -- 通过该路径的轨迹数量
     * (∑PointCount / r) -- 路径上的点密度
     * @return {[1-(r / S-1)^2] +
     *          [1 - e^(-(∑PointCount / r)/s_minPointDensity)] +
     *          [1 - e^(-C/s_minTrustyTrackCount)]}
     */
    public DoubleMatrix getDeleteWeight(DoubleMatrix raster){
        DoubleMatrix trackCellMx = getTrackCellCount(raster);

        DoubleMatrix rMx = getTrackDensity(trackCellMx, raster.rows * raster.columns);

        // ∑PointCount / r
        DoubleMatrix pMx = m_pointCountMatrix.div(trackCellMx);
        //-(∑PointCount / r)/s_minPointDensity
        pMx = pMx.divi(s_minPointDensity).muli(-1);

        // 1 - e^(-(∑PointCount / r)/s_minPointDensity)
        pMx = MatrixFunctions.expi(pMx).rsubi(1);

        // -C/s_minTrustyTrackCount
        DoubleMatrix trackMx = m_trafficMatrix.div(s_minTrustyTrackCount);
        trackMx = trackMx.muli(-1);

        // 1 - e^(-C/s_minTrustyTrackCount)
        trackMx = MatrixFunctions.expi(trackMx).rsubi(1);

        return rMx.addi(pMx).addi(trackMx).muli(100.0/3.0);
    }

    /**
     * 获取新增交限置信度矩阵
     * r -- 路径上栅格数量
     * S -- 栅格总数量
     * @param raster
     * @return {轨迹栅格比率 +
     *          轨迹通行率与总轨迹数量乘积的反比}
     */
    public DoubleMatrix getNewWeight(DoubleMatrix raster){
        DoubleMatrix trackCellMx = getTrackCellCount(raster);

        DoubleMatrix rMx = getTrackDensity(trackCellMx, raster.rows * raster.columns);

        DoubleMatrix throughRate = getThroughRateMx(); //[0--2--∞)

        // (1/200) ^ x
        throughRate = MatrixFunctions.powi(0.005, throughRate); //[1--0)

        long totalTrackCount = getRecordTrackCount();
        double trackrate = 1 - Math.exp(-(totalTrackCount / 400));

        throughRate = throughRate.muli(trackrate);

        return rMx.addi(throughRate).muli(100.0/2.0);
    }

    /**
     * 获取轨迹栅格数量矩阵
     * @param raster
     * @return
     */
    private DoubleMatrix getTrackCellCount(DoubleMatrix raster){
        int crossCount = raster.eq(255).findIndices().length;

        int[] rasterLen = new int[m_outLinkCount];
        for (int i = 0; i < m_outLinkCount; i++) {
            rasterLen[i] = raster.eq(i+1).findIndices().length;
        }

        DoubleMatrix rMx = DoubleMatrix.zeros(m_outLinkCount, m_outLinkCount);
        for (int i = 0; i < m_outLinkCount; i++) {
            for (int j = 0; j < m_outLinkCount; j++) {
                int totalRasterCount = crossCount + rasterLen[i] + rasterLen[j];
                rMx.put(i, j, totalRasterCount);
                rMx.put(j, i, totalRasterCount);
            }
        }

        return rMx;
    }

    /**
     * 获取归一化后的轨迹可信度矩阵
     * @param trackCellMx
     * @param total
     * @return
     */
    private DoubleMatrix getTrackDensity(DoubleMatrix trackCellMx, double total){
        // r/S - 1
        DoubleMatrix rMx = trackCellMx.div(total).subi(1);

        // 1 - (r/S-1)^2
        rMx = MatrixFunctions.powi(rMx, 2).rsubi(1);

        return rMx;
    }
}
