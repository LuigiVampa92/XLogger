package com.luigivampa92.xlogger.xposed;

import android.util.Log;

public class XLog {

    private static final String LOG_TAG = "LV92_XLOGGER";
    private static final String LOG_TAG_XPOSED = "Xposed";
    private static final int LOG_LEVEL = Log.VERBOSE;  // todo make buildconfig depend on buildtype !!
    private static final boolean LOG_TO_XPOSED = false;

    private XLog() {}

    private static void log(int priority, String message, Object... args) {
        if (priority < LOG_LEVEL) {
            return;
        }

        message = String.format(message, args);
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            Throwable throwable = (Throwable) args[args.length - 1];
            String stacktraceStr = Log.getStackTraceString(throwable);
            message += '\n' + stacktraceStr;
        }

        Log.println(priority, LOG_TAG, message);

        if (LOG_TO_XPOSED) {
            Log.println(priority, LOG_TAG_XPOSED, LOG_TAG + ": " + message);
        }
    }

    public static void v(String message, Object... args) {
        log(Log.VERBOSE, message, args);
    }

    public static void d(String message, Object... args) {
        log(Log.DEBUG, message, args);
    }

    public static void i(String message, Object... args) {
        log(Log.INFO, message, args);
    }

    public static void w(String message, Object... args) {
        log(Log.WARN, message, args);
    }

    public static void e(String message, Object... args) {
        log(Log.ERROR, message, args);
    }
}

