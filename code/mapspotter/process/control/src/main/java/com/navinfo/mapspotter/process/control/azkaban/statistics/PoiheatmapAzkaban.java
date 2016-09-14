package com.navinfo.mapspotter.process.control.azkaban.statistics;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.poistat.HeatmapMR;

/**
 * Created by ZhangJin1207 on 2016/4/21.
 */
public class PoiheatmapAzkaban  extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(PoiheatmapAzkaban.class);

    private String inputpath;
    private String mongohost;
    private String mongoport;
    private String mongodb;
    private String table;
    private String level;
    private String adminfile;
    private String split;
    public PoiheatmapAzkaban(String name , Props props){
        super(name , props);
        this.inputpath = props.getString("poiheatmap.inputpath");
        this.mongohost = props.getString("poiheatmap.mongohost");
        this.mongoport = props.getString("poiheatmap.mongoport");
        this.mongodb = props.getString("poiheatmap.mongodb");
        this.table = props.getString("poiheatmap.table");
        this.level = props.getString("poiheatmap.level");
        this.adminfile = props.getString("poiheatmap.adminfile");
        this.split = props.getString("poiheatmap.split");
    }

    public void run() throws Exception {
        logger.info("mongohost : " + mongohost + " mongoport : " + mongoport + " mongodb : " + mongodb + " table : " + table);
        logger.info("inputpath : " + inputpath + " level : " + level + " adminfile : " + adminfile + " split : " + split);

        HeatmapMR heatmapMR = new HeatmapMR();
        heatmapMR.azkabanRun(inputpath , mongohost , mongoport , mongodb , table , level , adminfile , split);
    }
}
