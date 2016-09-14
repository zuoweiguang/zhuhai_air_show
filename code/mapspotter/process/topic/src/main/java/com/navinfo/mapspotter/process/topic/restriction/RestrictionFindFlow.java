package com.navinfo.mapspotter.process.topic.restriction;

import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.restriction.io.display.Export2Display;
import com.navinfo.mapspotter.process.topic.restriction.restricfind.RestrictionAnalysis;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 整个交限挖掘的全过程workflow
 * Created by SongHuiXing on 2016/2/22.
 */
public class RestrictionFindFlow {

    public static class WorkFlowDriver
            extends Configured implements Tool {

        private Logger log = Logger.getLogger(RestrictionFindFlow.class);

        @Override
        public int run(String[] args) throws Exception {
            if(6 != args.length){
                System.err.printf("Usage: %s <hitpath> " +
                                "<analysis_result> " +
                                "<InfoColFamily> <RasterColFamily>" +
                                "<mintrackcount> <display>\n",
                                getClass().getSimpleName());

                ToolRunner.printGenericCommandUsage(System.err);
                return -1;
            }

            log.info("HitPath:" + args[0] +
                    " AnalysisPath:" + args[1] +
                    " InfoColFamily:" + args[2] +
                    " RaterColFamily:" + args[3] +
                    " MinTrackCount:" + args[4] +
                    " DisplayPath:" + args[5]);

            Configuration conf = getConf();

            //分析交限
            int minTrackCount = Integer.parseInt(args[4]);

            Job restricAnalysisJob =
                    RestrictionAnalysis.RestrictionAnalysisDriver.getJob(conf,
                                                                        args[0],
                                                                        args[1],
                                                                        args[2],
                                                                        args[3],
                                                                        minTrackCount);

            ArrayList<ControlledJob> analysisDepends = new ArrayList<>();
            ControlledJob analysisRestriction = new ControlledJob(restricAnalysisJob, analysisDepends);

            Job displayJob = Export2Display.ExportDriver.getJob(conf, args[1], args[5], args[2]);
            ArrayList<ControlledJob> displayDepends = new ArrayList<>();
            displayDepends.add(analysisRestriction);
            ControlledJob displayControl = new ControlledJob(displayJob, displayDepends);

            JobControl jobControl = new JobControl("Restriction find workflow");
            jobControl.addJob(analysisRestriction);
            jobControl.addJob(displayControl);

            Thread thread = new Thread(jobControl);
            thread.start();

            while (true){
                if(jobControl.allFinished()){
                    jobControl.stop();
                    return 0;
                }

                List<ControlledJob> failList = jobControl.getFailedJobList();
                if(failList.size() > 0){
                    jobControl.stop();
                    return 1;
                }
            }
        }
    }

    /**
     * Azkaban调度接口
     * @param props
     * @throws Exception
     */
    public void azkabanRun(Properties props) throws Exception {
        String[] args = new String[6];

        args[0] = props.getProperty(RestrictionConfig.HITDATA_PATH);
        args[1] = props.getProperty(RestrictionConfig.ANALYSISDATA_PATH);
        args[2] = props.getProperty(RestrictionConfig.BASEINFO_COLFAMILY);
        args[3] = props.getProperty(RestrictionConfig.RASTER_COLFAMILY);
        args[4] = props.getProperty(RestrictionConfig.MINTRACK_COUNT);
        args[5] = props.getProperty(RestrictionConfig.DISPLAYDATA_PATH);

        System.exit(ToolRunner.run(new WorkFlowDriver(), args));
    }

    public static void main( String[] args )
            throws Exception {
        System.exit(ToolRunner.run(new WorkFlowDriver(), args));
    }
}
