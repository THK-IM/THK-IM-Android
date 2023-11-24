package com.thk.im.android.core.api.bean

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Message
import kotlinx.parcelize.Parcelize

@Parcelize
data class ForwardMessageBean(
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
    @SerializedName("status")
    var status: Int? = null,
    @SerializedName("at_users")
    var atUsers: String? = null,
    @SerializedName("r_msg_id")
    var rMsgId: Long? = null,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("fwd_s_id")
    var forwardSid: Long = 0,
    @SerializedName("fwd_from_u_ids")
    var forwardFromUIds: Set<Long>? = null,
    @SerializedName("fwd_client_ids")
    var forwardClientIds: Set<Long>? = null,
) : Parcelable {
    companion object {
        fun buildMessageBean(
            message: Message,
            forwardSid: Long,
            forwardFromUIds: Set<Long>,
            forwardClientIds: Set<Long>
        ): ForwardMessageBean {
            val messageBean = ForwardMessageBean()
            messageBean.clientId = message.id
            messageBean.fUId = message.fUid
            messageBean.sessionId = message.sid
            messageBean.msgId = message.msgId
            messageBean.type = message.type
            messageBean.body = message.content
            messageBean.atUsers = message.atUsers
            messageBean.rMsgId = message.rMsgId
            messageBean.cTime = message.cTime
            messageBean.forwardSid = forwardSid
            messageBean.forwardFromUIds = forwardFromUIds
            messageBean.forwardClientIds = forwardClientIds
            return messageBean
        }
    }
}