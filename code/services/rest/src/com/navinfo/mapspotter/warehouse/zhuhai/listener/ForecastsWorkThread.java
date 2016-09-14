package com.navinfo.mapspotter.warehouse.zhuhai.listener;

import com.navinfo.mapspotter.warehouse.zhuhai.data.ActualTimeForecast;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class ForecastsWorkThread implements Runnable {

    private String zoneTime;

    public ForecastsWorkThread(String zoneTime) {
        this.zoneTime = zoneTime;
    }

    @Override
    public void run() {
        // TODO Do something
//        System.err.println("我被线程池调用执行啦~！");
//        System.out.println("");
        ActualTimeForecast atf = new ActualTimeForecast();
        atf.getForecast(this.zoneTime);
    }

}
