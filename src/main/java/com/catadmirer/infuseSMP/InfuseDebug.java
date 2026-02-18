package com.catadmirer.infuseSMP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class InfuseDebug {
    private static final Logger debugLogger = LoggerFactory.getLogger("InfuseDebug");

    public static void log(String msg) {
        debugLogger.debug(msg);
    }

    public static void log(String format, Object arg) {
        debugLogger.debug(format, arg);
    }

    public static void log(String format, Object arg1, Object arg2) {
        debugLogger.debug(format, arg1, arg2);
    }

    public static void log(String format, Object... arguments) {
        debugLogger.debug(format, arguments);
    }

    public static void log(String msg, Throwable t) {
        debugLogger.debug(msg, t);
    }

    public static void log(Marker marker, String msg) {
        debugLogger.debug(marker, msg);
    }

    public static void log(Marker marker, String format, Object arg) {
        debugLogger.debug(marker, format, arg);
    }

    public static void log(Marker marker, String format, Object arg1, Object arg2) {
        debugLogger.debug(marker, format, arg1, arg2);
    }

    public static void log(Marker marker, String format, Object... arguments) {
        debugLogger.debug(marker, format, arguments);
    }

    public static void log(Marker marker, String msg, Throwable t) {
        debugLogger.debug(marker, msg, t);
    }
}
