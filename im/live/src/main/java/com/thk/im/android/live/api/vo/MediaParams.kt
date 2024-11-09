package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName

data class MediaParams(
    @SerializedName("video_max_bitrate")
    val videoMaxBitrate: Int,
    @SerializedName("audio_max_bitrate")
    val audioMaxBitrate: Int,
    @SerializedName("video_width")
    val videoWidth: Int,
    @SerializedName("video_height")
    val videoHeight: Int,
    @SerializedName("video_fps")
    val videoFps: Int
)