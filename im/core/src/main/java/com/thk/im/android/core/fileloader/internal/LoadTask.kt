package com.thk.im.android.core.fileloader.internal

abstract class LoadTask(val taskId: String) {

    abstract fun start()

    abstract fun notify(progress: Int, state: Int, exception: Exception? = null)

    abstract fun cancel()
}