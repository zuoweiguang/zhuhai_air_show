package com.navinfo.mapspotter.process.control.azkaban.construction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.construction.ConstructionAnalysisMR;
import com.navinfo.mapspotter.process.topic.construction.ConstructionMR;


/**
 * Created by ZhangJin1207 on 2016/4/21.
 */
public class ConstructionAzkaban  extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(ConstructionAzkaban.class);

    private String inputpath;
    private String outputpath;
    private String zookeeperhost;
    private String roadtable;
    private String tracktable;
    private String roadfamily;
    private String roadqualifier;
    private String trackfamily;
    private String trackqualifier;
    private String filtervalue;
    private String split;
    private String type;//区分使用程序

    public ConstructionAzkaban(String name , Props props){
        super(name , props);

        this.inputpath = props.getString("construction.inputpath");
        this.outputpath = props.getString("construction.outputpath");
        this.zookeeperhost = props.getString("construction.zookeeperhost");
        this.roadtable = props.getString("construction.roadtable");
        this.tracktable = props.getString("construction.tracktable");
        this.roadfamily = props.getString("construction.roadfamily");
        this.roadqualifier = props.getString("construction.roadqualifier");
        this.trackfamily = props.getString("construction.trackfamily");
        this.trackqualifier = props.getString("construction.trackqualifier");
        this.filtervalue = props.getString("construction.filtervalue");
        this.split = props.getString("construction.split");
        this.type = props.getString("construction.type");
    }

    public void run() throws Exception {
        logger.info("inputpath : " + inputpath + " outputpath : " + outputpath + " filtervalue : " + filtervalue + " spilt : " + split);
        logger.info("zookeeperhost : " + zookeeperhost + " roadtable : " + roadtable + " roadfamily : " + roadfamily + " roadqualifier : " + roadqualifier + " tracktable : " + tracktable + " trackfamily : " + trackfamily + " trackqualifier : " + trackqualifier);

        if (type.equals("old")) {
            ConstructionAnalysisMR constructionAnalysisMR = new ConstructionAnalysisMR();
            constructionAnalysisMR.azkabanRun(inputpath, outputpath, zookeeperhost, roadtable, tracktable, roadfamily, roadqualifier, trackfamily, trackqualifier, filtervalue, split);
        } else {
            ConstructionMR constructionMR = new ConstructionMR();
            constructionMR.azkabanRun(inputpath, outputpath, zookeeperhost, roadtable, tracktable, roadfamily, roadqualifier, trackfamily, trackqualifier, filtervalue, split);
        }
    }
}
