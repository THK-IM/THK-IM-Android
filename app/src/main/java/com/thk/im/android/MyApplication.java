package com.thk.im.android;

import android.app.Application;

import com.thk.android.im.live.LiveManager;
import com.thk.im.android.base.AppUtils;
import com.thk.im.android.base.ToastUtils;
import com.thk.im.android.core.IMCoreManager;
import com.thk.im.android.ui.emoji.DefaultEmojiParser;
import com.thk.im.android.ui.emoji.EmojiManager;
import com.thk.im.android.ui.manager.IMItemViewManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.instance().init(this);
        ToastUtils.init(this);
        EmojiManager.INSTANCE.init(this, new DefaultEmojiParser(this));
        long uid = 1;
//        String bucket = "";
//        String endpoint = "";
//        FileLoaderModule module = new DefaultFileLoaderModule(
//                this, bucket, endpoint, String.valueOf(uid), new OSSFederationCredentialProvider() {
//
//            @Override
//            public OSSFederationToken getFederationToken() throws ClientException {
//                return new OSSFederationToken("", "", "", "");
//            }
//        });
//        IMManager.INSTANCE.registerFileLoaderModule(module);
        IMCoreManager.INSTANCE.init(this, uid, String.valueOf(uid), "ws://192.168.1.4:18002/ws", "http://192.168.1.4:18002/ws");
        IMItemViewManager.INSTANCE.init();
        LiveManager.Companion.shared().init(this, String.valueOf(uid), true);
    }
}
