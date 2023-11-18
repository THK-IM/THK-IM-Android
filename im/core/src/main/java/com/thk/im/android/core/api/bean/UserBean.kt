package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.User

data class UserBean(
    @SerializedName("id")
    var id: Long = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("avatar")
    var avatar: String? = "",
    @SerializedName("sex")
    var sex: Int? = 0,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    var mTime: Long = 0,
    @SerializedName("ext_data")
    var extData: String? = ""
) {
    constructor(user: User) : this(
        user.id,
        user.name,
        user.avatar,
        user.sex,
        user.cTime,
        user.mTime,
        user.ext_data
    )

    fun toUser(): User {
        return User(
            id, name, avatar, sex, 0, extData, cTime, mTime
        )
    }
}