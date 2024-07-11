package com.thk.im.android.live.vo

import com.google.gson.annotations.SerializedName

data class DelRoomVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
)