package com.navinfo.mapspotter.process.control.azkaban.coverage;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.coverage.BlockCoverageToExcel;

/**
 * Created by huanghai on 2016/4/29.
 */
public class BlockCoverageToExcelAzkaban extends AbstractHadoopJob {

    private static final Logger logger = Logger.getLogger(BlockAreaCoverageMRAzkaban.class);

    private String outputPath;
    private String excelOutpath;

    public BlockCoverageToExcelAzkaban(String name, Props props) {
        super(name, props);
        this.outputPath = props.getString("blockcoverageToExcel.outputPath");
        this.excelOutpath = props.getString("blockcoverageToExcel.excelOutpath");
    }

    public void run() throws Exception {
        BlockCoverageToExcel bcte = new BlockCoverageToExcel();
        bcte.azkabanRun(outputPath, excelOutpath);
    }

}
