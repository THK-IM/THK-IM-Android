package com.thk.im.android.core.module.internal

import com.thk.im.android.core.module.ContactModule

open class DefaultContactModule : ContactModule {
    override fun onSignalReceived(type: Int, body: String) {
    }
}