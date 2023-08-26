package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName


data class ContactorApplyMessageBean(
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