package com.navinfo.mapspotter.process.control.azkaban.restriction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.process.topic.restriction.trackhit.HitTrackInCross;

/**
 * 交限打点的Azkaban调度
 * Created by SongHuiXing on 2016/4/21.
 */
public class RestrictionHitAzk extends AbstractHadoopJob {
    public RestrictionHitAzk(String name, Props props) {
        super(name, props);
    }

    @Override
    public void run() throws Exception {
        HitTrackInCross hitAzk = new HitTrackInCross();

        hitAzk.azkabanRun(getProps().toProperties());
    }
}
