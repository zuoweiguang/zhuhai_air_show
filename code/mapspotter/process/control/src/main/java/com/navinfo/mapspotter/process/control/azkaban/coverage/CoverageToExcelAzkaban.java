package com.navinfo.mapspotter.process.control.azkaban.coverage;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.coverage.CoverageToExcel;

/**
 * Created by huanghai on 2016/4/19.
 */
public class CoverageToExcelAzkaban extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(CoverageToExcelAzkaban.class);

    private String coverageMileagePath;
    private String templateXlsPath;
    private String outpath;

    public CoverageToExcelAzkaban(String name, Props props) {
        super(name, props);
        this.coverageMileagePath = props.getString("coverageToExcel.coverageMileagePath");
        this.templateXlsPath = props.getString("coverageToExcel.templateXlsPath");
        this.outpath = props.getString("coverageToExcel.outpath");
    }

    public void run() throws Exception {
        logger.info("coverageMileagePath : " + coverageMileagePath + " templateXlsPath : " + templateXlsPath + " outpath : " + outpath);
        CoverageToExcel cte = new CoverageToExcel();
        cte.azkabanRun(coverageMileagePath, templateXlsPath, outpath);
    }
}
