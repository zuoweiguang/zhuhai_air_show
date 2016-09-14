package com.navinfo.mapspotter.process.control.azkaban.construction;

import azkaban.jobtype.javautils.AbstractHadoopJob;
import azkaban.utils.Props;
import com.navinfo.mapspotter.foundation.util.Logger;
import com.navinfo.mapspotter.process.topic.construction.ConstructioncombinMR;

/**
 * Created by zhangjin1207 on 2016/5/28.
 */
public class ConstructioncombinAzkaban extends AbstractHadoopJob {

    private final Logger logger = Logger.getLogger(ConstructioncombinAzkaban.class);
    private String input;
    private String output;

    public ConstructioncombinAzkaban(String name , Props props){
        super(name , props);

        input = props.getString("constructioncombin.input");
        output = props.getString("constructioncombin.output");
    }
    @Override
    public void run() throws Exception{
        logger.info("Input : " + input + " Output : " + output);
        ConstructioncombinMR constructioncombinMR = new ConstructioncombinMR();
        constructioncombinMR.azkabanRun(input , output);
    }
}
