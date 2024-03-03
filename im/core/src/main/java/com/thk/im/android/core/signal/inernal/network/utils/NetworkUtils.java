package com.thk.im.android.core.signal.inernal.network.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.thk.im.android.core.signal.inernal.network.NetType;
import com.thk.im.android.core.signal.inernal.network.NetworkManager;

public class NetworkUtils {
    /**
     * 网络是否可用
     */
    public static boolean isAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) NetworkManager.getInstance().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
        if (allNetworkInfo != null) {
            for (NetworkInfo networkInfo : allNetworkInfo) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取网络类型
     */
    public static NetType getNetType() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) NetworkManager.getInstance().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return NetType.NONE;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            int type = activeNetworkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE ||
                    type == ConnectivityManager.TYPE_MOBILE_SUPL ||
                    type == ConnectivityManager.TYPE_MOBILE_HIPRI ||
                    type == ConnectivityManager.TYPE_MOBILE_MMS ||
                    type == ConnectivityManager.TYPE_MOBILE_DUN
            ) {
                return NetType.Mobile;
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return NetType.WIFI;
            } else {
                return NetType.Unknown;
            }
        }
        return NetType.NONE;
    }

    public static void openNetSetting(Activity context, int code) {
        Intent intent = new Intent("/");
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
        intent.setComponent(componentName);
        context.startActivityForResult(intent, code);
    }
}
