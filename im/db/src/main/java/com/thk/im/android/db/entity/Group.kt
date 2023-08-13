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
@Entity(tableName = "group_", indices = [Index(value = ["sid"], unique = true)])
data class Group(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "sid") val sid: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "avatar") val avatar: String?,
    @ColumnInfo(name = "notice") val notice: String?, //群公告
    @ColumnInfo(name = "brief") val brief: String?, //群简介
    @ColumnInfo(name = "need_review") val needReview: Int,// 0：进群不需审核 1：进群需要审核
    @ColumnInfo(name = "ext_data") val extData: String?,
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") val mTime: Long,
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(id, 0, "", "", "", "", 0, "", 0, 0)

}