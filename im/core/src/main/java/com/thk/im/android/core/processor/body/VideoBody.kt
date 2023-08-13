package com.thk.im.android.core.processor.body

import com.google.gson.annotations.SerializedName

data class VideoBody(
    @SerializedName("url")
    var url: String?,
    @SerializedName("path")
    var path: String?,
    @SerializedName("thumbnail_path")
    var thumbnailPath: String?,
    @SerializedName("duration")
    var duration: Long?,
    @SerializedName("ratio")
    var ratio: Int?,
    @SerializedName("width")
    var width: Int?,
    @SerializedName("height")
    var height: Int?,
) {

    data class ExtData(
        @SerializedName("state")
        val state: Int,
        @SerializedName("progress")
        val progress: Int,
    )

    constructor(url: String?) : this(url, null, null, null, null, null, null)
}