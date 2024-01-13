package com.thk.im.android.api.contact.vo

import com.google.gson.annotations.SerializedName

data class ApplyFriendVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("contact_id")
    val contactId: Long,
    @SerializedName("channel")
    val channel: Int?,
    @SerializedName("msg")
    val msg: String?
)