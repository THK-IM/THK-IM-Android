package com.thk.im.android.core

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.base.utils.AppUtils
import com.thk.im.android.base.utils.ToastUtils
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadModule
import com.thk.im.android.core.module.BaseModule
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.module.ContactorModule
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.module.CustomModule
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactorModule
import com.thk.im.android.core.module.internal.DefaultGroupModule
import com.thk.im.android.core.module.internal.DefaultMessageModule
import com.thk.im.android.core.module.internal.DefaultUserModule
import com.thk.im.android.core.processor.ReadMessageProcessor
import com.thk.im.android.core.processor.ReeditMessageProcessor
import com.thk.im.android.core.processor.RevokeMessageProcessor
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.signal.SignalType
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.core.storage.internal.DefaultStorageModule
import com.thk.im.android.db.IMDataBase

object IMCoreManager {

    private val moduleMap: MutableMap<Int, BaseModule> = HashMap()

    private var innerSignalModule: SignalModule? = null
    var signalModule: SignalModule
        get() = this.innerSignalModule!!
        set(value) {
            this.innerSignalModule = value
        }


    private var innerImApi: IMApi? = null
    var imApi: IMApi
        get() = this.innerImApi!!
        set(value) {
            this.innerImApi = value
        }

    private var innerFileLoadModule: FileLoadModule? = null
    var fileLoadModule: FileLoadModule
        get() = this.innerFileLoadModule!!
        set(value) {
            this.innerFileLoadModule = value
        }

    private var innerStorageModule: StorageModule? = null
    var storageModule: StorageModule
        get() = this.innerStorageModule!!
        set(value) {
            this.innerStorageModule = value
        }

    private lateinit var db: IMDataBase
    private var uid: Long = 0L
    private lateinit var application: Application

    fun init(app: Application, uid: Long, debug: Boolean) {
        application = app
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        LiveEventBus.config()
            .setContext(app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
        this.uid = uid
        db = IMDataBase(app, uid)

        innerStorageModule = DefaultStorageModule(app, uid)

        registerModule(SignalType.Common.value, DefaultCommonModule())
        registerModule(SignalType.User.value, DefaultUserModule())
        registerModule(SignalType.Contactor.value, DefaultContactorModule())
        registerModule(SignalType.Group.value, DefaultGroupModule())
        registerModule(SignalType.Message.value, DefaultMessageModule())

        getMessageModule().registerMsgProcessor(ReadMessageProcessor())
        getMessageModule().registerMsgProcessor(ReeditMessageProcessor())
        getMessageModule().registerMsgProcessor(RevokeMessageProcessor())
    }

    fun connect() {
        signalModule.setSignalListener(object : SignalListener {
            override fun onSignalStatusChange(status: Int) {
                XEventBus.post(IMEvent.OnlineStatusUpdate.value, status)
            }

            override fun onNewSignal(type: Int, subType: Int, signal: String) {
                val module = moduleMap[type]
                module?.onSignalReceived(subType, signal)
            }
        })
        signalModule.connect()
    }

    fun shutdown() {
        signalModule.disconnect("shutdown")
        db.close()
    }

    fun registerModule(type: Int, module: BaseModule) {
        moduleMap[type] = module
    }

    fun getModule(type: Int): BaseModule {
        return moduleMap[type]!!
    }

    fun getCommonModule(): CommonModule {
        return getModule(SignalType.Common.value) as CommonModule
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

    fun getSelfDefineModule(): CustomModule {
        return getModule(SignalType.SelfDefine.value) as CustomModule
    }


    fun getContactorModule(): ContactorModule {
        return getModule(SignalType.Contactor.value) as ContactorModule
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