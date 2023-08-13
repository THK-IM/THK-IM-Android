package com.thk.im.android.core.module

interface CommonModule {

    fun onSignalReceived(subType: Int, body: String)
}