package com.thk.im.android.core

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.common.AppUtils
import com.thk.im.android.common.ToastUtils
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
import com.thk.im.android.core.processor.BaseMsgProcessor
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

object IMManager {

    private val moduleMap: MutableMap<Int, CommonModule> = HashMap()
    private val processorMap: MutableMap<Int, BaseMsgProcessor> = HashMap()
    private lateinit var signalModule: SignalModule
    private lateinit var storageModule: StorageModule
    private lateinit var fileLoaderModule: FileLoaderModule
    private lateinit var db: IMDataBase
    private var uid: Long = 0L
    private lateinit var application: Application

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

    fun init(app: Application, uid: Long, token: String, wsUrl: String) {
        application = app
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        LiveEventBus.config()
            .setContext(app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
        this.uid = uid
        db = IMDataBase(app, uid)

        val module: SignalModule = DefaultSignalModule(app, wsUrl, token)
        registerSignalModule(module)
        registerStorageModule(DefaultStorageModule(app, uid))

        registerModule(SignalType.Common.value, DefaultCommonModule())
        registerModule(SignalType.User.value, DefaultUserModule())
        registerModule(SignalType.Contactor.value, DefaultContactorModule())
        registerModule(SignalType.Group.value, DefaultGroupModule())
        registerModule(SignalType.Message.value, DefaultMessageModule())

        registerMsgProcessor(UnSupportMsgProcessor())
        registerMsgProcessor(TextMsgProcessor())
        registerMsgProcessor(ImageMsgProcessor())
        registerMsgProcessor(VoiceMsgProcessor())
        registerMsgProcessor(VideoMsgProcessor())

    }

    fun connect() {
        Thread {
            kotlin.run {
                try {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
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

    /**
     * 注册消息处理器
     *
     * @param processor   消息处理器
     */
    fun registerMsgProcessor(processor: BaseMsgProcessor) {
        processorMap[processor.messageType()] = processor
    }

    fun registerUnSupportMsgProcessor(processor: BaseMsgProcessor) {
        registerMsgProcessor(processor)
    }

    /**
     * 获取消息处理器
     *
     * @param messageType 消息类型
     * @return NotNullable
     */
    fun getMsgProcessor(messageType: Int): BaseMsgProcessor {
        val processor = processorMap[messageType]
        return processor ?: processorMap[0]!!
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
}