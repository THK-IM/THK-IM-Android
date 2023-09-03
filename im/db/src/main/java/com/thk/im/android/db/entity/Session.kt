package com.thk.im.android.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "session", indices = [Index(value = ["type", "entity_id"], unique = true)])
data class Session(
    @PrimaryKey @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "entity_id") var entityId: Long,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "remark") var remark: String,
    @ColumnInfo(name = "mute") var mute: Int,
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "role") var role: Int,
    @ColumnInfo(name = "top") var topTime: Long,
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") var mTime: Long,
    @ColumnInfo(name = "un_read") var unRead: Int,
    @ColumnInfo(name = "draft") var draft: String?,
    @ColumnInfo(name = "last_msg") var lastMsg: String?,
    @ColumnInfo(name = "ext_data") var extData: String?,   // 扩展字段
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