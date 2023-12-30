package com.thk.im.android.api.group.vo

import com.google.gson.annotations.SerializedName

data class JoinGroupVo(
    @SerializedName("group_id")
    val groupId: Long,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("channel")
    val channel: Int,
    @SerializedName("content")
    val content: String
)