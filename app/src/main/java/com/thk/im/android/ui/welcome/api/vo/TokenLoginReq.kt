package com.thk.im.android.ui.welcome.api.vo

import com.google.gson.annotations.SerializedName

data class TokenLoginReq(
    @SerializedName("token")
    val token: String? = null,
)