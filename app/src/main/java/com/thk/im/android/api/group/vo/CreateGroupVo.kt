package com.thk.im.android.api.group.vo

import com.google.gson.annotations.SerializedName

data class CreateGroupVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("members")
    val members: List<Long>,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("group_announce")
    val groupAnnounce: String,
    @SerializedName("group_type")
    val groupType: Int,  // 2普通群，3 超级群
)