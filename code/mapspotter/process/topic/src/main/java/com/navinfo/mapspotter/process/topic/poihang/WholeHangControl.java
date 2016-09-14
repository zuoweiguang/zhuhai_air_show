package com.navinfo.mapspotter.process.topic.poihang;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.navinfo.mapspotter.foundation.util.XmlUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Created by cuiliang on 2016/2/19.
 */
public class WholeHangControl extends Configured implements Tool {

    private static final String hdfs = "hdfs://192.168.4.166:9000/";

    public static void main(String[] args) throws Exception {
        try {
            ToolRunner.run(new WholeHangControl(), args);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("mapreduce过程失败");
            System.exit(0);
        }

    }

    public String readBox(String path, Configuration conf) {
        String box = "";
        String temp = "";
        try {
            FileSystem fileSystem = FileSystem.get(conf);
            FSDataInputStream fs = fileSystem.open(new Path(path));
            BufferedReader bis = new BufferedReader(new InputStreamReader(fs));
            while ((temp = bis.readLine()) != null) {
                box = temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return box;
    }

    private static String workspace = "mapreduce/workspace/";

    @Override
    public int run(String[] arg0) throws Exception {

        String cpName = arg0[0];
        String cpDate = arg0[1];
        String similarityConfPath = "/fusion/similarityXML/";

        String similarityConfJson = XmlUtil.parseXMLtoJson(similarityConfPath);
        String sourceBox = "";

        String sourceInput = "mapreduce/sources/" + cpName + "/" + cpDate;

        String makeBoxOutput = workspace + cpName + "/" + cpDate + "/box/output";
        String readIndexOutput = workspace + cpName + "/" + cpDate + "/index/output";
        String poiPidOutput = workspace + cpName + "/" + cpDate + "/pidHang/output";
        String poiBoxOutput = workspace + cpName + "/" + cpDate + "/poi/output";
        String busenessOutput = workspace + cpName + "/" + cpDate + "/businessHang/output";
        String busenessOutputTop = workspace + cpName + "/" + cpDate + "/businessHang/outputTop";


        //1.makeBox;
        Job makeBoundingBox = MakeBoundingBoxMR.makeBoundingBoxJob(new String[]{cpDate, hdfs + sourceInput, hdfs + makeBoxOutput});
        while (makeBoundingBox.waitForCompletion(true)) {
            //读取readBox
            sourceBox = readBox(hdfs + makeBoxOutput + "/part-r-00000", makeBoundingBox.getConfiguration());
            break;
        }

//        //2.readIndex
//        Job jobReadIndex = ReadIndexMR.readIndexJob(new String[]{cpDate, hdfs + readIndexOutput});
//        ControlledJob controlledJobReadIndex = new ControlledJob(jobReadIndex.getConfiguration());
//        controlledJobReadIndex.setJob(jobReadIndex);
//
//        //3.poiPidHang
//        Job jobPoiPid = PoiPidHangMR.poiPidHangJob(new String[]{cpDate, hdfs + sourceInput, hdfs + readIndexOutput, hdfs + poiPidOutput});
//        ControlledJob controlledPoiPid = new ControlledJob(jobPoiPid.getConfiguration());
//        controlledPoiPid.setJob(jobPoiPid);
//        controlledPoiPid.addDependingJob(controlledJobReadIndex);

        //4.readPoi
        Job jobReadPoi = ReadPoiMR.readPoiMRJob(new String[]{sourceBox, cpDate, hdfs + poiBoxOutput});
        ControlledJob controlledReadPoi = new ControlledJob(jobReadPoi.getConfiguration());
        controlledReadPoi.setJob(jobReadPoi);
//        controlledReadPoi.addDependingJob(controlledPoiPid);

        //5.poiBusinessHang
        Job jobBusinessHang = PoiBusinessHangMR.poiBusinessHangModifyMRJob(new String[]{similarityConfJson, cpDate, cpName, hdfs + poiPidOutput, hdfs + poiBoxOutput, hdfs + busenessOutput});
        ControlledJob controlledJobLoadByBusiness = new ControlledJob(jobBusinessHang.getConfiguration());
        controlledJobLoadByBusiness.setJob(jobBusinessHang);
//        controlledJobLoadByBusiness.addDependingJob(controlledPoiPid);
        controlledJobLoadByBusiness.addDependingJob(controlledReadPoi);

        //5.1
        Job jobBusinessHangTop = PoiBusinessHangTopMR.poiBusinessHangTopMRJob(new String[]{cpDate, hdfs + busenessOutput, hdfs + busenessOutputTop});
        ControlledJob controlledJobBusinessTop = new ControlledJob(jobBusinessHangTop.getConfiguration());
        controlledJobBusinessTop.setJob(jobBusinessHangTop);
        controlledJobBusinessTop.addDependingJob(controlledJobLoadByBusiness);


        // endjob

        JobControl jbcntrl = new JobControl("jbcntrl");
//        jbcntrl.addJob(controlledJobReadIndex);
//        jbcntrl.addJob(controlledPoiPid);
        jbcntrl.addJob(controlledReadPoi);
        jbcntrl.addJob(controlledJobLoadByBusiness);
        jbcntrl.addJob(controlledJobBusinessTop);

        Thread jcThread = new Thread(jbcntrl);
        jcThread.start();
        while (true) {
            if (jbcntrl.allFinished()) {
                jbcntrl.stop();
                break;
            }
            if (jbcntrl.getFailedJobList().size() > 0) {
                jbcntrl.stop();
                break;
            }
        }
        return 0;
    }


}
