package com.thk.im.android.api.user.vo

import com.google.gson.annotations.SerializedName

data class UserRegisterResp(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: User
)