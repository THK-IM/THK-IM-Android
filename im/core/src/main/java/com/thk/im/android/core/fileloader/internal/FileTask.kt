package com.thk.im.android.core.fileloader.internal

abstract class FileTask(val taskId : String) {

    abstract fun start()

    abstract fun notify(progress: Int, state: Int)

    abstract fun cancel()
}