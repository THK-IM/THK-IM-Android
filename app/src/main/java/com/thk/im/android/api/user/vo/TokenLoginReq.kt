package com.thk.im.android.api.user.vo

import com.google.gson.annotations.SerializedName

data class TokenLoginReq(
    @SerializedName("token")
    val token: String? = null,
)