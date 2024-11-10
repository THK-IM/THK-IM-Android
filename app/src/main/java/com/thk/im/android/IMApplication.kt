package com.thk.im.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.thk.im.android.api.DataRepository
import com.thk.im.android.constant.Host
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.api.internal.DefaultIMApi
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.fileloader.internal.DefaultFileLoadModule
import com.thk.im.android.core.signal.inernal.DefaultSignalModule
import com.thk.im.android.live.LiveManager
import com.thk.im.android.media.Provider
import com.thk.im.android.module.IMCipherCrypto
import com.thk.im.android.module.IMContactModule
import com.thk.im.android.module.IMCustomModule
import com.thk.im.android.module.IMDemoUIProvider
import com.thk.im.android.module.IMExternalPageRouter
import com.thk.im.android.module.IMGroupModule
import com.thk.im.android.module.IMLiveRequestProcessor
import com.thk.im.android.module.IMUserModule
import com.thk.im.android.ui.manager.IMUIManager
import com.thk.im.preview.Previewer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class IMApplication : Application() {

    private var currentActivity: Activity? = null
    override fun onCreate() {
        super.onCreate()
        LLog.v("onCreate IMApplication")
        DataRepository.init(this)
        addActivityListener()
        initIMConfig()
        val token = DataRepository.getUserToken()
        val uId = DataRepository.getUserId()
        if (token != null && uId != null) {
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
        val debug = true
        IMCoreManager.init(this, debug)
        IMCoreManager.crypto = IMCipherCrypto("1234123412341234", "0000000000000000")
        IMCoreManager.userModule = IMUserModule()
        IMCoreManager.contactModule = IMContactModule()
        IMCoreManager.groupModule = IMGroupModule()
        IMCoreManager.customModule = IMCustomModule(this)
        IMUIManager.init(this)
        IMUIManager.pageRouter = IMExternalPageRouter()
        IMUIManager.uiResourceProvider = IMDemoUIProvider(this)
        LiveManager.shared().init(this)
        LiveManager.shared().liveRequestProcessor = IMLiveRequestProcessor(this)
    }

    fun initIMUser(token: String, uId: Long): Flowable<Boolean> {
        return Flowable.create({

            val mediaProvider = Provider(this, token)
            val mediaPreviewer = Previewer(this, token, Host.MsgAPI)
            IMUIManager.mediaProvider = mediaProvider
            IMUIManager.mediaPreviewer = mediaPreviewer

            val signalModule = DefaultSignalModule(this, Host.Websocket, token)
            val fileLoaderModule = DefaultFileLoadModule(this, Host.MsgAPI, token)
            val imApi = DefaultIMApi(token, Host.MsgAPI)
            IMCoreManager.signalModule = signalModule
            IMCoreManager.fileLoadModule = fileLoaderModule
            IMCoreManager.imApi = imApi
            IMCoreManager.initUser(uId)

            LiveManager.shared().initUser(uId, token, Host.RtcApi)

            it.onNext(true)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }

    private fun addActivityListener() {
        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}

        })
    }

    fun currentActivity(): Activity? {
        return currentActivity
    }

    fun exitIMUser(): Flowable<Boolean> {
        return Flowable.create({
            IMCoreManager.shutdown()
            it.onNext(true)
            it.onComplete()
        }, BackpressureStrategy.LATEST)
    }
}