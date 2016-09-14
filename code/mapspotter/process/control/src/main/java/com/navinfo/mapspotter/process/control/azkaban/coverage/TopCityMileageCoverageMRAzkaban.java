package com.navinfo.mapspotter.process.control.azkaban.coverage;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.coverage.TopCityMileageCoverageMR;

/**
 * Created by huanghai on 2016/4/19.
 */
public class TopCityMileageCoverageMRAzkaban extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(TopCityMileageCoverageMRAzkaban.class);

    private String coverageLinkPath;
    private String inputPath;
    private String outputPath;
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPWD;

    public TopCityMileageCoverageMRAzkaban(String name, Props props) {
        super(name, props);

        this.coverageLinkPath = props.getString("topcoverage.coverageLinkPath");
        this.inputPath = props.getString("topcoverage.inputPath");
        this.outputPath = props.getString("topcoverage.outputPath");
        this.mysqlHost = props.getString("topcoverage.mysqlHost");
        this.mysqlPort = props.getString("topcoverage.mysqlPort");
        this.mysqlDatabase = props
                .getString("topcoverage.mysqlDatabase");
        this.mysqlUsername = props
                .getString("topcoverage.mysqlUsername");
        this.mysqlPWD = props
                .getString("topcoverage.mysqlPWD");
    }

    public void run() throws Exception {
        logger.info("coverageLinkPath : " + coverageLinkPath + " inputPath : " + inputPath + " outputPath : " + outputPath + " mysqlHost : " + mysqlHost + " mysqlPort : " + mysqlPort + " mysqlDatabase : " + mysqlDatabase + " mysqlUsername : " + mysqlUsername + " mysqlPWD : " + mysqlPWD);
        TopCityMileageCoverageMR mr = new TopCityMileageCoverageMR();
        mr.azkabanRun(coverageLinkPath, inputPath, outputPath, mysqlHost, mysqlPort, mysqlDatabase, mysqlUsername, mysqlPWD);
    }
}
