package com.navinfo.mapspotter.process.control.azkaban.coverage;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.coverage.BlockAreaCoverageMR;

/**
 * Created by huanghai on 2016/4/29.
 */
public class BlockAreaCoverageMRAzkaban extends AbstractHadoopJob {
    private static final Logger logger = Logger.getLogger(BlockAreaCoverageMRAzkaban.class);

    private String coverageLinkPath;
    private String inputPath;
    private String outputPath;
    private String blockfilePath;

    public BlockAreaCoverageMRAzkaban(String name, Props props) {
        super(name, props);
        this.coverageLinkPath = props.getString("blockcoverage.coverageLinkPath");
        this.inputPath = props.getString("blockcoverage.inputPath");
        this.outputPath = props.getString("blockcoverage.outputPath");
        this.blockfilePath = props.getString("blockcoverage.blockfilePath");
    }

    public void run() throws Exception {
        BlockAreaCoverageMR mr = new BlockAreaCoverageMR();
        mr.azkabanRun(coverageLinkPath, inputPath, outputPath, blockfilePath);
    }
}
