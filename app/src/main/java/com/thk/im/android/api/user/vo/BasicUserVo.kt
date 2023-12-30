package com.thk.im.android.api.user.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// 基本用户信息
@Parcelize
data class BasicUserVo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("display_id")
    var displayId: String,
    @SerializedName("avatar")
    var avatar: String?,
    @SerializedName("nickname")
    var nickname: String?,
    @SerializedName("sex")
    var sex: Int,
): Parcelable