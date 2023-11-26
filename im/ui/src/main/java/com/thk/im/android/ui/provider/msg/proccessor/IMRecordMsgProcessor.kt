package com.thk.im.android.ui.provider.msg.proccessor

import com.google.gson.Gson
import com.thk.im.android.core.IMCoreManager
import com.thk.im.android.core.MsgType
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.processor.IMBaseMsgProcessor
import com.thk.im.android.ui.manager.IMRecordMsgBody
import io.reactivex.Flowable

class IMRecordMsgProcessor : IMBaseMsgProcessor() {
    override fun messageType(): Int {
        return MsgType.RECORD.value
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

    override fun getSessionDesc(msg: Message): String {
        return "[会话记录]"
    }
}