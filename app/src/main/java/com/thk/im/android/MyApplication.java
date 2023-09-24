package com.thk.im.android;

import android.app.Application;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.thk.android.im.live.LiveManager;
import com.thk.im.android.base.AppUtils;
import com.thk.im.android.base.LLog;
import com.thk.im.android.base.ToastUtils;
import com.thk.im.android.core.IMCoreManager;
import com.thk.im.android.core.api.internal.DefaultIMApi;
import com.thk.im.android.core.signal.inernal.DefaultSignalModule;
import com.thk.im.android.minio.MinioFileLoadModule;
import com.thk.im.android.oss.OSSFileLoaderModule;
import com.thk.im.android.ui.emoji.DefaultEmojiParser;
import com.thk.im.android.ui.emoji.EmojiManager;
import com.thk.im.android.ui.manager.IMItemViewManager;
import com.thk.im.android.ui.panel.component.internal.UIComponentManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.instance().init(this);
        ToastUtils.init(this);
        IMItemViewManager.INSTANCE.init(this);
        EmojiManager.INSTANCE.init(this, new DefaultEmojiParser(this));
        new Thread() {
            public void run() {
//                String bucket = "bucket";
//                String endpoint = "https://oss-cn-ss.aliyuncs.com";
//                OSSFileLoaderModule fileLoaderModule = new OSSFileLoaderModule(
//                        MyApplication.this, bucket, endpoint, String.valueOf(uid), new OSSFederationCredentialProvider() {
//
//                    @Override
//                    public OSSFederationToken getFederationToken() {
//                        return new OSSFederationToken("", "", "", "");
//                    }
//                });
                Long uid = 4L;
                String host = "192.168.1.3:10000";
                String endpoint = "http://" + host;
                String wsEndpoint = "ws://192.168.1.3:20000/ws";
                String token = uid.toString();
                MinioFileLoadModule fileLoaderModule = new MinioFileLoadModule(MyApplication.this, endpoint, token);
                IMCoreManager.INSTANCE.setSignalModule(new DefaultSignalModule(MyApplication.this, wsEndpoint, uid.toString()));
                IMCoreManager.INSTANCE.setImApi(new DefaultIMApi(endpoint, token));
                IMCoreManager.INSTANCE.init(MyApplication.this, uid, true);
                IMCoreManager.INSTANCE.setFileLoaderModule(fileLoaderModule);
                IMCoreManager.INSTANCE.connect();
                LiveManager.Companion.shared().init(MyApplication.this, String.valueOf(uid), true);
            }
        }.start();
    }
}
