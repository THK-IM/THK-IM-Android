package com.thk.im.android.core.exception

import com.google.gson.annotations.SerializedName
import java.io.IOException

class CodeMsgException(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String
) : IOException()