package com.thk.im.android;

import android.app.Application;

import com.thk.android.im.live.LiveManager;
import com.thk.im.android.base.AppUtils;
import com.thk.im.android.base.LLog;
import com.thk.im.android.base.ToastUtils;
import com.thk.im.android.core.IMCoreManager;
import com.thk.im.android.core.api.internal.DefaultIMApi;
import com.thk.im.android.core.fileloader.internal.DefaultFileLoadModule;
import com.thk.im.android.core.signal.inernal.DefaultSignalModule;
import com.thk.im.android.media.ContentProvider;
import com.thk.im.android.media.preview.VideoCache;
import com.thk.im.android.ui.manager.IMUIManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LLog.v("onCreate MyApplication");
        AppUtils.instance().init(this);
        ToastUtils.init(this);
        new Thread() {
            public void run() {
                Long uid = 4L;
                String host = "192.168.1.5:10000";
                String endpoint = "http://" + host;
                String wsEndpoint = "ws://192.168.1.5:20000/ws";
                String token = uid.toString();

                DefaultFileLoadModule fileLoaderModule = new DefaultFileLoadModule(MyApplication.this, endpoint, token);
                IMCoreManager.INSTANCE.setSignalModule(new DefaultSignalModule(MyApplication.this, wsEndpoint, uid.toString()));
                IMCoreManager.INSTANCE.setImApi(new DefaultIMApi(endpoint, token));
                IMCoreManager.INSTANCE.init(MyApplication.this, uid, true);
                IMCoreManager.INSTANCE.setFileLoadModule(fileLoaderModule);
                IMUIManager.INSTANCE.init(MyApplication.this);
                IMUIManager.INSTANCE.setContentProvider(new ContentProvider(MyApplication.this, token));

                IMCoreManager.INSTANCE.connect();
                LiveManager.Companion.shared().init(MyApplication.this, String.valueOf(uid), true);
            }
        }.start();
    }
}
