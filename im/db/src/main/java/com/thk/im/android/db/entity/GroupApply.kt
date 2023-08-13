package com.thk.im.android.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "group_apply")
data class GroupApply(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "apply_u_id") val applyUId: Long,
    @ColumnInfo(name = "gid") val gId: Long,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") val mTime: Long,
) : Parcelable {

}