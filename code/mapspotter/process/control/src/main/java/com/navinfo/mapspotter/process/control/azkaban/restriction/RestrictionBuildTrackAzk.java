package com.navinfo.mapspotter.process.control.azkaban.restriction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.restriction.trackbuild.BuildDidiTrack;
import com.navinfo.mapspotter.process.topic.restriction.trackbuild.BuildSogouTrack;
import com.navinfo.mapspotter.process.topic.restriction.trackbuild.BuildTrack;

/**
 * 通过嘀嘀或者搜狗数据构建轨迹的Azkaban调度任务
 * Created by SongHuiXing on 2016/4/21.
 */
public class RestrictionBuildTrackAzk extends AbstractHadoopJob {

    private static final String DATA_TYPE = "BuildTrack.DataType";

    private Logger logger = Logger.getLogger(RestrictionBuildTrackAzk.class);

    public RestrictionBuildTrackAzk(String name, Props props) {
        super(name, props);
    }

    @Override
    public void run() throws Exception {
        Props props = this.getProps();

        String dataType = props.getString(DATA_TYPE);

        BuildTrack tracker = null;

        if(dataType.toLowerCase().equals("didi")){
            tracker = new BuildDidiTrack();
        } else if(dataType.toLowerCase().equals("sogou")){
            tracker = new BuildSogouTrack();
        }

        if(null == tracker) {
            logger.error("There is no data type : " + dataType + " be supported.");
            return;
        }

        BuildTrack.azkabanRun(tracker, props.toProperties());
    }
}
