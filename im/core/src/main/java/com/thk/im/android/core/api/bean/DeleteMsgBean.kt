package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName

data class DeleteMsgBean(
    @SerializedName("session_id")
    val sid: Long,
    @SerializedName("uid")
    val uid: Long,
    @SerializedName("msg_ids")
    val msgIds: Set<Long>,
)