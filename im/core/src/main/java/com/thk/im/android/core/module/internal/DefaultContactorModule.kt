package com.thk.im.android.core.module.internal

import com.thk.im.android.core.module.ContactorModule

open class DefaultContactorModule : ContactorModule {
    override fun onSignalReceived(subType: Int, body: String) {
    }
}