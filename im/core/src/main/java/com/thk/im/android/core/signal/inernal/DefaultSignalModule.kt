package com.thk.im.android.core.signal.inernal

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.signal.Signal
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.signal.inernal.network.NetType
import com.thk.im.android.core.signal.inernal.network.NetworkListener
import com.thk.im.android.core.signal.inernal.network.NetworkManager
import com.thk.im.android.core.signal.inernal.network.utils.NetworkUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


class DefaultSignalModule(app: Application, wsUrl: String, token: String) : SignalModule, NetworkListener {
    private val mHandler = Handler(Looper.getMainLooper())
    private val heatBeatInterval = 10 * 1000L
    private val reconnectInterval = 3000L // 3s 重连一次
    private val connectTimeout = 5L
    private var token: String
    private var wsUrl: String
    private var app: Application
    private var webSocket: WebSocket? = null
    private var status: Int = SignalStatus.Init.value
    private var signalListener: SignalListener? = null

    private var connId: String? = null

    init {
        this.app = app
        this.token = token
        this.wsUrl = wsUrl
        NetworkManager.getInstance().init(app)
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            LLog.d("onClosed, code: $code, reason: $reason")
            super.onClosed(webSocket, code, reason)
            mHandler.removeCallbacksAndMessages(null)
            onStatusChange(SignalStatus.Disconnected.value)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            LLog.d("onFailure, Throwable: ${if (t.message == null) "unknown" else t.message}")
            onStatusChange(SignalStatus.Disconnected.value)
            webSocket.close(1000, "onFailure")
            mHandler.removeCallbacksAndMessages(null)
            reconnect()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            LLog.v("Receive Signal: $text")
            try {
                val signal = Gson().fromJson(text, Signal::class.java)
                signalListener?.onNewSignal(signal.type, signal.body)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            LLog.d("onClosing, code: $code, reason: $reason")
            super.onClosing(webSocket, code, reason)
            onStatusChange(SignalStatus.Disconnected.value)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            LLog.d("onOpen ${response.code}, $response")
            if (response.code == 101) {
                onStatusChange(SignalStatus.Connected.value)
                heatBeat()
            } else {
                reconnect()
            }
        }
    }

    override fun setConnId(id: String) {
        connId = id
    }

    override fun getConnId(): String? {
        return connId
    }

    override fun updateToken(token: String) {
        this.token = token
    }

    override fun connect() {
        NetworkManager.getInstance().registerObserver(this)
        startConnect()
    }
    override fun disconnect(reason: String) {
        NetworkManager.getInstance().unRegisterObserver(this)
        webSocket?.close(0, reason)
        webSocket = null
        signalListener = null
        mHandler.removeCallbacksAndMessages(null)
    }
    private fun reconnect() {
        LLog.v("reconnect")
        mHandler.postDelayed({
            if (NetworkUtils.isAvailable()) {
                startConnect()
            }
        }, reconnectInterval)

    }

    private fun startConnect() {
        synchronized(this) {
            if (status == SignalStatus.Connecting.value || status == SignalStatus.Connected.value) {
                return
            }
            onStatusChange(SignalStatus.Connecting.value)
            val request = Request.Builder()
                .header("token", token)
                .header("platform", "android")
                .url(wsUrl)
                .build()
            val client = OkHttpClient.Builder()
                .readTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(connectTimeout, TimeUnit.SECONDS)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .build()
            webSocket = client.newWebSocket(request, webSocketListener)
            webSocket?.request()
        }
    }

    override fun getConnectStatus(): Int {
        return status
    }

    override fun sendMessage(msg: String) {
        if (status != SignalStatus.Connected.value) {
            throw RuntimeException("disconnected")
        }
        LLog.v("Send Signal: $msg")
        val success = webSocket?.send(msg)
        if (success == false) {
            LLog.e("send result false")
        }
    }

    override fun setSignalListener(listener: SignalListener) {
        signalListener = listener
    }

    private fun onStatusChange(status: Int) {
        this.status = status
        signalListener?.onSignalStatusChange(status)
    }

    private fun heatBeat() {
        if (status == SignalStatus.Connected.value) {
            try {
                sendMessage(Signal.ping)
            } catch (e: RuntimeException) {
                LLog.e("IMException: ${e.message}")
            }
            mHandler.postDelayed({
                heatBeat()
            }, heatBeatInterval)
        }
    }

    override fun onNetworkChangeListener(type: NetType?) {
        type?.let {
            when (it) {
                NetType.WIFI, NetType.Mobile, NetType.Unknown -> {
                    LLog.d("有网络")
                    reconnect()
                }
                NetType.NONE -> LLog.d("无网络")
            }
        }
    }

}
