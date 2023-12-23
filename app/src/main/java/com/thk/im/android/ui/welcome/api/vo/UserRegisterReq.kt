package com.thk.im.android.ui.welcome.api.vo

import com.google.gson.annotations.SerializedName

data class UserRegisterReq(
    @SerializedName("account")
    val account: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("sex")
    val sex: Int? = null,
    @SerializedName("avatar")
    val avatar: String? = null,
    @SerializedName("birthday")
    var birthday: Long? = null
)