package com.surfapi.log;

public class Log {

    public static void log(Object obj, String msg) {
        log(obj.getClass().getName() + ": " + msg);
    }
    
    public static void log(String msg) {
        System.err.println(msg);
    }
}