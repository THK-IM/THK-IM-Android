package com.thk.im.android.core.api.bean

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

data class GroupApplyMessageBean(
    @SerializedName("id")
    val id: Long,
    @SerializedName("apply_id")
    val applyId: Long,
    @SerializedName("f_u_id")
    val FUid: Long,
    @SerializedName("message")
    val message: String,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
)