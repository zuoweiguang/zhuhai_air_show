package com.navinfo.mapspotter.foundation.util;

/**
 * Created by gaojian on 2016/1/6.
 */
public class Logger {
    private org.apache.log4j.Logger logger;
    private Logger(String name) {
        logger = org.apache.log4j.Logger.getLogger(name);
    }

    public static Logger getLogger(Class clazz) {
        return new Logger(clazz.getName());
    }
}
