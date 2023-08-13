package com.thk.im.android.core.processor.body

import com.google.gson.annotations.SerializedName

data class VoiceBody(
    @SerializedName("url")
    var url: String?,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("path")
    var path: String?,
) {

    data class ExtData(
        @SerializedName("state")
        val state: Int,
        @SerializedName("progress")
        val progress: Int,
    )
}