package com.thk.android.im.live.bean

import com.google.gson.annotations.SerializedName
import com.thk.android.im.live.room.Member

data class CreateRoomReqBean(
    @SerializedName("uid")
    var uid: String,
    @SerializedName("mode")
    var mode: Int,
)

data class CreateRoomResBean(
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
)