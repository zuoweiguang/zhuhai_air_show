package com.navinfo.mapspotter.process.control.azkaban.restriction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.process.topic.restriction.io.InsertCross2HBase;

/**
 * 创建路口栅格信息到HBase的任务
 * Created by SongHuiXing on 2016/4/21.
 */
public class InsertCross2HBaseAzk extends AbstractHadoopJob {
    public InsertCross2HBaseAzk(String name, Props props) {
        super(name, props);
    }

    @Override
    public void run() throws Exception {
        InsertCross2HBase insertCross = new InsertCross2HBase();

        insertCross.azkabanRun(this.getProps().toProperties());
    }
}
