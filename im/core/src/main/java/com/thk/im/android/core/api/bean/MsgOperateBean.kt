package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName

data class AckMsgBean(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_ids")
    val msgIds: Set<Long>,
)

data class ReadMsgBean(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_ids")
    val msgIds: Set<Long>,
)

data class RevokeMsgBean(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_id")
    val msgIds: Long,
)

data class ReeditMsgBean(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_id")
    val msgIds: Long,
    @SerializedName("content")
    val content: String,
)