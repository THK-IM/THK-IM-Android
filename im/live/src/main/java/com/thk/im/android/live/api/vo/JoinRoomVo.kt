package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName
import com.thk.im.android.live.ParticipantVo

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
    var ownerId: Long,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("participants")
    var participants: MutableList<ParticipantVo>?,
    @SerializedName("mode")
    var mode: Int,
)