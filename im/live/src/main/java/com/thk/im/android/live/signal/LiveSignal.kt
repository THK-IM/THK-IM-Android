package com.thk.im.android.live.signal

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


enum class LiveSignalType(val value: Int) {
    // 正在被请求通话
    BeingRequesting(1),

    // 取消请求通话
    CancelRequesting(2),

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

data class BeingRequestingSignal(
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

data class CancelRequestingSignal(
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
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("reject_time")
    var rejectTime: Long,
)

data class AcceptRequestSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("accept_time")
    var acceptTime: Long,
)

data class HangupSignal(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
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