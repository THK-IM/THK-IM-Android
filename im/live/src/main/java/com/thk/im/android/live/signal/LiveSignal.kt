package com.thk.im.android.live.signal

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


enum class LiveSignalType(val value: Int) {
    // 正在被请求通话
    BeingRequested(1),

    // 取消被请求通话
    CancelRequested(2),

    // 拒绝请求通话
    RejectRequest(3),

    // 接受请求通话
    AcceptRequest(4),

    // 挂断电话
    Hangup(5),

    // 结束通话
    EndCall(6),
}

data class BeingRequestedSignal(
    @SerializedName("roomId")
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

data class CancelRequestedSignal(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("cancel_time")
    var cancelTime: Long,
)

data class RejectRequestSignal(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("reject_time")
    var rejectTime: Long,
)

data class AcceptRequestSignal(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("accept_time")
    var acceptTime: Long,
)

data class HangupSignal(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("hangup_time")
    var hangupTime: Long,
)

data class EndCallSignal(
    @SerializedName("roomId")
    var roomId: String,
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("msg")
    var msg: String,
    @SerializedName("end_call_time")
    var endCallTime: Long,
)

data class LiveSignal(
    @SerializedName("type")
    var type: Int,
    @SerializedName("body")
    var body: String,
) {
    fun beingRequestedSignal(): BeingRequestedSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, BeingRequestedSignal::class.java)
        }
        return null
    }

    fun cancelRequestedSignal(): CancelRequestedSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, CancelRequestedSignal::class.java)
        }
        return null
    }

    fun rejectRequestSignal(): RejectRequestSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, RejectRequestSignal::class.java)
        }
        return null
    }

    fun acceptRequestSignal(): AcceptRequestSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, AcceptRequestSignal::class.java)
        }
        return null
    }

    fun hangupSignal(): HangupSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, HangupSignal::class.java)
        }
        return null
    }

    fun endCallSignal(): EndCallSignal? {
        if (this.type == LiveSignalType.BeingRequested.value) {
            return Gson().fromJson(body, EndCallSignal::class.java)
        }
        return null
    }
}