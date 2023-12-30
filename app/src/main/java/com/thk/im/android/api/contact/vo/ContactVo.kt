package com.thk.im.android.api.contact.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Contact
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactVo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("relation")
    val relation: Int,
    @SerializedName("note_name")
    val noteName: String?,
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
) : Parcelable {

    fun toContact(): Contact {
        return Contact(id, noteName, relation, null, createTime, updateTime)
    }

}