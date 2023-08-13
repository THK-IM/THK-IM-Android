package com.thk.im.android.db.entity

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
    @PrimaryKey @ColumnInfo(name = "id") var id: Long,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "entity_id") var entityId: Long,     // session对应的id=>根据type,用户或者公众号或者群id
    @ColumnInfo(name = "draft") var draft: String?,
    @ColumnInfo(name = "last_msg") var lastMsg: String?,
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "top") var topTime: Long,
    @ColumnInfo(name = "un_read") var unRead: Int,
    @ColumnInfo(name = "ext_data") var ext_data: String?,   // 扩展字段
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") var mTime: Long,
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(
        id, 0, 0L, null,
        null, 0, 0L, 0, null, 0L, 0L
    )

    @Ignore
    constructor() : this(
        0L, 0, 0L, null,
        null, 0, 0L, 0, null, 0L, 0L
    )

    @Ignore
    constructor(type: Int, entityId: Long) : this(
        0L, type, entityId, null,
        null, 0, 0L, 0, null, 0L, 0L
    )

    @Ignore
    constructor(
        id: Long,
        type: Int,
        entityId: Long,
        status: Int,
        top: Long,
        cTime: Long,
        mTime: Long,
        extData: String,
        lastMsg: String?
    ) : this(
        id, type,
        entityId, null, lastMsg, status, top, 0, extData, cTime, mTime
    )
}