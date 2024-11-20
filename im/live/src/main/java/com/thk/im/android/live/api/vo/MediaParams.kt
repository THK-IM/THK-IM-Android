package com.thk.im.android.live.api.vo

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
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
) : Parcelable

enum class Media(val value: MediaParams) {
    R169_H90(MediaParams(90_000, 48_000, 160, 90, 15)),
    R169_H180(MediaParams(160_000, 48_000, 320, 180, 15)),
    R169_H216(MediaParams(180_000, 48_000, 384, 216, 15)),
    R169_H360(MediaParams(450_000, 48_000, 640, 360, 30)),
    R169_H540(MediaParams(800_000, 48_000, 960, 540, 30)),
    R169_H720(MediaParams(1_700_000, 48_000, 1280, 720, 30)),
    R169_H1080(MediaParams(3_000_000, 48_000, 1920, 1080, 30)),
    R169_H1440(MediaParams(5_000_000, 48_000, 2560, 1440, 30)),
    R169_H2160(MediaParams(8_000_000, 48_000, 3840, 2160, 30)),
    H43_H120(MediaParams(70_000, 48_000, 160, 120, 15)),
    H43_H180(MediaParams(125_000, 48_000, 240, 180, 15)),
    H43_H240(MediaParams(140_000, 48_000, 320, 240, 15)),
    H43_H360(MediaParams(330_000, 48_000, 480, 360, 30)),
    H43_H480(MediaParams(500_000, 48_000, 640, 480, 30)),
    H43_H540(MediaParams(600_000, 48_000, 720, 540, 30)),
    H43_H720(MediaParams(1_300_000, 48_000, 960, 720, 30)),
    H43_H1080(MediaParams(2_300_000, 48_000, 1440, 1080, 30)),
    H43_H1440(MediaParams(3_800_000, 48_000, 1920, 1440, 30)),
}