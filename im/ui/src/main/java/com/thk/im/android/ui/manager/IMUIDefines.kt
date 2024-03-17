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
) : MediaItem(), Parcelable

/**
功能，1基础功能 2语音 4 表情  8 图片 16视频  32转发 64已读
 */
enum class IMChatFunction(val value: Long) {
    BaseInput(1), // 文本输入/删除/文本表情
    Audio(2),
    Emoji(4),      //
    Image(8),
    Video(16),
    Forward(32),
    Read(64)
}

