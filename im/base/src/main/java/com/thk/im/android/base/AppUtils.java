package com.thk.im.android.base;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
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

    public void init(Application app) {
        this.mApp = app;
    }

    /**
     * 获取当前本地apk的版本
     *
     * @return
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
     *
     * @return
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


}
