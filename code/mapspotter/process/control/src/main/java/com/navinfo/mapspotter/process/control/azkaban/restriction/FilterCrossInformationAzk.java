package com.navinfo.mapspotter.process.control.azkaban.restriction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.process.topic.restriction.io.CleanCrossInfoMR;

/**
 * Created by SongHuiXing on 4/26 0026.
 */
public class FilterCrossInformationAzk extends AbstractHadoopJob {
    public FilterCrossInformationAzk(String name, Props props) {
        super(name, props);
    }

    @Override
    public void run() throws Exception {
        CleanCrossInfoMR filterCross = new CleanCrossInfoMR();

        filterCross.azkabanRun(this.getProps().toProperties());
    }
}
