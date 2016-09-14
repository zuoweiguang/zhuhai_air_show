package com.navinfo.mapspotter.foundation.util;

import java.util.*;

/**
 * 封装Logger
 *
 * Created by gaojian on 2016/1/6.
 */
public class Logger {
    private org.apache.log4j.Logger logger;
    private Logger(String name) {
        logger = org.apache.log4j.Logger.getLogger(name);
    }

    private static final Object _synObj = new Object();
    private static Map<String, Logger> _loggers;
    private static Queue<String> _names;
    static {
        _loggers = new HashMap<>();
        _names = new LinkedList<>();
    }

    public static Logger getLogger(Class clazz) {
        synchronized (_synObj) {
            String name = clazz.getName();
            Logger logger = _loggers.get(name);
            if (logger == null) {
                logger = new Logger(name);
                _loggers.put(name, logger);
                _names.offer(name);
                if (_names.size() > 1000) {
                    _loggers.remove(_names.poll());
                }
            }
            return logger;
        }
    }

    public final void error(Object message) {
        logger.error(message);
    }
    public final void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public final void info(Object message) {
        logger.info(message);
    }
    public final void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    public final void warn(Object message) {
        logger.warn(message);
    }
    public final void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public final void debug(Object message) {
        logger.debug(message);
    }
    public final void debug(Object message, Throwable t) {
        logger.debug(message, t);
    }

    public final void trace(Object message) {
        logger.trace(message);
    }
    public final void trace(Object message, Throwable t) {
        logger.trace(message, t);
    }

    public final void fatal(Object message) {
        logger.fatal(message);
    }
    public final void fatal(Object message, Throwable t) {
        logger.fatal(message, t);
    }
}
