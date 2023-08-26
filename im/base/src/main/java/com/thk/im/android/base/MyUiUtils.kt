package com.thk.im.android.base


object MyUiUtils {
    /**
     * 在主线程执行
     * @param runnable
     */
    fun runOnMainThread(runnable: Runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable)
    }

    fun runOnMainThread(runnable: Runnable, time: Long) {
        EaseThreadManager.getInstance().runOnMainThread(runnable, time)
    }

    /**
     * 在异步线程
     * @param runnable
     */
    fun runOnIOThread(runnable: Runnable?) {
        EaseThreadManager.getInstance().runOnIOThread(runnable)
    }

    fun runOnBackground(runnable: Runnable?) {
        EaseThreadManager.getInstance().runOnIOThread(runnable)
    }


}