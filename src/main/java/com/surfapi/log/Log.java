package com.surfapi.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;

public class Log {

    public static void info(Object obj, String msg) {
        info(obj.getClass().getName() + ": " + msg);
    }
    
    public static void info(String msg) {
        log("INFO", msg);
    }
    
    public static void error(String msg) {
        log("ERROR", msg);
    }
    
    public static void error(Object obj, String msg) {
        error(obj.getClass().getName() + ": " + msg);
    }

    public static void error(Object obj, String msg, Throwable e) {
        error(obj, msg + ": " + etos(e));
    }
    
    protected static String etos(Throwable e) {
        StringWriter sw = new StringWriter();
        if (e != null) {
            e.printStackTrace( new PrintWriter(sw) );
        }
        return sw.toString();
    }
    
    private static void log(String type, String msg) {
        System.out.println("[" + type + "] [" + new Date() + "] " + msg);
    }

    public static void trace(Object obj, String msg) {
        trace(obj.getClass().getName() + ": " + msg);
    }
    
    public static void trace(String msg) {
        log("TRACE", msg);
    }

    public static void trace(Object obj,  Collection<?> msgs) {
        for (Object msg : msgs) {
            trace(obj, String.valueOf(msg));
        }
    }
    
    public static void trace(Object obj, String prefixMsg, Collection<?> msgs) {
        for (Object msg : msgs) {
            trace(obj, prefixMsg + String.valueOf(msg));
        }
    }


}
