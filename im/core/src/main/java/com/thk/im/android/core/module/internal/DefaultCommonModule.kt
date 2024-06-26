package com.thk.im.android.core.module.internal

import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.signal.SignalType

open class DefaultCommonModule : CommonModule {

    private val timeMap: MutableMap<String, Long> = HashMap()
    private val client = "client"
    private val server = "server"
    private var connId = ""

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

    override fun beKickOff() {
        IMCoreManager.signalModule.disconnect("be kick off")
    }

    override fun reset() {
    }

    override fun onSignalReceived(type: Int, body: String) {
        if (type == SignalType.SignalHeatBeat.value) {

        } else if (type == SignalType.SignalSyncTime.value) {
            val time = body.toLong()
            if (time != 0L) {
                setSeverTime(time)
            }
        } else if (type == SignalType.SignalConnId.value) {
            connId = body
        } else if (type == SignalType.SignalKickOffUser.value) {
            beKickOff()
        }
    }
}