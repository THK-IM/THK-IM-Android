package com.thk.im.android.api.group.vo

import com.google.gson.annotations.SerializedName


data class DeleteGroupVo(
    @SerializedName("group_id")
    val groupId: Long,
    @SerializedName("u_id")
    val uId: Long,
)