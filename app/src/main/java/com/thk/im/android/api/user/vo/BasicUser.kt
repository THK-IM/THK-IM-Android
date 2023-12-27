package com.thk.im.android.api.user.vo

import com.google.gson.annotations.SerializedName

// 基本用户信息
data class BasicUserInfo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("display_id")
    var displayId: String,
    @SerializedName("avatar")
    var avatar: String?,
    @SerializedName("nickname")
    var nickname: String?,
    @SerializedName("sex")
    var sex: Int,
)