package com.thk.im.android.core.signal.inernal

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.api.internal.APITokenInterceptor
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.utils.AppUtils
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


class DefaultSignalModule(app: Application, wsUrl: String, token: String) : SignalModule,
    NetworkListener {
    private val mHandler = Handler(Looper.getMainLooper())
    private val heatBeatInterval = 10 * 1000L
    private val reconnectInterval = 3000L // 3s 重连一次
    private val connectTimeout = 5L
    private var token: String
    private var wsUrl: String
    private var app: Application
    private var webSocket: WebSocket? = null
    private var status: SignalStatus = SignalStatus.Init
    private var signalListener: SignalListener? = null

    init {
        this.app = app
        this.token = token
        this.wsUrl = wsUrl
        NetworkManager.getInstance().init(app)
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            LLog.d("DefaultSignalModule", "onClosed, code: $code, reason: $reason")
            super.onClosed(webSocket, code, reason)
            if (this@DefaultSignalModule.webSocket == webSocket) {
                onStatusChange(SignalStatus.Disconnected)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            LLog.d(
                "DefaultSignalModule",
                "onFailure, Throwable: ${if (t.message == null) "unknown" else t.message}"
            )
            super.onFailure(webSocket, t, response)
            if (this@DefaultSignalModule.webSocket == webSocket) {
                webSocket.close(3001, "onFailure")
                onStatusChange(SignalStatus.Disconnected)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            onStatusChange(SignalStatus.Connected)
            LLog.v("DefaultSignalModule", "Receive Signal: $text")
            try {
                val signal = Gson().fromJson(text, Signal::class.java)
                signalListener?.onNewSignal(signal.type, signal.body)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            LLog.d("DefaultSignalModule", "onClosing, code: $code, reason: $reason")
            super.onClosing(webSocket, code, reason)
            onStatusChange(SignalStatus.Disconnected)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            LLog.d("DefaultSignalModule", "onOpen ${response.code}, ${response.isSuccessful}")
        }
    }

    override fun connect() {
        LLog.d("DefaultSignalModule", "connect")
        NetworkManager.getInstance().registerObserver(this)
        startConnect()
    }

    override fun disconnect(reason: String) {
        LLog.d("DefaultSignalModule", "disconnect true" )
        mHandler.removeCallbacksAndMessages(null)
        signalListener = null
        NetworkManager.getInstance().unRegisterObserver(this)
        webSocket?.close(3008, reason)
        status = SignalStatus.Disconnected
    }

    private fun reconnect() {
        LLog.d("DefaultSignalModule", "reconnect")
        if (signalListener == null) {
            return
        }
        mHandler.postDelayed({
            if (NetworkUtils.isAvailable()) {
                startConnect()
            }
        }, reconnectInterval)

    }

    private fun startConnect() {
        synchronized(this) {
            if (status == SignalStatus.Connecting || status == SignalStatus.Connected) {
                return
            }
            onStatusChange(SignalStatus.Connecting)
            val request = Request.Builder()
                .header(APITokenInterceptor.clientVersionKey, AppUtils.instance().verName)
                .header(APITokenInterceptor.tokenKey, token)
                .header(APITokenInterceptor.platformKey, "Android")
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

    override fun getSignalStatus(): SignalStatus {
        return status
    }

    override fun sendSignal(signal: String) {
        if (status != SignalStatus.Connected) {
            return
        }
        val success = webSocket?.send(signal)
        if (success == false) {
            LLog.e("DefaultSignalModule, Send Signal failed $webSocket $status ${Thread.currentThread().name}")
        } else {
            LLog.d("DefaultSignalModule", "Send Signal $webSocket $status ${Thread.currentThread().name}")
        }
    }

    override fun setSignalListener(signalListener: SignalListener) {
        this.signalListener = signalListener
    }

    private fun onStatusChange(status: SignalStatus) {
        LLog.d("DefaultSignalModule", "status: ${this.status} $status")
        if (this.status != status) {
            this.status = status
            signalListener?.onSignalStatusChange(status.value)
            if (status == SignalStatus.Connected) {
                mHandler.removeCallbacksAndMessages(null)
                heatBeat()
            } else if (status == SignalStatus.Disconnected) {
                mHandler.removeCallbacksAndMessages(null)
                reconnect()
            }
        }
    }

    private fun heatBeat() {
        if (status == SignalStatus.Connected) {
            try {
                sendSignal(Signal.ping)
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
                    LLog.d("DefaultSignalModule", "有网络")
                    reconnect()
                }

                NetType.NONE -> LLog.d("DefaultSignalModule", "无网络")
            }
        }
    }

}
