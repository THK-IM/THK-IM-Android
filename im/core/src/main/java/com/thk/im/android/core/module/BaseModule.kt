package com.thk.im.android.core.module

interface BaseModule {
    fun onSignalReceived(type: Int, body: String)
}