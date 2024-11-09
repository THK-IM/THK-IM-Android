package com.thk.im.android.live

import android.app.Application
import com.thk.im.android.live.api.DefaultLiveApi
import com.thk.im.android.live.engine.LiveRTCEngine
import com.thk.im.android.live.room.RTCRoomManager
import com.thk.im.android.live.signal.LiveSignal
import com.thk.im.android.live.signal.LiveSignalProtocol


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
    var liveSignalProtocol: LiveSignalProtocol? = null

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
        val delegate = this.liveSignalProtocol ?: return
        delegate.onSignalReceived(signal)
    }

}