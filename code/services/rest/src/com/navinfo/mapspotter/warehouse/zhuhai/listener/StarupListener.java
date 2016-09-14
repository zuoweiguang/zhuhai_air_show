package com.navinfo.mapspotter.warehouse.zhuhai.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Timer;
import java.util.concurrent.Executors;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class StarupListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        sce.getServletContext().log("定时器销毁");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
//        sce.getServletContext().log("启动线程池");
        // Start a thread pool to deal with different task;
        PoolManager.pool = Executors.newFixedThreadPool(10);
//        sce.getServletContext().log("启动定时器");
        //Create a Daemon timer thread
        Timer timer=new Timer(true);

        // 每隔 120秒 执行一次
        timer.schedule(new EventsTimerTask(sce.getServletContext()), 0, 120 * 1000);
        sce.getServletContext().log("添加 [事件信息] 任务调度表");
        // 每隔 10分钟 执行一次
        timer.schedule(new ForecastsTimerTask(sce.getServletContext()), 0, 10 * 60 * 1000);
        sce.getServletContext().log("添加 [拥堵预测] 任务调度表");

    }

}
