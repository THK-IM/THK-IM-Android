package com.thk.im.android.live

import com.google.gson.annotations.SerializedName


enum class Role(val value: Int) {
    Audience(1), // 观众
    Broadcaster(2), // 主播
}

enum class Mode(val value: Int) {
    Chat(1), // 文字直播间
    Audio(2), // 语音电话
    Video(3), // 视频电话
    VoiceRoom(4), // 语音直播间
    VideoRoom(5), // 视频直播间
}

enum class NotifyType(val value: String) {
    NewStream("NewStream"),
    RemoveStream("RemoveStream"),
    DataChannelMsg("DataChannelMsg"),
}


data class NotifyBean(
    @SerializedName("type")
    var type: String,
    @SerializedName("message")
    var message: String,
)

data class NewStreamNotify(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("stream_key")
    var streamKey: String,
    @SerializedName("role")
    var role: Int
)

data class RemoveStreamNotify(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("stream_key")
    var streamKey: String,
)

data class DataChannelMsg(
    @SerializedName("type")
    var type: Int,
    @SerializedName("text")
    var text: String,
)

data class ParticipantVo(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("role")
    var role: Int,
    @SerializedName("join_time")
    var joinTime: Long,
    @SerializedName("stream_key")
    var streamKey: String,
)

const val VolumeMsgType = 0

data class VolumeMsg(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("volume")
    var volume: Double,
)
