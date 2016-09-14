package com.navinfo.mapspotter.process.control.azkaban.statistics;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.trackmap.TrackStatMR;

/**
 * Created by ZhangJin1207 on 2016/4/21.
 */
public class TrackstatAzkaban extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(TrackstatAzkaban.class);
    private String table;
    private String family;
    private String qualifier;
    private String blockfile;
    private String meshlist;
    private String outputpath;
    private String zookeeperHost;
    public TrackstatAzkaban(String name , Props props){
        super(name , props);
        this.table = props.getString("trackstat.table");
        this.family = props.getString("trackstat.family");
        this.qualifier = props.getString("trackstat.qualifier");
        this.blockfile = props.getString("trackstat.blockfile");
        this.meshlist = props.getString("trackstat.meshlist");
        this.outputpath = props.getString("trackstat.outputpath");
        this.zookeeperHost = props.getString("trackstat.zookeeperhost");
    }

    public void run() throws Exception{
        logger.info("table : " + table + " family : " + family + " qualifier : " + qualifier + " blockfile : " + blockfile + " meshlist : " + meshlist + " outputpath : " + outputpath);

        TrackStatMR trackStatMR = new TrackStatMR();
        trackStatMR.azkabanRun(table , family , qualifier , outputpath , blockfile , meshlist , zookeeperHost);
    }
}
