package com.thk.im.android.core.signal.inernal.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thk.im.android.core.base.LLog;
import com.thk.im.android.core.signal.inernal.network.utils.Constants;
import com.thk.im.android.core.signal.inernal.network.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class NetworkStateReceiver extends BroadcastReceiver {
    private NetType netType;
    private final List<NetworkListener> listeners = new ArrayList<>();

    public NetworkStateReceiver() {
        netType = NetType.NONE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            LLog.i("intent or intent.getAction() is null");
            return;

        }
        if (intent.getAction().equalsIgnoreCase(Constants.ANDROID_NET_ACTION)) {
            netType = NetworkUtils.getNetType();
            post(netType);
        }
    }

    private void post(NetType netType) {
        for (NetworkListener listener : listeners) {
            listener.onNetworkChangeListener(netType);
        }
    }

    public void registerObserver(NetworkListener observer) {
        if (!listeners.contains(observer)) {
            listeners.add(observer);
        }
    }


    public void unRegisterObserver(NetworkListener observer) {
        if (!listeners.contains(observer)) {
            listeners.remove(observer);
        }
    }

    public void unRegisterAllObserver() {
        if (!listeners.isEmpty()) {
            listeners.clear();
        }
    }
}
