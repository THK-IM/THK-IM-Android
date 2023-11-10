package com.thk.im.android.core.module

interface BaseModule {
    fun onSignalReceived(subType: Int, body: String)
}