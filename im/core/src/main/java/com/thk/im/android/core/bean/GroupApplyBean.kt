package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName

data class GroupApplyBean(
    @SerializedName("id")
    val id: Long,
    @SerializedName("apply_u_id")
    val applyUId: Long,
    @SerializedName("gid")
    val gid: Long,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
)