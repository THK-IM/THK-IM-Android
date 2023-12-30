package com.thk.im.android.api.group.vo

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.Group
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupVo(
    @SerializedName("id")
    val id: Long,
    @SerializedName("display_id")
    val displayId: String,
    @SerializedName("owner_id")
    val ownerId: Long,
    @SerializedName("session_id")
    val sessionId: Long,
    @SerializedName("qrcode")
    val qrcode: String,
    @SerializedName("member_count")
    val memberCount: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("announce")
    val announce: String,
    @SerializedName("ext_data")
    val extData: String?,
    @SerializedName("enter_flag")
    val enterFlag: Int,
    @SerializedName("create_time")
    val createTime: Long,
    @SerializedName("update_time")
    val updateTime: Long,
) : Parcelable {

    fun toGroup(): Group {
        return Group(
            id, displayId, name, sessionId, ownerId, avatar, announce,
            qrcode, enterFlag, memberCount, extData, createTime, updateTime
        )
    }

}