package com.thk.im.android.ui.manager

import com.google.gson.annotations.SerializedName

enum class IMMsgPosType(val value: Int) {
    Mid(0),
    Left(1),
    Right(2)
}

data class IMFile(
    @SerializedName("path")
    var path: String,
    @SerializedName("mime_type")
    var mimeType: String,
)