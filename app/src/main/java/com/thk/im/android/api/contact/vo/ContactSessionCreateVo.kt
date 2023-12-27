package com.thk.im.android.api.contact.vo

import com.google.gson.annotations.SerializedName

data class ContactSessionCreateVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("contact_id")
    val contactId: Long
)