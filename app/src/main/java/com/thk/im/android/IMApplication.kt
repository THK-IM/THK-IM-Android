package com.thk.im.android

import android.app.Application
import com.thk.im.android.api.ApiFactory
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.api.internal.DefaultIMApi
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.fileloader.internal.DefaultFileLoadModule
import com.thk.im.android.core.signal.inernal.DefaultSignalModule
import com.thk.im.android.media.Provider
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.preview.Previewer

class IMApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LLog.v("onCreate IMApplication")
        ApiFactory.init("")
    }

    fun initIM(token: String, uId: Long): Boolean {
        if (IMCoreManager.inited) {
            return IMCoreManager.signalModule.connectStatus == SignalStatus.Connected.value
        }
        Thread{
            kotlin.run {
                val apiEndpoint = "http://msg-api.thkim.com"
                val wsEndpoint = "ws://ws.thkim.com/ws"
                val fileLoaderModule = DefaultFileLoadModule(this, apiEndpoint, token)
                val signalModule = DefaultSignalModule(this, wsEndpoint, token)
                val imApi = DefaultIMApi(apiEndpoint, token)
                IMCoreManager.init(this, uId, true)
                IMCoreManager.fileLoadModule = fileLoaderModule
                IMCoreManager.signalModule = signalModule
                IMCoreManager.imApi = imApi
                IMUIManager.init(this)
                val mediaProvider = Provider(this, token)
                val mediaPreviewer = Previewer(this, token, apiEndpoint)
                IMUIManager.mediaProvider = mediaProvider
                IMUIManager.mediaPreviewer = mediaPreviewer
                IMCoreManager.connect()
            }
        }.start()
        return false
    }
}