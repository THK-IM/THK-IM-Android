package com.thk.android.im.live.vo

import com.google.gson.annotations.SerializedName
import com.thk.android.im.live.Member

data class JoinRoomReqVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("role")
    var role: Int
)

data class JoinRoomResVo(
    @SerializedName("id")
    var id: String,
    @SerializedName("owner_id")
    var ownerId: String,
    @SerializedName("token")
    var token: String,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("participants")
    var members: MutableList<Member>,
    @SerializedName("mode")
    var mode: Int,
)