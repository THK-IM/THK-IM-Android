package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "session", indices = [Index(value = ["type", "entity_id"], unique = true)])
data class Session(
    @SerializedName("id")
    @PrimaryKey @ColumnInfo(name = "id")
    var id: Long,
    @SerializedName("id")
    @ColumnInfo(name = "type")
    var type: Int,
    @SerializedName("entity_id")
    @ColumnInfo(name = "entity_id")
    var entityId: Long,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String,
    @SerializedName("remark")
    @ColumnInfo(name = "remark")
    var remark: String,
    @SerializedName("mute")
    @ColumnInfo(name = "mute")
    var mute: Int,
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: Int, // 1 静音 2 拒收
    @SerializedName("role")
    @ColumnInfo(name = "role")
    var role: Int,
    @SerializedName("top_timestamp")
    @ColumnInfo(name = "top_timestamp")
    var topTimestamp: Long,
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    val cTime: Long,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long,
    @SerializedName("unread_count")
    @ColumnInfo(name = "unread_count")
    var unReadCount: Int,
    @SerializedName("draft")
    @ColumnInfo(name = "draft")
    var draft: String?,
    @SerializedName("last_msg")
    @ColumnInfo(name = "last_msg")
    var lastMsg: String?,
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data")
    var extData: String?,   // 扩展字段
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(
        id, 0, 0, "", "", 0, 0, 0, 0, 0,
        0, 0, null, null, null
    )

    @Ignore
    constructor() : this(
        0, 0, 0, "", "", 0, 0, 0, 0, 0,
        0, 0, null, null, null
    )

    @Ignore
    constructor(type: Int, entityId: Long) : this(
        0, type, entityId, "", "", 0, 0, 0, 0, 0,
        0, 0, null, null, null
    )

}