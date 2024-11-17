package com.thk.im.android.live

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

val liveSignalEvent = "LiveSignalEvent"

interface LiveRequestProcessor {

    /**
     * 收到被呼叫请求
     */
    fun onBeingRequested(signal: BeingRequestedSignal)


    /**
     * 收到取消呼叫请求
     */
    fun onCancelBeingRequested(signal: CancelBeingRequestedSignal)

}

enum class LiveSignalType(val value: Int) {
    // 正在被请求通话
    BeingRequested(1),

    // 取消请求通话
    CancelBeingRequested(2),

    // 拒绝请求通话
    RejectRequest(3),

    // 接受请求通话
    AcceptRequest(4),

    // 挂断电话
    Hangup(5),

    // 结束通话
    EndCall(6),

    // 踢出成员
    KickMember(7),

}

data class BeingRequestedSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("members")
    var members: Set<Long>,
    @SerializedName("request_id")
    var requestId: Long,
    @SerializedName("mode")
    var mode: Int,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("timeout_time")
    var timeoutTime: Long,
)

data class CancelBeingRequestedSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("cancel_time")
    var cancelTime: Long,
)

data class RejectRequestSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("reject_time")
    var rejectTime: Long,
)

data class AcceptRequestSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("accept_time")
    var acceptTime: Long,
)

data class HangupSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("hangup_time")
    var hangupTime: Long,
)

data class EndCallSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("end_call_time")
    var endCallTime: Long,
)

data class KickMemberSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("kick_ids")
    var kickIds: Set<Long>,
    @SerializedName("kick_time")
    var kickTime: Long,
)

data class LiveSignal(
    @SerializedName("type")
    var type: Int,
    @SerializedName("body")
    var body: String,
) {
    fun <T> signalForType(type: Int, clazz: Class<T>): T? {
        if (this.type == type) {
            return try {
                Gson().fromJson(body, clazz)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}

enum class Role(val value: Int) {
    Audience(1), // 观众
    Broadcaster(2), // 主播
}

enum class CallType(val value: Int) {
    RequestCalling(1), // 请求通话
    BeCalling(2), // 被请求通话
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

@Keep
@Parcelize
data class ParticipantVo(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("role")
    var role: Int,
    @SerializedName("join_time")
    var joinTime: Long,
    @SerializedName("stream_key")
    var streamKey: String,
) : Parcelable

const val VolumeMsgType = 0

data class VolumeMsg(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("volume")
    var volume: Double,
)
