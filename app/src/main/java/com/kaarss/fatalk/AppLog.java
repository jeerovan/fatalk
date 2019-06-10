package com.kaarss.fatalk;

import android.util.Log;

public class AppLog {
    public static void v(String tag, String message) {

        if (BuildConfig.DEBUG) Log.v(tag, message);
    }
    public static void i(String tag, String message) {

        if (BuildConfig.DEBUG) Log.i(tag, message);
    }

    public static void w(String tag, String message) {

        if (BuildConfig.DEBUG) Log.w(tag, message);
    }

    public static void d(String tag, String message) {

        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    public static void e(String tag, String message) {

        if (BuildConfig.DEBUG) Log.e(tag, message);
    }
}
