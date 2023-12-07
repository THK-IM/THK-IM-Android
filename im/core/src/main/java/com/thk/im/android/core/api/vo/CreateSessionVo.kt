package com.thk.im.android.core.api.vo

import com.google.gson.annotations.SerializedName

data class CreateSessionVo(
    @SerializedName("u_id")
    var uId: Long,
    @SerializedName("type")
    val type: Int,
    @SerializedName("entity_id")
    val entityId: Long,
    @SerializedName("members")
    val members: Set<Long>?,
    @SerializedName("name")
    val name: String,
    @SerializedName("remark")
    val remark: String,
)