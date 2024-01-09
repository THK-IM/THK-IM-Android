package com.thk.im.android.core

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.base.utils.ToastUtils
import com.thk.im.android.core.db.IMDataBase
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.internal.DefaultIMDataBase
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadModule
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactModule
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
    var contactModule = DefaultContactModule()
    var groupModule = DefaultGroupModule()
    var customModule = DefaultCustomModule()

    lateinit var signalModule: SignalModule
    lateinit var imApi: IMApi
    lateinit var fileLoadModule: FileLoadModule

    lateinit var db: IMDataBase
    lateinit var app: Application
    lateinit var storageModule: StorageModule
    var uId: Long = 0L

    fun init(app: Application) {
        this.app = app
        AppUtils.instance().init(app)
        ToastUtils.init(app)
        LiveEventBus.config().setContext(IMCoreManager.app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
    }

    fun initUser(uId: Long, debug: Boolean) {
        if (uId <= 0) {
            return
        }
        if (this.uId == uId) {
            return
        }
        shutdown()

        this.uId = uId
        db = DefaultIMDataBase(app, uId, debug)
        storageModule = DefaultStorageModule(app, uId)
        messageModule.registerMsgProcessor(IMReadMessageProcessor())
        db.open()
        connect()
    }

    private fun connect() {
        signalModule.setSignalListener(object : SignalListener {
            override fun onSignalStatusChange(status: Int) {
                if (status == SignalStatus.Connected.value) {
                    // 同步离线消息、session、联系人
                    messageModule.syncOfflineMessages()
                    messageModule.syncLatestSessionsFromServer()
                    contactModule.syncContacts()
                }
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
                    contactModule.onSignalReceived(type, body)
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
        fileLoadModule.reset()
        messageModule.reset()
        signalModule.disconnect("shutdown")
        db.close()
        this.uId = 0L
    }

    fun getImDataBase(): IMDataBase {
        return db
    }

}