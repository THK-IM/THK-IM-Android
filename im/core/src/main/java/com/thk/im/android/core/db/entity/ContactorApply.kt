package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "contactor_apply")
data class ContactorApply(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "apply_u_id") val applyUId: Long,
    @ColumnInfo(name = "to_u_id") val toUId: Long,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "c_time") val cTime: Long,
    @ColumnInfo(name = "m_time") val mTime: Long,
) : Parcelable