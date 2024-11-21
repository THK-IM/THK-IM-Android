package com.thk.im.android.module.msg.call

import com.google.gson.annotations.SerializedName


data class IMCallMsg(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("room_owner_id")
    val roomOwnerId: Long,
    @SerializedName("room_mode")
    var roomMode: Int,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("accepted")
    var accepted: Int, // 是否接听 0未接听 1被挂断 2已接通
    @SerializedName("accept_time")
    var acceptTime: Long, // 接听时间
    @SerializedName("duration")
    var duration: Long, // 通话时长
)