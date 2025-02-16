package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName

data class RefuseJoinRoomVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("msg")
    val msg: String
)