package com.thk.im.android.core.api.vo

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.db.entity.SessionMember
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SessionMemberVo(
    @SerializedName("s_id")
    var sId: Long = 0,
    @SerializedName("u_id")
    var uId: Long = 0,
    @SerializedName("mute")
    var mute: Int = 0,
    @SerializedName("role")
    var role: Int = 0,
    @SerializedName("note_name")
    var noteName: String? = null,
    @SerializedName("note_name")
    var noteAvatar: String? = null,
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("deleted")
    var deleted: Int = 0,
    @SerializedName("c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    var mTime: Long = 0,
) : Parcelable {
    fun toSessionMember(): SessionMember {
        return SessionMember(
            sId,
            uId,
            role,
            status,
            mute,
            noteName,
            noteAvatar,
            null,
            cTime,
            mTime,
            deleted
        )
    }

}