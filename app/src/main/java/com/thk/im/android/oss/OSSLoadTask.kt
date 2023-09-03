package com.thk.im.android.oss

abstract class OSSLoadTask(val taskId : String) {

    abstract fun start()

    abstract fun notify(progress: Int, state: Int)

    abstract fun cancel()
}