package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName


data class CallRoomMemberReqVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("members")
    var members: Set<Long>,
)