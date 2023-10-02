package com.carlt.networklibs;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;

import com.carlt.networklibs.utils.Constants;


/**
 * Description:
 * Company    : carlt
 * Author     : zhanglei
 * Date       : 2019/2/26 16:23
 */
public class NetworkManager {
    private static volatile NetworkManager       manager;
    private                 Application          application;
    private final NetworkStateReceiver receiver;

    private NetworkManager() {
        receiver = new NetworkStateReceiver();
    }

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
        if (application == null) {
            throw new RuntimeException("please call init method in your app");
        }
        return application;
    }


    public void init(Application app) {
        this.application = app;
        //广播注册
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ANDROID_NET_ACTION);
        application.registerReceiver(receiver, filter);
        NetworkCallbackImpl networkCallback = new NetworkCallbackImpl();
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request = builder.build();
        ConnectivityManager connectivityManager = (ConnectivityManager) NetworkManager.getInstance()
                .getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(request, networkCallback);
            //                connmagr.unregisterNetworkCallback(networkCallback);
        }

    }

    public void registerObserver(Object register) {
        receiver.registerObserver(register);
    }

    public void unRegisterObserver(Object register) {
        receiver.unRegisterObserver(register);
    }

    public void unRegisterAllObserver() {
        receiver.unRegisterAllObserver();
    }


}
