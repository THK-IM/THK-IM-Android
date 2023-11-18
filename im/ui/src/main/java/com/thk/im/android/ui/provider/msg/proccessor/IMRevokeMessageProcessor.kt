package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.processor.BaseMsgProcessor
import com.thk.im.android.ui.manager.IMRevokeMsgData
import io.reactivex.Flowable

open class IMRevokeMessageProcessor : BaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun send(msg: Message, resend: Boolean, callback: IMSendMsgCallback?) {
        if (msg.fUid != IMCoreManager.getUid()) {
            return
        }
        val subscriber = object : BaseSubscriber<Void>() {
            override fun onStart() {
                super.onStart()
                callback?.onStart()
            }
            override fun onNext(t: Void?) {
                callback?.onResult(null)
            }

            override fun onError(t: Throwable?) {
                callback?.onResult(Exception(t))
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        IMCoreManager.imApi.revokeMessage(msg.fUid, msg.sid, msg.msgId)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun received(msg: Message) {
        val subscriber = object : BaseSubscriber<IMRevokeMsgData>() {
            override fun onNext(t: IMRevokeMsgData?) {
                if (t != null) {
                    msg.data = Gson().toJson(t)
                }
                IMCoreManager.getImDataBase().messageDao().insertOrIgnoreMessages(listOf(msg))
                XEventBus.post(IMEvent.MsgNew.value, msg)
                IMCoreManager.getMessageModule().processSessionByMessage(msg)
            }

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }
        }
        getRevokeMsgData(msg)
            .compose(RxTransform.flowableToIo())
            .subscribe(subscriber)
        disposables.add(subscriber)
    }

    open fun getRevokeMsgData(msg: Message): Flowable<IMRevokeMsgData> {
        return getNickname(msg).flatMap {
            val data = IMRevokeMsgData(it)
            if (msg.rMsgId != null) {
                val dbMsg = IMCoreManager.getImDataBase().messageDao()
                    .findMessageByMsgId(msg.rMsgId!!, msg.sid)
                if (dbMsg != null) {
                    IMCoreManager.getImDataBase().messageDao().deleteMessages(listOf(dbMsg))
                    XEventBus.post(IMEvent.MsgDelete.value, dbMsg)
                    if (dbMsg.fUid == IMCoreManager.getUid()) {
                        data.content = dbMsg.content
                        data.data = dbMsg.data
                        data.type = dbMsg.type
                    }
                }
            }
            return@flatMap Flowable.just(data)
        }
    }

    open fun getNickname(msg: Message): Flowable<String> {
        if (msg.fUid == IMCoreManager.getUid()) {
            return Flowable.just("你")
        } else {
            return IMCoreManager.getUserModule().getUserInfo(msg.fUid)
                .flatMap {
                    return@flatMap Flowable.just(it.name)
                }
        }
    }

    open override fun getSessionDesc(msg: Message): String {
        return if (msg.data != null) {
            val revokeData = Gson().fromJson(msg.data!!, IMRevokeMsgData::class.java)
            "${revokeData.nick}撤回了一条消息"
        } else {
            ""
        }
    }
}