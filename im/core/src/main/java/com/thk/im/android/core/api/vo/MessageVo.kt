package com.thk.im.android.core.api.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Message
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageVo(
    @SerializedName("c_id")
    var clientId: Long = 0,
    @SerializedName("f_u_id")
    var fUId: Long = 0,
    @SerializedName("s_id")
    var sessionId: Long = 0,
    @SerializedName("msg_id")
    var msgId: Long = 0,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("ext_data")
    var extData: String? = null,
    @SerializedName("status")
    var status: Int? = null,
    @SerializedName("at_users")
    var atUsers: String? = null,
    @SerializedName("r_msg_id")
    var rMsgId: Long? = null,
    @SerializedName("c_time")
    var cTime: Long = 0,
) : Parcelable {

    fun toMessage(): Message {
        val message = Message()
        message.id = clientId
        message.fUid = fUId
        message.sid = sessionId
        message.msgId = msgId
        message.type = type
        message.content = body
        message.atUsers = atUsers
        message.rMsgId = rMsgId
        message.extData = extData
        message.cTime = cTime
        message.mTime = cTime
        status?.let {
            message.oprStatus = it
        }
        return message
    }

    companion object {
        fun buildMessageVo(message: Message): MessageVo {
            val messageVo = MessageVo()
            messageVo.clientId = message.id
            messageVo.fUId = message.fUid
            messageVo.sessionId = message.sid
            messageVo.msgId = message.msgId
            messageVo.type = message.type
            messageVo.body = message.content
            messageVo.extData = message.extData
            messageVo.atUsers = message.atUsers
            messageVo.rMsgId = message.rMsgId
            messageVo.cTime = message.cTime
            return messageVo
        }
    }
}
