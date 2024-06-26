package com.thk.im.android.core.base.utils;

import android.app.Application;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;


public class ToastUtils {

    private static final List<String> arrayList = Collections.emptyList();
    private static Application mApp;
    private static Toast mToast;

    private ToastUtils() {
    }

    public static void init(Application app) {
        mApp = app;
    }

    public static void showShort(String text) {
        Toast.makeText(mApp, text, Toast.LENGTH_SHORT).show();
    }

    public static void show(String text) {
        if (text.isEmpty())
            return;
        if (arrayList.contains(text)) {
            return;
        }
        if (null != mToast)
            mToast.cancel();
        mToast = Toast.makeText(mApp, text, Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.TOP, 0, 0);
        mToast.show();
    }

    public static void showLong(String text) {
        Toast.makeText(mApp, text, Toast.LENGTH_LONG).show();
    }

    public static void show(int resId) {
        Toast.makeText(mApp, resId, Toast.LENGTH_LONG).show();
    }
}