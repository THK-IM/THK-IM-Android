package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName
import com.thk.im.android.live.ParticipantVo

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

data class CreateRoomResVo(
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
)