package com.thk.im.android.api.user.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("id")
    val id: Long,
    @SerializedName("display_id")
    var displayId: String,
    @SerializedName("avatar")
    var avatar: String?,
    @SerializedName("nickname")
    var nickname: String?,
    @SerializedName("qrcode")
    var qrcode: String?,
    @SerializedName("sex")
    var sex: Int,
    @SerializedName("birthday")
    var birthday: Long,
) : Parcelable