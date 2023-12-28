package com.thk.im.android.core.exception

import com.google.gson.annotations.SerializedName

data class StatusCode(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)