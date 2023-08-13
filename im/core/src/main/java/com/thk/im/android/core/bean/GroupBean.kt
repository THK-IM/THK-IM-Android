package com.thk.im.android.core.bean

import com.google.gson.annotations.SerializedName
import com.thk.im.android.db.entity.Group

data class GroupBean(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("sid")
    val sid: Long,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
    @SerializedName("notice")
    val notice: String? = "",
    @SerializedName("brief")
    val brief: String? = "",
    @SerializedName("ext_data")
    val extData: String? = ""
) {
    fun toGroup(): Group {
        return Group(id, sid, name, avatar, notice, brief, 0, extData, cTime, mTime)
    }
}