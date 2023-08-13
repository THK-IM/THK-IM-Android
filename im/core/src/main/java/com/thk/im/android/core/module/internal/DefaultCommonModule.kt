package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMManager
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.utils.LLog

open class DefaultCommonModule : CommonModule {

    override fun onSignalReceived(subType: Int, body: String) {
        if (subType == CommonSubType.PONG.value) {
            IMManager.getMessageModule().ackMessages()
        } else if (subType == CommonSubType.ServerTime.value) {
            val time = body.toLong()
            if (time != 0L) {
                IMManager.getSignalModule().severTime = time
            }
        } else if (subType == CommonSubType.ConnId.value) {
            IMManager.getSignalModule().connId = body
            val cTime = IMManager.getImDataBase().messageDao().findLatestMessageCTime()
            LLog.v("cTime: $cTime")
            IMManager.getMessageModule().syncOfflineMessages(cTime, 0, 50)
        }
    }
}