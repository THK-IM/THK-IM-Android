package com.thk.im.android.core.api.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.thk.im.android.db.entity.Message
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MessageBean(
    @SerializedName("c_id")
    var clientId: Long = 0,
    @SerializedName("f_uid")
    var fUId: Long = 0,
    @SerializedName("s_id")
    var sessionId: Long = 0,
    @SerializedName("msg_id")
    var msgId: Long = 0,
    @SerializedName("type")
    var type: Int = 0,
    @SerializedName("body")
    var body: String = "",
    @SerializedName("status")
    var status: Int? = null,
    @SerializedName("at_users")
    var atUsers: String? = "",
    @SerializedName("r_msg_id")
    var rMsgId: Long? = 0,
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
        message.cTime = cTime
        status?.let {
            message.oprStatus = it
        }
        return message
    }


    fun extData(): String? {
        return null
    }

    fun <T> transformBody(type: Class<T>): T {
        return Gson().fromJson(body, type)
    }

    companion object {
        fun buildMessageBean(message: Message): MessageBean {
            val messageBean = MessageBean()
            messageBean.clientId = message.id
            messageBean.fUId = message.fUid
            messageBean.sessionId = message.sid
            messageBean.msgId = message.msgId
            messageBean.type = message.type
            messageBean.body = message.content
            messageBean.atUsers = message.atUsers
            messageBean.rMsgId = message.rMsgId
            messageBean.cTime = message.cTime
            return messageBean
        }
    }
}