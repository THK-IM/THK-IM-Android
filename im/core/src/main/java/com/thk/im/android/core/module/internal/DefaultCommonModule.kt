package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.module.CommonModule

open class DefaultCommonModule : CommonModule {


    private val timeMap: MutableMap<String, Long> = HashMap()
    private val client = "client"
    private val server = "server"

    override fun setSeverTime(time: Long) {
        synchronized(this) {
            val current = System.currentTimeMillis()
            timeMap[client] = current
            timeMap[server] = time
        }
    }

    override fun getSeverTime(): Long {
        synchronized(this) {
            if (timeMap[client] == null || timeMap[server] == null) {
                return System.currentTimeMillis()
            } else {
                return timeMap[server]!! + System.currentTimeMillis() - timeMap[client]!!
            }
        }
    }

    override fun onSignalReceived(subType: Int, body: String) {
        if (subType == CommonSubType.PONG.value) {
            IMCoreManager.getMessageModule().ackMessagesToServer()
        } else if (subType == CommonSubType.ServerTime.value) {
            val time = body.toLong()
            if (time != 0L) {
                setSeverTime(time)
            }
        } else if (subType == CommonSubType.ConnId.value) {
            IMCoreManager.signalModule.connId = body
            IMCoreManager.getMessageModule().syncOfflineMessages()
        }
    }
}