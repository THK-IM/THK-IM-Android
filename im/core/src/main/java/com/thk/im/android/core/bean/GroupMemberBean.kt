package com.thk.im.android.core.bean

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.db.entity.GroupMember
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GroupMemberBean(
    @SerializedName("gid")
    val gid: Long,
    @SerializedName("uid")
    val uid: Long,
    @SerializedName("nick")
    val nick: String,
    @SerializedName("role")
    val role: Int,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("c_time")
    val cTime: Long,
    @SerializedName("m_time")
    val mTime: Long,
    @SerializedName("ext_data")
    val extData: String? = "",
    @SerializedName("status")
    val status: Int = 0
) : Parcelable {
    fun toGroupMember(): GroupMember {
        return GroupMember(gid, uid, role, nick, status, extData, cTime, mTime)
    }
}