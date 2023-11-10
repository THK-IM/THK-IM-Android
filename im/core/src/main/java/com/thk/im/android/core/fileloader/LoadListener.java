package com.thk.im.android.core.fileloader;

import androidx.annotation.NonNull;

public interface LoadListener {

    /**
     * 下载/上传进度
     *
     * @param progress  0-100
     * @param state     0未开始 1进行中 2成功 3失败
     * @param url       网路地址
     * @param path      本地路径
     * @param exception 异常信息
     */
    void onProgress(int progress, int state, @NonNull String url, @NonNull String path, Exception exception);

    /**
     * 是否在主线程订阅
     */
    boolean notifyOnUiThread();
}
