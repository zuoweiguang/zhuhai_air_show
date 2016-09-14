package com.navinfo.mapspotter.process.control.azkaban.missing;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.missingroad.RoadDetectionMR;

/**
 * Created by cuiliang on 2016/4/21.
 */
public class MissingRoadDetectAzkaban extends AbstractHadoopJob {


    private static final Logger logger = Logger.getLogger(MissingRoadDetectAzkaban.class);

    private String source_table;
    private String family;
    private String source;
    private String config;
    private String target_table;

    public MissingRoadDetectAzkaban(String name, Props props) {
        super(name, props);
        this.family = props.getString("missing.detect.family");
        this.source = props.getString("missing.detect.source");
        this.config = props.getString("missing.detect.config");
        this.source_table = props.getString("missing.detect.source_table");
        this.target_table = props.getString("missing.detect.target_table");
    }

    public void run() throws Exception {
        logger.info("source_table : " + source_table + " family : " + family +
                " source : " + source + " config : " + config +
                " target_table : " + target_table);
        RoadDetectionMR mr = new RoadDetectionMR();
        mr.azkabanRun(source_table, family, source, config, target_table);
    }

}
