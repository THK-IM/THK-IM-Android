package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMEvent
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.event.XEventBus
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMRevokeMsgData

open class IMRevokeMsgProcessor : IMBaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Revoke.value
    }

    override fun send(msg: Message, resend: Boolean, callback: IMSendMsgCallback?) {
        if (msg.fUid != IMCoreManager.uId) {
            return
        }
        val subscriber = object : BaseSubscriber<Void>() {

            override fun onNext(t: Void?) {
                callback?.onResult(msg, null)
            }

            override fun onError(t: Throwable?) {
                callback?.onResult(msg, Exception(t))
                disposables.remove(this)
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
        processRevokeMsg(msg)
        if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.uId) {
            IMCoreManager.messageModule.ackMessageToCache(msg)
        }
    }

    open fun processRevokeMsg(msg: Message) {
        val sender = getSenderName(msg)
        val data = IMRevokeMsgData(sender)
        var existed = false
        if (msg.rMsgId != null) {
            val dbMsg = IMCoreManager.getImDataBase().messageDao()
                .findByMsgId(msg.rMsgId!!, msg.sid)
            if (dbMsg != null) {
                IMCoreManager.getImDataBase().messageDao().delete(listOf(dbMsg))
                XEventBus.post(IMEvent.MsgDelete.value, dbMsg)
                if (dbMsg.fUid == IMCoreManager.uId) {
                    data.content = dbMsg.content
                    data.data = dbMsg.data
                    data.type = dbMsg.type
                }
                existed = true
            }
        }
        if (existed) {
            msg.data = Gson().toJson(data)
            msg.oprStatus = MsgOperateStatus.ClientRead.value or MsgOperateStatus.ServerRead.value
            msg.sendStatus = MsgSendStatus.Success.value
            IMCoreManager.getImDataBase().messageDao().insertOrIgnore(listOf(msg))
            XEventBus.post(IMEvent.MsgNew.value, msg)
            IMCoreManager.messageModule.processSessionByMessage(msg)
        }
    }

    override fun getSenderName(msg: Message): String {
        return if (msg.fUid == IMCoreManager.uId) {
            IMCoreManager.app.getString(R.string.yourself)
        } else {
            super.getSenderName(msg) ?: "xxx"
        }
    }

    override fun needReprocess(msg: Message): Boolean {
        return true
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_revoke_msg)
    }
}