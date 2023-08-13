package com.thk.im.android.core.processor.body

import com.google.gson.annotations.SerializedName

data class ImageBody(
    @SerializedName("url")
    var url: String?,
    @SerializedName("path")
    var path: String?,
    @SerializedName("width")
    var width: Int,
    @SerializedName("height")
    var height: Int,
) {


    data class ExtData(
        @SerializedName("state")
        val state: Int,
        @SerializedName("progress")
        val progress: Int,
    )
}