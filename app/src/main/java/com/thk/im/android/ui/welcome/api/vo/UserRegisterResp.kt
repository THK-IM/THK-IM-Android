package com.thk.im.android.ui.welcome.api.vo

import com.google.gson.annotations.SerializedName

data class UserRegisterResp(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: User?
)