package com.thk.im.android.live.api.vo

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.live.ParticipantVo
import kotlinx.parcelize.Parcelize

data class CreateRoomReqVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("mode")
    var mode: Int,
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

@Keep
@Parcelize
data class RoomResVo(
    @SerializedName("id")
    var id: String,
    @SerializedName("owner_id")
    var ownerId: Long,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("participants")
    var participantVos: MutableList<ParticipantVo>?,
    @SerializedName("mode")
    var mode: Int,
    @SerializedName("media_params")
    val mediaParams: MediaParams,
) : Parcelable