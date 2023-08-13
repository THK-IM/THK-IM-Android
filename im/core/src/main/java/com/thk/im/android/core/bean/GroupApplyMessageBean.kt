package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName


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