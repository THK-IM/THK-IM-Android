package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName

data class CreateSessionBean(
    @SerializedName("type")
    val type: Int,
    @SerializedName("entity_id")
    val entityId: Long?,
    @SerializedName("members")
    val members: List<Long>,
) {
}