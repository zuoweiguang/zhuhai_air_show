package com.navinfo.mapspotter.warehouse.zhuhai.listener;

import javax.servlet.ServletContext;
import java.util.TimerTask;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class StaffTimerTask extends TimerTask {

    private ServletContext context = null;
    private int  param;
    public StaffTimerTask(ServletContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        context.log("开始执行 [事件信息] 定時任务");
        // TODO 自定义
//        System.err.println("号称每10秒钟跑一次哦！我要调用线程池去执行另外的任务");
        //让线程池去跑一个任务
        PoolManager.pool.execute(new StaffWorkThread());
        context.log("--------------------[事件信息] 定時任务执行结束-----------------");
    }

}
