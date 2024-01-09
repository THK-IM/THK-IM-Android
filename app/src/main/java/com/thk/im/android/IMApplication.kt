package com.thk.im.android

import android.app.Application
import com.thk.im.android.api.DataRepository
import com.thk.im.android.constant.Host
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.internal.DefaultIMApi
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.fileloader.internal.DefaultFileLoadModule
import com.thk.im.android.core.signal.inernal.DefaultSignalModule
import com.thk.im.android.media.Provider
import com.thk.im.android.module.IMContactModule
import com.thk.im.android.module.IMGroupModule
import com.thk.im.android.module.IMUserModule
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.preview.Previewer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class IMApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LLog.v("onCreate IMApplication")
        DataRepository.init(this)
        initIMConfig()
        val token = DataRepository.getUserToken()
        val uId = DataRepository.getUserId()
        if (token != null && uId > 0) {
            val subscribe = object : BaseSubscriber<Boolean>() {
                override fun onNext(t: Boolean?) {
                    t?.let {
                        LLog.v("init: $it")
                    }
                }
            }
            initIMUser(token, uId)
                .compose(RxTransform.flowableToIo())
                .subscribe(subscribe)
        }
    }

    private fun initIMConfig() {
        IMCoreManager.userModule = IMUserModule()
        IMCoreManager.contactModule = IMContactModule()
        IMCoreManager.groupModule = IMGroupModule()
        IMCoreManager.init(this)
        IMUIManager.init(this)
        IMUIManager.pageRouter = ExternalPageRouter()
    }

    fun initIMUser(token: String, uId: Long): Flowable<Boolean> {
        return Flowable.create({

            val signalModule = DefaultSignalModule(this, Host.Websocket, token)
            val fileLoaderModule = DefaultFileLoadModule(this, Host.MsgAPI, token)
            val imApi = DefaultIMApi(token, Host.MsgAPI)
            val mediaProvider = Provider(this, token)
            val mediaPreviewer = Previewer(this, token, Host.MsgAPI)
            IMCoreManager.signalModule = signalModule
            IMCoreManager.fileLoadModule = fileLoaderModule
            IMCoreManager.imApi = imApi
            IMUIManager.mediaProvider = mediaProvider
            IMUIManager.mediaPreviewer = mediaPreviewer

            IMCoreManager.initUser(uId, true)

            it.onNext(true)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    fun exitIMUser(): Flowable<Boolean> {
        return Flowable.create({
            IMCoreManager.shutdown()
            it.onNext(true)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }
}