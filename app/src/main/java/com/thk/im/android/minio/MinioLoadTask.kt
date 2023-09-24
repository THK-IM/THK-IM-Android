package com.thk.im.android.minio

abstract class MinioLoadTask(val taskId : String) {

    abstract fun start()

    abstract fun notify(progress: Int, state: Int)

    abstract fun cancel()
}