package com.thk.im.android.api.user.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserBasicInfoVo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("display_id")
    var displayId: String,
    @SerializedName("nickname")
    var nickname: String?,
    @SerializedName("avatar")
    var avatar: String?,
    @SerializedName("sex")
    var sex: Int?
) : Parcelable {

    fun toUser(): User {
        val now = System.currentTimeMillis()
        return User(id, displayId, nickname?:"", avatar, sex, null, null, now, now)
    }

}