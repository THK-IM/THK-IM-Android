package com.thk.im.android.api.group.vo

import com.google.gson.annotations.SerializedName

data class UpdateGroupVo(
    @SerializedName("group_id")
    val groupId: Long,
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("name")
    val name: String?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("announce")
    val announce: String?,
    @SerializedName("ext_data")
    val extData: String?,
    @SerializedName("enter_flag")
    val enterFlag: Int?,
)