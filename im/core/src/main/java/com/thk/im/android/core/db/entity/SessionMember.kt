package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = "session_member",
    primaryKeys = ["session_id", "user_id"],
)
data class SessionMember(
    @SerializedName("session_id")
    @ColumnInfo(name = "session_id")
    var sessionId: Long,
    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    var userId: Long,
    @SerializedName("role")
    @ColumnInfo(name = "role")
    var role: Int,
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: Int,
    @SerializedName("mute")
    @ColumnInfo(name = "mute")
    var mute: Int,
    @SerializedName("note_name")
    @ColumnInfo(name = "note_name")
    var noteName: String?,
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data", typeAffinity = ColumnInfo.TEXT)
    var extData: String?,   //扩展字段
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    var cTime: Long,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long,
    @ColumnInfo(name = "deleted")
    @SerializedName("deleted")
    var deleted: Int,
) : Parcelable