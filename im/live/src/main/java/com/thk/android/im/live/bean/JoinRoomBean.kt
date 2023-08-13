package com.thk.android.im.live.bean

import com.google.gson.annotations.SerializedName
import com.thk.android.im.live.room.Member

data class JoinRoomReqBean(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("uid")
    var uid: String,
    @SerializedName("role")
    var role: Int,
    @SerializedName("token")
    var token: String,
)

data class JoinRoomResBean(
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