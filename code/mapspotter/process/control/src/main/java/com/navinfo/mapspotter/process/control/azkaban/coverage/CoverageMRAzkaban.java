package com.navinfo.mapspotter.process.control.azkaban.coverage;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.process.topic.coverage.CoverageMR;

import com.navinfo.mapspotter.foundation.util.Logger;

/**
 * Created by huanghai on 2016/4/18.
 */
public class CoverageMRAzkaban extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(CoverageMRAzkaban.class);

    private String tableName; // 表名
    private String roadFamily; // 道路底图列族
    private String roadQualifie; // 道路底图列
    private String trajectoryFamily; // 轨迹列族
    private String trajectoryQualifie; // 轨迹列
    private String gpsThreholdNum; // GPS点数门限
    private String ratioThrehold; // link覆盖百分比
    private String outputPath; // 被覆盖link输出目录
    private String zookeeperHost; // zookeeper


    public CoverageMRAzkaban(String name, Props props) {
        super(name, props);
        this.tableName = props.getString("coverage.tableName");
        this.roadFamily = props.getString("coverage.roadFamily");
        this.roadQualifie = props.getString("coverage.roadQualifie");
        this.trajectoryFamily = props.getString("coverage.trajectoryFamily");
        this.trajectoryQualifie = props.getString("coverage.trajectoryQualifie");
        this.gpsThreholdNum = props
                .getString("coverage.gpsThreholdNum");
        this.ratioThrehold = props
                .getString("coverage.ratioThrehold");
        this.outputPath = props
                .getString("coverage.outputPath");
        this.zookeeperHost = props
                .getString("coverage.zookeeperHost");
    }

    public void run() throws Exception {
        logger.info("tableName : " + tableName + " roadFamily:" + roadFamily + " roadQualifie:" + roadQualifie + " trajectoryFamily:" + trajectoryFamily + " trajectoryQualifie:" + trajectoryQualifie + " gpsThreholdNum:" + gpsThreholdNum + " ratioThrehold:" + ratioThrehold + " outputPath:" + outputPath + " zookeeperHost : " + zookeeperHost);
        CoverageMR coverageMR = new CoverageMR();
        coverageMR.azkabanRun(tableName, roadFamily, roadQualifie, trajectoryFamily, trajectoryQualifie, gpsThreholdNum, ratioThrehold, outputPath, zookeeperHost);
    }
}
