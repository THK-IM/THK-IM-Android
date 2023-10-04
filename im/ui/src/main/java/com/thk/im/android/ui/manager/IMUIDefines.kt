package com.thk.im.android.ui.manager

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

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

@Parcelize
open class MediaItem : Parcelable

@Parcelize
data class ImageMediaItem(
    var width: Int,
    var height: Int,
    var thumbnailPath: String?,
    var thumbnailUrl: String?,
    var sourcePath: String?,
    var sourceUrl: String?,
) :MediaItem(),  Parcelable

@Parcelize
data class VideoMediaItem(
    var width: Int,
    var height: Int,
    var duration: Int,
    var coverPath: String?,
    var coverUrl: String?,
    var sourcePath: String?,
    var sourceUrl: String?,
) :MediaItem(),  Parcelable

