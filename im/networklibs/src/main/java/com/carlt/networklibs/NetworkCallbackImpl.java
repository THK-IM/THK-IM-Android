package com.carlt.networklibs;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;

import com.carlt.networklibs.utils.Constants;

/**
 * Description:
 * Company    : carlt
 * Author     : zhanglei
 * Date       : 2019/2/27 10:33
 */
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        Log.e(Constants.LOG_TAG, "网络已连接");
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        Log.e(Constants.LOG_TAG, "网络已断开");
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.e(Constants.LOG_TAG, "网络发生变更，类型为：wifi");
            } else {
                Log.e(Constants.LOG_TAG, "网络发生变更，类型为：4G");
            }
        }

    }
}
