package com.navinfo.mapspotter.process.control.azkaban.statistics;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.poistat.PoiStatMR;

/**
 * Created by ZhangJin1207 on 2016/4/21.
 */
public class PoistatAzkaban extends AbstractHadoopJob {

    private static final Logger logger = Logger.getLogger(PoistatAzkaban.class);
    private String inputPath;
    private String outputPath;
    private String blockfile;
    private String meshlist;
    private String split;

    public PoistatAzkaban(String name , Props props){
        super(name , props);
        this.inputPath = props.getString("poistat.inputpath");
        this.outputPath = props.getString("poistat.outputpath");
        this.blockfile = props.getString("poistat.blockfile");
        this.meshlist = props.getString("poistat.meshlist");
        this.split = props.getString("poistat.split");
    }

    public void run() throws  Exception{
        logger.info("inputpath : " + inputPath + " outputpath : " + outputPath + " blockfile : " + blockfile + " meshlist : " + meshlist + " split : " + split);
        PoiStatMR poiStatMR = new PoiStatMR();
        poiStatMR.azkabanRun(inputPath , outputPath , blockfile ,meshlist , split);
    }
}
