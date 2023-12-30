package com.thk.im.android.api.contact.vo

import com.google.gson.annotations.SerializedName

data class ReviewFriendApplyVo(
    @SerializedName("u_id")
    val uId: Long,
    @SerializedName("apply_id")
    val applyId: Long,
    @SerializedName("pass")
    val pass: Int, // 是否通过 1 待审核 2通过 3驳回
    @SerializedName("msg")
    val msg: String
)