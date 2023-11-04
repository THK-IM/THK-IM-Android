package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName

data class DeleteMsgBean(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_ids")
    val msgIds: Set<Long>,
)