package com.thk.im.android.live.api.vo

import com.google.gson.annotations.SerializedName

data class KickoffMemberReqVo(
    @SerializedName("room_id")
    var roomId: String,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("kickoff_u_ids")
    val kickoffUIds: Set<Long>
)