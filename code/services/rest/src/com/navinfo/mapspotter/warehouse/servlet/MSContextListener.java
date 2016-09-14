package com.navinfo.mapspotter.warehouse.servlet;

import com.navinfo.mapspotter.warehouse.connection.DBPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by SongHuiXing on 6/27 0027.
 */
@WebListener
public class MSContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        DBPool.getInstance().initDatabaseConnections();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        DBPool.getInstance().closeConnections();
    }
}
