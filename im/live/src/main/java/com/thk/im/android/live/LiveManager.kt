package com.thk.im.android.live

import android.app.Application
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.live.api.DefaultLiveApi
import com.thk.im.android.live.engine.LiveRTCEngine
import com.thk.im.android.live.room.RTCRoomManager


class LiveManager private constructor() {

    companion object {
        private var shareManager: LiveManager? = null

        @Synchronized
        fun shared(): LiveManager {
            if (shareManager == null) {
                shareManager = LiveManager()
            }
            return shareManager as LiveManager
        }
    }

    var app: Application? = null
    var liveCallingProtocol: LiveCallingProtocol? = null

    fun init(app: Application) {
        this.app = app
        LiveRTCEngine.shared().init(app)
    }

    fun initUser(id: Long, token: String, serverURL: String) {
        val liveApi = DefaultLiveApi(token, serverURL)
        RTCRoomManager.shared().liveApi = liveApi
        RTCRoomManager.shared().myUId = id
    }

    fun onLiveSignalReceived(signal: LiveSignal) {
        when (signal.type) {
            LiveSignalType.BeingRequesting.value -> {
                signal.signalForType(
                    LiveSignalType.BeingRequesting.value,
                    BeingRequestingSignal::class.java
                )?.let {
                    liveCallingProtocol?.onBeCalling(it)
                }
            }

            LiveSignalType.CancelRequesting.value -> {
                signal.signalForType(
                    LiveSignalType.CancelRequesting.value,
                    CancelRequestingSignal::class.java
                )?.let {
                    liveCallingProtocol?.onCancelBeCalling(it)
                }
            }

            else -> {
                XEventBus.post(liveSignalEvent, signal)
            }
        }
    }

}