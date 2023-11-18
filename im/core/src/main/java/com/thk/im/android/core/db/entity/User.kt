package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
@Entity(tableName = "user")
data class User(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "avatar") var avatar: String?,
    @ColumnInfo(name = "sex") var sex: Int?,
    @ColumnInfo(name = "status") var status: Int?,
    @ColumnInfo(name = "ext_data") var ext_data: String?,   //扩展字段
    @ColumnInfo(name = "c_time") var cTime: Long,
    @ColumnInfo(name = "m_time") var mTime: Long,
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(id, "", null, null, null, null, 0L, 0L)

}