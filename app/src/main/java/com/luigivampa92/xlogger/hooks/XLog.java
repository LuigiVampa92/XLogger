package com.luigivampa92.xlogger.hooks;

import android.util.Log;

import com.luigivampa92.xlogger.BuildConfig;

public final class XLog {

    private static final boolean LOG_ENABLED = BuildConfig.LOGS_ENABLED;
    private static final String LOG_TAG = BuildConfig.LOG_TAG;
    private static final int LOG_LEVEL = BuildConfig.LOG_LEVEL;
    private static final boolean LOG_TO_XPOSED = false;
    private static final String LOG_TAG_XPOSED = "Xposed";

    public static final int SILENT = Log.ASSERT;
    public static final int INFO = Log.INFO;
    public static final int DEBUG = Log.DEBUG;
    public static final int VERBOSE = Log.VERBOSE;

    private XLog() {
        throw new IllegalAccessError("No instantiation!");
    }

    private static void log(int priority, String message, Object... args) {
        if (!LOG_ENABLED || priority < LOG_LEVEL) {
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

