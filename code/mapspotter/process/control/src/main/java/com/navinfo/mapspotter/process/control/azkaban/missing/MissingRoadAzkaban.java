package com.navinfo.mapspotter.process.control.azkaban.missing;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.roaddetect.ReadSourceMR;

/**
 * Created by cuiliang on 2016/4/21.
 */
public class MissingRoadAzkaban extends AbstractHadoopJob {

    private static final Logger logger = Logger.getLogger(MissingRoadAzkaban.class);

    private String source;
    private int mapNum;
    private int reduceNum;
    private String isIncrement;
    private String prefix;
    private String root;
    private String startDate;
    private String endDate;
    private String tableName;
    private String familyName;


    public MissingRoadAzkaban(String name, Props props) {
        super(name, props);
        this.source = props.getString("missing.readSource.source");
        this.mapNum = props.getInt("missing.readSource.mapNum");
        this.reduceNum = props.getInt("missing.readSource.reduceNum");
        this.isIncrement = props.getString("missing.readSource.isIncrement");
        this.prefix = props.getString("missing.readSource.prefix");
        this.root = props.getString("missing.readSource.root");
        this.startDate = props.getString("missing.readSource.startDate");
        this.endDate = props.getString("missing.readSource.endDate");
        this.startDate = props.getString("missing.readSource.startDate");
        this.endDate = props.getString("missing.readSource.endDate");
        this.tableName = props.getString("missing.readSource.tableName");
        this.familyName = props.getString("missing.readSource.familyName");
    }

    public void run() throws Exception {
        logger.info("source : " + source + " mapNum : " + mapNum +
                " reduceNum : " + reduceNum + " isIncrement : " + isIncrement +
                " prefix : " + prefix + " root : " + root +
                " startDate : " + startDate + " endDate : " + endDate +
                " tableName : " + tableName + " familyName : " + familyName);
        ReadSourceMR mr = new ReadSourceMR();
        mr.azkabanRun(source, mapNum, reduceNum, isIncrement, prefix,
                root, startDate, endDate, tableName, familyName);
    }
}
