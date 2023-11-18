package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "group_member", primaryKeys = ["gid", "uid"])
data class GroupMember(
    @ColumnInfo(name = "gid") val gid: Long,
    @ColumnInfo(name = "uid") val uid: Long,
    @ColumnInfo(name = "role") val role: Int,
    @ColumnInfo(name = "nick") val nick: String?,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "ext_data") val ext_data: String?,   // 扩展字段
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") val mTime: Long,
) : Parcelable {

    @Ignore
    constructor(gid: Long, uid: Long) : this(gid, uid, 0, "", 0, "", 0, 0)

}