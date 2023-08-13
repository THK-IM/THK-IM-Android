package com.thk.im.android.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "contactor", indices = [Index(value = ["sid"], unique = true)])
data class Contactor(
    @PrimaryKey
    @ColumnInfo(name = "uid") val uid: Long,
    @ColumnInfo(name = "sid") val sid: Long,
    @ColumnInfo(name = "nick") val nick: String?,
    @ColumnInfo(name = "friend") val friend: Int?,
    @ColumnInfo(name = "black") val black: Int?,
    @ColumnInfo(name = "ext_data") val ext_data: String?,   //扩展字段
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") val mTime: Long,
) : Parcelable {

}