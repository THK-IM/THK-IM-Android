package com.thk.im.android.core.fileloader.internal

import com.google.gson.annotations.SerializedName


data class UploadParams(
    @SerializedName("id")
    val id: Long,
    @SerializedName("url")
    val url: String,
    @SerializedName("method")
    val method: String,
    @SerializedName("params")
    val params: Map<String, String>
)