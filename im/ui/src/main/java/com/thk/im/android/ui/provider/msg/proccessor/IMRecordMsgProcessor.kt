package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.R
import com.thk.im.android.ui.manager.IMRecordMsgBody
import io.reactivex.Flowable

class IMRecordMsgProcessor : IMBaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.Record.value
    }

    override fun received(msg: Message) {
        super.received(msg)
        if (msg.content != null) {
            val recordBody = Gson().fromJson(msg.content, IMRecordMsgBody::class.java)
            for (m in recordBody.messages) {
                m.oprStatus = MsgOperateStatus.Ack.value.or(MsgOperateStatus.ClientRead.value)
                    .or(MsgOperateStatus.ServerRead.value)
                m.sendStatus = MsgSendStatus.Success.value
            }
            IMCoreManager.getImDataBase().messageDao().insertOrIgnore(recordBody.messages)
        }
    }

    override fun reprocessingFlowable(message: Message): Flowable<Message>? {
        if (message.content != null) {
            val recordBody = Gson().fromJson(message.content, IMRecordMsgBody::class.java)
            IMCoreManager.getImDataBase().messageDao().insertOrIgnore(recordBody.messages)
        }
        return Flowable.just(message)
    }

    override fun sendToServer(message: Message): Flowable<Message> {
        if (message.content == null) {
            return super.sendToServer(message)
        }
        val recordBody = Gson().fromJson(message.content, IMRecordMsgBody::class.java)

        var recordSessionId: Long? = null
        val recordFromUIds = mutableSetOf<Long>()
        val recordClientIds = mutableSetOf<Long>()
        for (subMessage in recordBody.messages) {
            recordSessionId = subMessage.sid
            recordFromUIds.add(subMessage.fUid)
            recordClientIds.add(subMessage.id)
        }
        if (recordSessionId == null || recordFromUIds.isEmpty() || recordClientIds.isEmpty()) {
            return super.sendToServer(message)
        }

        return IMCoreManager.imApi.forwardMessages(
            message,
            recordSessionId,
            recordFromUIds,
            recordClientIds
        )
    }

    override fun needReprocess(msg: Message): Boolean {
        return true
    }

    override fun msgDesc(msg: Message): String {
        return IMCoreManager.app.getString(R.string.im_record_msg)
    }
}