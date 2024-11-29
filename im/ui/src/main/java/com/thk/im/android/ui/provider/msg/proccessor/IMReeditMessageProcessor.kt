package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.IMSendMsgCallback
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.base.BaseSubscriber
import com.thk.im.android.core.base.RxTransform
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMReeditMsgData
import io.reactivex.Flowable

open class IMReeditMessageProcessor : IMBaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Reedit.value
    }

    override fun needReprocess(msg: Message): Boolean {
        return true
    }

    override fun send(msg: Message, resend: Boolean, callback: IMSendMsgCallback?) {
        if (msg.content == null) {
            return
        }
        val reeditMsgData = Gson().fromJson(msg.content!!, IMReeditMsgData::class.java) ?: return
        val subscriber = object : BaseSubscriber<Boolean>() {

            override fun onComplete() {
                super.onComplete()
                disposables.remove(this)
            }

            override fun onError(t: Throwable?) {
                super.onError(t)
                disposables.remove(this)
                t?.let {
                    callback?.onResult(msg, t)
                }
            }

            override fun onNext(t: Boolean?) {
                t?.let {
                    if (it) {
                        callback?.onResult(msg, null)
                    }
                }
            }
        }
        IMCoreManager.imApi.reeditMessage(
            msg.fUid,
            reeditMsgData.sessionId,
            reeditMsgData.originId,
            msg.content!!
        ).flatMap {
            val success = updateOriginMsg(reeditMsgData)
            return@flatMap Flowable.just(success)
        }.compose(RxTransform.flowableToMain()).subscribe(subscriber)
        disposables.add(subscriber)
    }

    override fun received(msg: Message) {
        if (msg.content == null) {
            return
        }
        val reeditMsgData = Gson().fromJson(msg.content!!, IMReeditMsgData::class.java) ?: return
        val success = updateOriginMsg(reeditMsgData)
        if (success) {
            if (msg.oprStatus.and(MsgOperateStatus.Ack.value) == 0 && msg.fUid != IMCoreManager.uId) {
                IMCoreManager.messageModule.ackMessageToCache(msg)
            }
        }
    }

    private fun updateOriginMsg(reeditMsgData: IMReeditMsgData): Boolean {
        val originMsg = IMCoreManager.db.messageDao().findByMsgId(
            reeditMsgData.originId, reeditMsgData.sessionId
        ) ?: return false
        originMsg.content = reeditMsgData.edit
        originMsg.oprStatus = originMsg.oprStatus.or(MsgOperateStatus.Update.value)
        insertOrUpdateDb(originMsg)
        return true
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_reedit_msg)
    }

}