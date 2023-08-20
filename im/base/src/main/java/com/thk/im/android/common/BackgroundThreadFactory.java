package com.thk.im.android.common;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

public class BackgroundThreadFactory implements ThreadFactory {
    private final int mThreadPriority;

    public BackgroundThreadFactory(int threadPriority) {
        mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Runnable wrapperRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                runnable.run();
            }
        };
        return new Thread(wrapperRunnable);
    }
}
