package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName

data class ContactorApplyBean(
    @SerializedName("id")
    val id: Long,
    @SerializedName("apply_u_id")
    val applyUId: Long,
    @SerializedName("to_u_id")
    val toUId: Long,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
)