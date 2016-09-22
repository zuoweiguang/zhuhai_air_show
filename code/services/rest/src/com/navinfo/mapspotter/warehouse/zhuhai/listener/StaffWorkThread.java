package com.navinfo.mapspotter.warehouse.zhuhai.listener;

import com.navinfo.mapspotter.warehouse.zhuhai.data.ActualStaffSync;
import com.navinfo.mapspotter.warehouse.zhuhai.data.ActualTimeEvent;

/**
 * Created by zuoweiguang on 2016/9/6.
 */
public class StaffWorkThread implements Runnable {
    public StaffWorkThread() {

    }

    @Override
    public void run() {
        // TODO Do something
//        System.err.println("我被线程池调用执行啦~！");
//        System.out.println("");
        new ActualStaffSync().getStaff();
    }

}
