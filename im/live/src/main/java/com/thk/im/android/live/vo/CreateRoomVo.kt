package com.thk.im.android.live.vo

import com.google.gson.annotations.SerializedName
import com.thk.im.android.live.ParticipantVo

data class CreateRoomReqVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("mode")
    var mode: Int,
    @SerializedName("members")
    var members: Set<Long>,
)

data class CreateRoomResVo(
    @SerializedName("id")
    var id: String,
    @SerializedName("owner_id")
    var ownerId: Long,
    @SerializedName("create_time")
    var createTime: Long,
    @SerializedName("members")
    var members: Set<Long>,
    @SerializedName("participants")
    var participantVos: MutableList<ParticipantVo>?,
    @SerializedName("mode")
    var mode: Int,
)