package com.navinfo.mapspotter.process.control.azkaban.restriction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.process.topic.restriction.RestrictionFindFlow;

/**
 * 交限分析和输出的Azkaban调度类
 * Created by SongHuiXing on 2016/4/20.
 */
public class RestrictionFindAzk extends AbstractHadoopJob {
    public RestrictionFindAzk(String name, Props props){
        super(name, props);
    }

    @Override
    public void run() throws Exception {
        RestrictionFindFlow findFlow = new RestrictionFindFlow();

        findFlow.azkabanRun(getProps().toProperties());
    }
}
