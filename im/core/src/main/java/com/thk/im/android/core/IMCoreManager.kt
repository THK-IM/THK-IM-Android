package com.thk.im.android.core

import android.app.Application
import androidx.annotation.Keep
import com.jeremyliao.liveeventbus.LiveEventBus
import com.thk.im.android.core.api.IMApi
import com.thk.im.android.core.base.LLog
import com.thk.im.android.core.base.LanguageUtils
import com.thk.im.android.core.base.utils.AppUtils
import com.thk.im.android.core.db.IMDataBase
import com.thk.im.android.core.db.internal.DefaultIMDataBase
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.fileloader.FileLoadModule
import com.thk.im.android.core.module.internal.DefaultCommonModule
import com.thk.im.android.core.module.internal.DefaultContactModule
import com.thk.im.android.core.module.internal.DefaultCustomModule
import com.thk.im.android.core.module.internal.DefaultGroupModule
import com.thk.im.android.core.module.internal.DefaultMessageModule
import com.thk.im.android.core.module.internal.DefaultUserModule
import com.thk.im.android.core.signal.SignalListener
import com.thk.im.android.core.signal.SignalModule
import com.thk.im.android.core.signal.SignalType
import com.thk.im.android.core.storage.StorageModule
import com.thk.im.android.core.storage.internal.DefaultStorageModule


@Keep
object IMCoreManager {

    private var debug: Boolean = false
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
    val severTime: Long
        get() = commonModule.getSeverTime()

    val cacheMap = hashMapOf<String, IMCache>()

    var crypto: Crypto? = null

    fun init(app: Application, debug: Boolean = true) {
        this.app = app
        this.debug = debug
        LLog.init("THK-IM", if (debug) LLog.DEBUG else LLog.INFO)
        AppUtils.instance().init(app)
        LanguageUtils.init(app)
        LiveEventBus.config().setContext(IMCoreManager.app)
            .autoClear(true)
            .lifecycleObserverAlwaysActive(true)
    }

    fun initUser(uId: Long) {
        if (uId <= 0) {
            return
        }
        if (this.uId == uId) {
            return
        }

        if (this.uId != 0L) {
            shutdown()
        }
        this.uId = uId
        db = DefaultIMDataBase(app, uId, debug)
        storageModule = DefaultStorageModule(app, uId)
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

    // 默认10分钟过期
    fun setCache(key: String, data: Any, expired: Long = 10 * 3600 * 1000) {
        cacheMap[key] = IMCache(severTime + expired, data)
    }

    inline fun <reified T : Any> getCache(key: String): T? {
        val cache = cacheMap[key] ?: return null
        if (cache.time > severTime) {
            val data = cache.data as? T
            return data
        } else {
            cacheMap.remove(key)
            return null
        }
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