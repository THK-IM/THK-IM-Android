package com.thk.im.android.core.api.vo

import com.google.gson.annotations.SerializedName

class CodeMessage(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)