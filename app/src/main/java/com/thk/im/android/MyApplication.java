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
import com.thk.im.android.db.MsgType;
import com.thk.im.android.media.Provider;
import com.thk.im.android.ui.manager.IMUIManager;
import com.thk.im.preview.Previewer;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        int value = MsgType.valueOf("UnSupport").getValue();
        LLog.v("value:" + value);
        LLog.v("onCreate MyApplication");
        AppUtils.instance().init(this);
        ToastUtils.init(this);
        new Thread() {
            public void run() {
                Long uid = 4L;
                String apiEndpoint = "http://api.thkim.com";
                String wsEndpoint = "ws://ws.thkim.com/ws";
                String token = uid.toString();

                DefaultFileLoadModule fileLoaderModule = new DefaultFileLoadModule(MyApplication.this, apiEndpoint, token);
                IMCoreManager.INSTANCE.setSignalModule(new DefaultSignalModule(MyApplication.this, wsEndpoint, uid.toString()));
                IMCoreManager.INSTANCE.setImApi(new DefaultIMApi(apiEndpoint, token));
                IMCoreManager.INSTANCE.init(MyApplication.this, uid, true);
                IMCoreManager.INSTANCE.setFileLoadModule(fileLoaderModule);
                IMUIManager.INSTANCE.init(MyApplication.this);
                IMUIManager.INSTANCE.setMediaProvider(new Provider(MyApplication.this, token));
                IMUIManager.INSTANCE.setMediaPreviewer(new Previewer(MyApplication.this, token));
                IMCoreManager.INSTANCE.connect();
                LiveManager.Companion.shared().init(MyApplication.this, String.valueOf(uid), true);
            }
        }.start();
    }
}
