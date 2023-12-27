package com.thk.im.android.api.contact.vo

import com.google.gson.annotations.SerializedName

data class ContactVo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("relation")
    val relation: Int,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("sex")
    val sex: Int,
    @SerializedName("create_time")
    val createTime: Long,
    @SerializedName("update_time")
    val updateTime: Long,
)