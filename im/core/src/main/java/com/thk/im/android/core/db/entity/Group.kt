package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = "group_",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["session_id"], unique = true)
    ]
)
data class Group(
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Long,
    @SerializedName("display_id")
    @ColumnInfo(name = "display_id")
    val displayId: String,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String,
    @SerializedName("session_id")
    @ColumnInfo(name = "session_id")
    var sessionId: Long,
    @SerializedName("owner_id")
    @ColumnInfo(name = "owner_id")
    var ownerId: Long,
    @SerializedName("avatar")
    @ColumnInfo(name = "avatar")
    var avatar: String,
    @SerializedName("announce")
    @ColumnInfo(name = "announce")
    var announce: String,
    @SerializedName("qrcode")
    @ColumnInfo(name = "qrcode")
    var qrcode: String,
    @SerializedName("enter_flag")
    @ColumnInfo(name = "enter_flag")
    var enterFlag: Int,
    @SerializedName("member_count")
    @ColumnInfo(name = "member_count")
    var memberCount: Int,
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data")
    var extData: String?,   //扩展字段
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    var cTime: Long,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long,
) : Parcelable