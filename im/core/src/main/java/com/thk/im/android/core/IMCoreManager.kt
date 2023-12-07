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
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactorModule
import com.thk.im.android.core.module.internal.DefaultCustomModule
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

    var commonModule = DefaultCommonModule()
    var messageModule = DefaultMessageModule()
    var userModule = DefaultUserModule()
    var contactorModule = DefaultContactorModule()
    var groupModule = DefaultGroupModule()
    var customModule = DefaultCustomModule()

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

        messageModule.registerMsgProcessor(IMReadMessageProcessor())
    }

    fun connect() {
        db.open()
        signalModule.setSignalListener(object : SignalListener {
            override fun onSignalStatusChange(status: Int) {
                XEventBus.post(IMEvent.OnlineStatusUpdate.value, status)
            }

            override fun onNewSignal(type: Int, body: String) {
                if (type == SignalType.SignalNewMessage.value) {
                    messageModule.onSignalReceived(type, body)
                } else if (type < 100) {
                    commonModule.onSignalReceived(type, body)
                } else if (type < 200) {
                    userModule.onSignalReceived(type, body)
                } else if (type < 300) {
                    contactorModule.onSignalReceived(type, body)
                } else if (type < 400) {
                    groupModule.onSignalReceived(type, body)
                } else {
                    customModule.onSignalReceived(type, body)
                }
            }
        })
        signalModule.connect()
    }

    fun shutdown() {
        signalModule.disconnect("shutdown")
        db.close()
    }

    fun getImDataBase(): IMDataBase {
        return db
    }

}