package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.signal.SignalType

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

    override fun onSignalReceived(type: Int, body: String) {
        if (type == SignalType.SignalHeatBeat.value) {
            IMCoreManager.messageModule.ackMessagesToServer()
        } else if (type == SignalType.SignalSyncTime.value) {
            val time = body.toLong()
            if (time != 0L) {
                setSeverTime(time)
            }
        } else if (type == SignalType.SignalConnId.value) {
            IMCoreManager.signalModule.connId = body
        }
    }
}