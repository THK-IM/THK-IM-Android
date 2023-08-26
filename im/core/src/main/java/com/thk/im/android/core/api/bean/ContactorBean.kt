package com.thk.im.android.core.api.bean

import com.google.gson.annotations.SerializedName
import com.thk.im.android.db.entity.Contactor

data class ContactorBean(
    @SerializedName("id")
    val id: Long,
    @SerializedName("sid")
    val sid: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("friend")
    val friend: Int,
    @SerializedName("black")
    val black: Int,
    @SerializedName("ext_data")
    val extData: String,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
) {
    fun toContact(): Contactor {
        return Contactor(id, sid, name, friend, black, extData, cTime, mTime)
    }
}