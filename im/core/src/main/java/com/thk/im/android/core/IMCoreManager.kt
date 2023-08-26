package com.thk.im.android.core

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.base.AppUtils
import com.thk.im.android.base.ToastUtils
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.api.internal.DefaultIMApi
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.event.XEventType
import com.thk.im.android.core.fileloader.FileLoaderModule
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.module.ContactorModule
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.module.SelfDefineMsgModule
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactorModule
import com.thk.im.android.core.module.internal.DefaultGroupModule
import com.thk.im.android.core.module.internal.DefaultMessageModule
import com.thk.im.android.core.module.internal.DefaultUserModule
import com.thk.im.android.core.processor.ImageMsgProcessor
import com.thk.im.android.core.processor.TextMsgProcessor
import com.thk.im.android.core.processor.UnSupportMsgProcessor
import com.thk.im.android.core.processor.VideoMsgProcessor
import com.thk.im.android.core.processor.VoiceMsgProcessor
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.signal.SignalType
import com.thk.im.android.core.signal.inernal.DefaultSignalModule
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.core.storage.internal.DefaultStorageModule
import com.thk.im.android.db.IMDataBase

object IMCoreManager {

    private val moduleMap: MutableMap<Int, CommonModule> = HashMap()
    private lateinit var signalModule: SignalModule
    private lateinit var imApi: IMApi
    private lateinit var storageModule: StorageModule
    private lateinit var fileLoaderModule: FileLoaderModule
    private lateinit var db: IMDataBase
    private var uid: Long = 0L
    private lateinit var application: Application

    fun init(app: Application, uid: Long, token: String, wsUrl: String, apiUrl: String) {
        application = app
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        LiveEventBus.config()
            .setContext(app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
        this.uid = uid
        db = IMDataBase(app, uid)
        imApi = DefaultIMApi(apiUrl, token)

        val module: SignalModule = DefaultSignalModule(app, wsUrl, token)
        registerSignalModule(module)
        registerStorageModule(DefaultStorageModule(app, uid))

        registerModule(SignalType.Common.value, DefaultCommonModule())
        registerModule(SignalType.User.value, DefaultUserModule())
        registerModule(SignalType.Contactor.value, DefaultContactorModule())
        registerModule(SignalType.Group.value, DefaultGroupModule())
        registerModule(SignalType.Message.value, DefaultMessageModule())

        getMessageModule().registerMsgProcessor(UnSupportMsgProcessor())
        getMessageModule().registerMsgProcessor(TextMsgProcessor())
        getMessageModule().registerMsgProcessor(ImageMsgProcessor())
        getMessageModule().registerMsgProcessor(VoiceMsgProcessor())
        getMessageModule().registerMsgProcessor(VideoMsgProcessor())
    }

    fun connect() {
        db.open()
        db.initData()
        getSignalModule().setSignalListener(object : SignalListener {
            override fun onStatusChange(status: Int) {
                when (status) {
                    SignalListener.StatusConnected -> {
                        XEventBus.post(XEventType.Connected.value, "")
                    }

                    SignalListener.StatusConnecting -> {
                        XEventBus.post(XEventType.Connecting.value, "")
                    }

                    SignalListener.StatusDisConnected -> {
                        XEventBus.post(XEventType.UnConnected.value, "")
                    }
                }
            }

            override fun onNewMessage(type: Int, subType: Int, msg: String) {
                val module = moduleMap[type]
                module?.onSignalReceived(subType, msg)
            }
        })
        getSignalModule().connect()
    }

    fun shutdown() {
        signalModule.disconnect("shutdown")
        db.close()
    }

    fun registerModule(type: Int, module: CommonModule) {
        moduleMap[type] = module
    }

    fun getModule(type: Int): CommonModule {
        return moduleMap[type]!!
    }

    fun getUserModule(): UserModule {
        return getModule(SignalType.User.value) as UserModule
    }

    fun getGroupModule(): GroupModule {
        return getModule(SignalType.Group.value) as GroupModule
    }

    fun getMessageModule(): MessageModule {
        return getModule(SignalType.Message.value) as MessageModule
    }

    fun getSelfDefineModule(): SelfDefineMsgModule {
        return getModule(SignalType.SelfDefine.value) as SelfDefineMsgModule
    }

    fun registerFileLoaderModule(fileLoaderModule: FileLoaderModule) {
        this.fileLoaderModule = fileLoaderModule
    }

    fun getFileLoaderModule(): FileLoaderModule {
        return fileLoaderModule
    }

    fun getContactorModule(): ContactorModule {
        return getModule(SignalType.Contactor.value) as ContactorModule
    }

    fun registerIMApi(api: IMApi) {
        this.imApi = api
    }

    fun getIMApi(): IMApi {
        return this.imApi
    }

    fun getSignalModule(): SignalModule {
        return signalModule
    }

    fun registerSignalModule(signalModule: SignalModule) {
        this.signalModule = signalModule
    }

    fun getStorageModule(): StorageModule {
        return this.storageModule
    }

    fun registerStorageModule(storageModule: StorageModule) {
        this.storageModule = storageModule
    }

    fun getImDataBase(): IMDataBase {
        return db
    }

    fun getUid(): Long {
        return uid
    }

    fun getApplication(): Application {
        return application
    }
}