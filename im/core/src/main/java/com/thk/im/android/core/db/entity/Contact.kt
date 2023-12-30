package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "contact")
data class Contact(
    @SerializedName("id")
    @PrimaryKey @ColumnInfo(name = "id")
    val id: Long,
    @SerializedName("note_name")
    @ColumnInfo(name = "note_name")
    var name: String,
    @SerializedName("relation")
    @ColumnInfo(name = "relation")
    var relation: Int,
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