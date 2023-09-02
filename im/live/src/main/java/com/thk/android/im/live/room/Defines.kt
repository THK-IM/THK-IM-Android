package com.thk.android.im.live.room

import com.google.gson.annotations.SerializedName
import java.nio.ByteBuffer


enum class Role(val value: Int) {
    Audience(1), // 观众
    Broadcaster(2), // 主播
}

enum class Mode(val value: Int) {
    Chat(1), // 文字直播间
    Audio(2), // 视频直播间
    Video(3), // 视频直播间
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
    @SerializedName("uid")
    var uid: String,
    @SerializedName("stream_key")
    var streamKey: String,
    @SerializedName("role")
    var role: Int
)

data class RemoveStreamNotify(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("uid")
    var uid: String,
    @SerializedName("stream_key")
    var streamKey: String,
)

data class DataChannelMsg(
    @SerializedName("uid")
    var uid: String,
    @SerializedName("text")
    var text: String,
)

data class Member(
    @SerializedName("uid")
    var uid: String,
    @SerializedName("role")
    var role: Int,
    @SerializedName("join_time")
    var joinTime: Long,
    @SerializedName("stream_key")
    var streamKey: String,
)

interface RoomObserver {
    fun join(p: BaseParticipant)

    fun leave(p: BaseParticipant)

    fun onTextMsgReceived(uid: String, text: String)

    fun onBufferMsgReceived(bb: ByteBuffer)
}
