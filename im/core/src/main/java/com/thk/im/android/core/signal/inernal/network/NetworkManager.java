package com.thk.im.android.core.signal.inernal.network;

import android.app.Application;
import android.content.IntentFilter;

import com.thk.im.android.core.signal.inernal.network.utils.Constants;

public class NetworkManager {
    private static volatile NetworkManager manager;
    private final NetworkStateReceiver receiver = new NetworkStateReceiver();
    private Application application;

    public static NetworkManager getInstance() {
        if (manager == null) {
            synchronized (NetworkManager.class) {
                if (manager == null) {
                    manager = new NetworkManager();
                }
            }
        }
        return manager;
    }

    public Application getApplication() {
        return application;
    }


    public void init(Application app) {
        this.application = app;
        //广播注册
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ANDROID_NET_ACTION);
        application.registerReceiver(receiver, filter);

    }

    public void registerObserver(NetworkListener register) {
        receiver.registerObserver(register);
    }

    public void unRegisterObserver(NetworkListener register) {
        receiver.unRegisterObserver(register);
    }

    public void unRegisterAllObserver() {
        receiver.unRegisterAllObserver();
    }

    public void release() {
        unRegisterAllObserver();
        application.unregisterReceiver(receiver);
    }


}
