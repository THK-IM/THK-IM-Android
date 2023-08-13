package com.thk.im.android.core.signal.inernal

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.carlt.networklibs.NetType
import com.carlt.networklibs.NetworkManager
import com.carlt.networklibs.annotation.NetWork
import com.carlt.networklibs.utils.NetworkUtils
import com.google.gson.Gson
import com.thk.im.android.core.signal.Signal
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.utils.LLog
import okhttp3.*
import java.util.concurrent.TimeUnit


class DefaultSignalModule(app: Application, wsUrl: String, token: String) : SignalModule {
    private val mHandler = Handler(Looper.getMainLooper())
    private val heatBeatInterval = 10 * 1000L
    private val reconnectInterval = 200L // 200ms
    private var reconnectTimes = 0
    private val connectTimeout = 5L
    private var token: String
    private var wsUrl: String
    private var app: Application
    private var webSocket: WebSocket? = null
    private var status: Int = SignalListener.StatusInit
    private var signalListener: SignalListener? = null

    private val timeMap: MutableMap<String, Long> = HashMap()
    private var connId: String? = null
    private val client = "client"
    private val server = "server"

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
            onStatusChange(SignalListener.StatusDisConnected)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            LLog.d("onFailure, Throwable: ${if (t.message == null) "unknown" else t.message}")
            onStatusChange(SignalListener.StatusDisConnected)
            webSocket.close(1000, "onFailure")
            mHandler.removeCallbacksAndMessages(null)
            reconnect()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            LLog.v("onMessage: $text")
            if (reconnectTimes != 0) {
                reconnectTimes = 0
            }
            try {
                val signal = Gson().fromJson(text, Signal::class.java)
                signalListener?.onNewMessage(signal.type, signal.subType, signal.Body)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            LLog.d("onClosing, code: $code, reason: $reason")
            super.onClosing(webSocket, code, reason)
            onStatusChange(SignalListener.StatusDisConnected)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            // 没有连成功，也会触发这个回调
            LLog.d("onOpen ${response.isSuccessful}, $response")
            onStatusChange(SignalListener.StatusConnected)
            heatBeat()
        }
    }

    override fun setConnId(id: String) {
        connId = id
    }

    override fun setSeverTime(serverTime: Long) {
        synchronized(this) {
            val current = System.currentTimeMillis()
            timeMap[client] = current
            timeMap[server] = serverTime
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

    override fun getConnId(): String? {
        return connId
    }

    override fun updateToken(token: String) {
        this.token = token
    }

    override fun connect() {
        synchronized(this) {
            if (status == SignalListener.StatusConnecting || status == SignalListener.StatusConnected) {
                return
            }
            onStatusChange(SignalListener.StatusConnecting)
            NetworkManager.getInstance().registerObserver(this)
            val request = Request.Builder()
                .header("uid", token)
                .header("platform", "1")
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
        if (status != SignalListener.StatusConnected) {
            throw RuntimeException("disconnected")
        }
        LLog.v("sendMessage: $msg")
        val success = webSocket?.send(msg)
        if (success == false) {
            throw RuntimeException("send result false")
        }
    }

    override fun setSignalListener(listener: SignalListener) {
        signalListener = listener
    }

    override fun disconnect(reason: String) {
        NetworkManager.getInstance().unRegisterObserver(this)
        webSocket?.close(0, reason)
        webSocket = null
        signalListener = null
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun onStatusChange(status: Int) {
        this.status = status
        signalListener?.onStatusChange(status)
    }

    private fun reconnect() {
        if (status != SignalListener.StatusConnected
            && NetworkUtils.isAvailable()
        ) {
            mHandler.postDelayed({
                connect()
            }, reconnectInterval * reconnectTimes)
            if (reconnectTimes < 100) {
                reconnectTimes++ // 最多100x100ms=10s重连
            }
        }
    }

    @NetWork(netType = NetType.AUTO)
    fun network(netType: NetType?) {
        when (netType) {
            NetType.WIFI, NetType.CMNET, NetType.CMWAP, NetType.AUTO -> {
                LLog.d("有网络")
                reconnectTimes = 0
                reconnect()
            }
            NetType.NONE -> LLog.d("无网络")
            else -> {}
        }
    }

    private fun heatBeat() {
        if (status == SignalListener.StatusConnected) {
            try {
                sendMessage(Signal.heatBeat)
            } catch (e: RuntimeException) {
                LLog.e("IMException: ${e.message}")
            }
            mHandler.postDelayed({
                heatBeat()
            }, heatBeatInterval)
        }
    }

}
