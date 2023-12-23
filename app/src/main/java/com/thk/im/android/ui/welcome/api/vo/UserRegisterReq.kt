package com.thk.im.android.ui.welcome.api.vo

import com.google.gson.annotations.SerializedName

data class UserRegisterReq(
    @SerializedName("account")
    val account: String?,
    @SerializedName("password")
    val password: String?,
    @SerializedName("nickname")
    val nickname: String?,
    @SerializedName("sex")
    val sex: Int?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("birthday")
    var birthday: Long?
)