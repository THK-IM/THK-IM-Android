package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName

data class AckMsgBean(
    @SerializedName("session_id")
    val sid: Long,
    @SerializedName("uid")
    val uid: Long,
    @SerializedName("msg_ids")
    val msgIds: List<Long>,
) {
}