package com.navinfo.mapspotter.process.topic.restriction.restricfind;

import java.util.Map;

/**
 * 路口交限的分析结果
 * Created by SongHuiXing on 2016/1/15.
 */
public class RestrictionAnalysisResult {
    //路口PID
    public long CrossPid;

    public int MeshId;

    public int AdminId;

    public String AdminName;

    //通过路口的轨迹总数
    public long TotalTrackCount;

    //比较完美的轨迹数量"4N5"
    public long PerfectTrackCount;

    //路口的母库交限
    public int[][] OriginalResMatrix;

    //路口的分析交限
    public int[][] AnalysisResMatrix;

    //解除交限的权重
    public int[][] AnalysisDeleteWeights;

    //新增交限权重
    public int[][] AnalysisNewWeights;

    //新增交限矩阵
    public int[][] NewRestrictions;

    //解除交限矩阵
    public int[][] ReleaseRestrictions;
}
