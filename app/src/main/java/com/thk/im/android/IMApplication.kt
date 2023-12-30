package com.thk.im.android

import android.app.Application
import com.thk.im.android.api.DataRepository
import com.thk.im.android.api.ExternalIMApi
import com.thk.im.android.constant.Host
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.SignalStatus
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.fileloader.internal.DefaultFileLoadModule
import com.thk.im.android.core.signal.inernal.DefaultSignalModule
import com.thk.im.android.media.Provider
import com.thk.im.android.module.IMUserModule
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.preview.Previewer
import io.reactivex.Flowable

class IMApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LLog.v("onCreate IMApplication")
        DataRepository.init(this)
        IMCoreManager.userModule = IMUserModule()
        IMCoreManager.init(this)
        IMUIManager.init(this)
    }

    fun initIM(token: String, uId: Long): Boolean {
        if (IMCoreManager.inited) {
            return IMCoreManager.signalModule.connectStatus == SignalStatus.Connected.value
        }

        val fileLoaderModule = DefaultFileLoadModule(this, Host.MsgAPI, token)
        val signalModule = DefaultSignalModule(this, Host.Websocket, token)
        val imApi = ExternalIMApi(Host.MsgAPI, token)
        IMCoreManager.fileLoadModule = fileLoaderModule
        IMCoreManager.signalModule = signalModule
        IMCoreManager.imApi = imApi

        val mediaProvider = Provider(this, token)
        val mediaPreviewer = Previewer(this, token, Host.MsgAPI)
        IMUIManager.mediaProvider = mediaProvider
        IMUIManager.mediaPreviewer = mediaPreviewer
        IMUIManager.sessionOperator = ExternalSessionOperator()

        val subscribe = object : BaseSubscriber<Long>() {
            override fun onNext(t: Long?) {
                IMCoreManager.initUser(uId, true)
                IMCoreManager.connect()
            }
        }
        Flowable.just(uId)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscribe)
        return false
    }
}