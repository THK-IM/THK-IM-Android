package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.module.CommonModule

open class DefaultCommonModule : CommonModule {

    override fun onSignalReceived(subType: Int, body: String) {
        if (subType == CommonSubType.PONG.value) {
            IMCoreManager.getMessageModule().ackMessagesToServer()
        } else if (subType == CommonSubType.ServerTime.value) {
            val time = body.toLong()
            if (time != 0L) {
                IMCoreManager.signalModule.severTime = time
            }
        } else if (subType == CommonSubType.ConnId.value) {
            IMCoreManager.signalModule.connId = body
            IMCoreManager.getMessageModule().syncOfflineMessages()
        }
    }
}