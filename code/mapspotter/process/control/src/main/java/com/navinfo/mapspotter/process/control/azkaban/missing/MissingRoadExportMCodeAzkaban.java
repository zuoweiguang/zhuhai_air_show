package com.navinfo.mapspotter.process.control.azkaban.missing;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.roaddetect.RoadDetectionExportMcodeMR;

/**
 * Created by cuiliang on 2016/5/3.
 */
public class MissingRoadExportMCodeAzkaban extends AbstractHadoopJob {

    private static final Logger logger = Logger.getLogger(MissingRoadDetectAzkaban.class);

    private String table_name;
    private String family;
    private String source;
    private String output;

    public MissingRoadExportMCodeAzkaban(String name, Props props) {
        super(name, props);
        this.table_name = props.getString("missing.detect.table_name");
        this.family = props.getString("missing.detect.family");
        this.source = props.getString("missing.detect.source");
        this.output = props.getString("missing.detect.output");
    }

    public void run() throws Exception {
        logger.info("table_name : " + table_name + " family : " + family +
                " source : " + source + " output : " + output);
        RoadDetectionExportMcodeMR mr = new RoadDetectionExportMcodeMR();
        mr.azkabanRun(table_name, family, source, output);
    }

}
