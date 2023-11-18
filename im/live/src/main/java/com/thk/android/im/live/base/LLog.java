package com.thk.android.im.live.base;

import android.util.Log;

import androidx.annotation.NonNull;

public class LLog {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int ASSERT = 6;

    private static int level = VERBOSE;
    private static String tag = "THK_IM";

    /**
     * @param tag:   tag
     * @param level:
     */
    public static void init(String tag, int level) {
        LLog.tag = tag;
        LLog.level = level;
    }

    public static void v(@NonNull String tag, @NonNull String message) {
        if (VERBOSE >= LLog.level) {
            Log.v(tag, message);
        }
    }

    public static void v(@NonNull String message) {
        if (VERBOSE >= LLog.level) {
            Log.v(LLog.tag, message);
        }
    }

    public static void d(@NonNull String message) {
        if (DEBUG >= LLog.level) {
            Log.d(LLog.tag, message);
        }
    }

    public static void d(@NonNull String tag, @NonNull String message) {
        if (DEBUG >= LLog.level) {
            Log.d(tag, message);
        }
    }

    public static void i(@NonNull String message) {
        if (INFO >= LLog.level) {
            Log.i(LLog.tag, message);
        }
    }

    public static void w(@NonNull String message) {
        if (WARN >= LLog.level) {
            Log.w(LLog.tag, message);
        }
    }

    public static void e(@NonNull String message) {
        if (ERROR >= LLog.level) {
            Log.e(LLog.tag, message + ", " + Log.getStackTraceString(new Throwable()));
        }
    }

    public static void a(@NonNull String message) {
        if (ASSERT >= LLog.level) {
            Log.e(LLog.tag, message + ", " + Log.getStackTraceString(new Throwable()));
        }
    }

}
