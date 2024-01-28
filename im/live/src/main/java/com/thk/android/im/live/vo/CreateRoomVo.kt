package com.thk.android.im.live.vo

import com.google.gson.annotations.SerializedName
import com.thk.android.im.live.Member

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
    @SerializedName("participants")
    var members: MutableList<Member>,
    @SerializedName("mode")
    var mode: Int,
)