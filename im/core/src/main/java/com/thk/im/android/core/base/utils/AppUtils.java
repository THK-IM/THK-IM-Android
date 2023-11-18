package com.thk.im.android.core.base.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppUtils {

    private static final AppUtils sAppUtils = new AppUtils();
    private Application mApp;

    private AppUtils() {
    }

    public static AppUtils instance() {
        return sAppUtils;
    }

    public Application getApp() {
        return mApp;
    }

    public void init(Application app) {
        this.mApp = app;
    }

    /**
     * 获取当前本地apk的版本
     */
    public int getVersionCode() {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = mApp.getPackageManager().
                    getPackageInfo(mApp.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     */
    public String getVerName() {
        String verName = "";
        try {
            verName = mApp.getPackageManager().
                    getPackageInfo(mApp.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }


    public int getScreenWidth() {
        WindowManager wm = (WindowManager) mApp
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) mApp
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * Value of px to value of dp.
     *
     * @param pxValue The value of px.
     * @return value of dp
     */
    public float px2dp(final float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }

    /**
     * Value of sp to value of px.
     *
     * @param spValue The value of sp.
     * @return value of px
     */
    public Float sp2px(final float spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (spValue * fontScale + 0.5f);
    }

    /**
     * Value of px to value of sp.
     *
     * @param pxValue The value of px.
     * @return value of sp
     */
    public Float px2sp(final float pxValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (pxValue / fontScale + 0.5f);
    }


}
