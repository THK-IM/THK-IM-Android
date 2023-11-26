package com.thk.im.android.core

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.IMDataBase
import com.thk.im.android.core.db.internal.DefaultIMDataBase
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadModule
import com.thk.im.android.core.module.BaseModule
import com.thk.im.android.core.module.CommonModule
import com.thk.im.android.core.module.ContactorModule
import com.thk.im.android.core.module.CustomModule
import com.thk.im.android.core.module.GroupModule
import com.thk.im.android.core.module.MessageModule
import com.thk.im.android.core.module.UserModule
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactorModule
import com.thk.im.android.core.module.internal.DefaultGroupModule
import com.thk.im.android.core.module.internal.DefaultMessageModule
import com.thk.im.android.core.module.internal.DefaultUserModule
import com.thk.im.android.core.processor.IMReadMessageProcessor
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.signal.SignalType
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.core.storage.internal.DefaultStorageModule

object IMCoreManager {

    private val moduleMap: MutableMap<Int, BaseModule> = HashMap()

    lateinit var signalModule: SignalModule
    lateinit var imApi: IMApi
    lateinit var fileLoadModule: FileLoadModule

    lateinit var db: IMDataBase
    lateinit var app: Application
    lateinit var storageModule: StorageModule
    var uId: Long = 0L

    fun init(app: Application, uId: Long, debug: Boolean) {
        this.app = app
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        LiveEventBus.config()
            .setContext(app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
        this.uId = uId
        db = DefaultIMDataBase(app, uId, debug)
        storageModule = DefaultStorageModule(app, uId)

        registerModule(SignalType.Common.value, DefaultCommonModule())
        registerModule(SignalType.User.value, DefaultUserModule())
        registerModule(SignalType.Contactor.value, DefaultContactorModule())
        registerModule(SignalType.Group.value, DefaultGroupModule())
        registerModule(SignalType.Message.value, DefaultMessageModule())

        getMessageModule().registerMsgProcessor(IMReadMessageProcessor())
    }

    fun connect() {
        db.open()
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

}