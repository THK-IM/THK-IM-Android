package com.thk.im.android.api.user.vo

import com.google.gson.annotations.SerializedName

data class RegisterVo(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val userVo: UserVo
)