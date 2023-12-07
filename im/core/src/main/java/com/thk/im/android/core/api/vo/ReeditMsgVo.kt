package com.thk.im.android.core.api.vo

import com.google.gson.annotations.SerializedName


data class ReeditMsgVo(
    @SerializedName("s_id")
    val sid: Long,
    @SerializedName("u_id")
    val uid: Long,
    @SerializedName("msg_id")
    val msgIds: Long,
    @SerializedName("content")
    val content: String?,
)