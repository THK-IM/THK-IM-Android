package com.thk.im.android.core.base.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.thk.im.android.core.R;
import com.thk.im.android.core.base.LanguageUtils;

import java.util.Locale;
import java.util.TimeZone;

public class AppUtils {

    private static final AppUtils sAppUtils = new AppUtils();
    private Application mApp;
    private SoundPool soundPoll;
    private int newMsgSound;


    private AppUtils() {

    }

    public static AppUtils instance() {
        return sAppUtils;
    }

    public static int dp2px(float dp) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    public Application getApp() {
        return mApp;
    }

    public void init(Application app) {
        this.mApp = app;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                    .build();
            soundPoll = new SoundPool.Builder().setContext(app)
                    .setMaxStreams(2)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPoll = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        }
        newMsgSound = soundPoll.load(app, R.raw.im_new_msg_voice_tip, 1);
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
    public String getVersionName() {
        String verName = "";
        try {
            verName = mApp.getPackageManager().
                    getPackageInfo(mApp.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public String getTimeZone() {
        int offset = TimeZone.getDefault().getRawOffset() / 3600 / 1000;
        if (offset > 0) {
            return "GMT+" + offset;
        } else {
            return "GMT" + offset;
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        char first = str.charAt(0);
        if (Character.isUpperCase(first)) {
            return str;
        } else {
            return Character.toUpperCase(first) + str.substring(1);
        }
    }

    public String getLanguage() {
        return LanguageUtils.INSTANCE.getAppLocale().getLanguage();
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

    public void notifyNewMessage() {
        AudioManager audioManager = (AudioManager) getApp().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                soundPoll.play(newMsgSound, 1f, 1f, 1, 0, 1f);
            } else {
                long[] pattern = new long[]{200, 200, 200, 200};
                VibrateUtils.INSTANCE.vibrate(getApp(), pattern, -1);
            }
        }
    }

    public void vibrate(long ms) {
        VibrateUtils.INSTANCE.vibrate(getApp(), ms);
    }
}
