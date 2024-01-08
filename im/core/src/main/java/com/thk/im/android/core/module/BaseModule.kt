package com.thk.im.android.core.module

interface BaseModule {
    fun reset()

    fun onSignalReceived(type: Int, body: String)
}