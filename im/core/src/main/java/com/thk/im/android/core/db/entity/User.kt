package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
@Entity(
    tableName = "user",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["display_id"], unique = false)
    ]
)
data class User(
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Long,
    @SerializedName("display_id")
    @ColumnInfo(name = "display_id")
    val displayId: String,
    @SerializedName("nickname")
    @ColumnInfo(name = "nickname")
    var nickname: String,
    @SerializedName("avatar")
    @ColumnInfo(name = "avatar")
    var avatar: String?,
    @SerializedName("sex")
    @ColumnInfo(name = "sex")
    var sex: Int?,
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: Int?,
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data", typeAffinity = ColumnInfo.TEXT)
    var extData: String?,   //扩展字段
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    var cTime: Long,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long,
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(id, "", "", null, null, null, null, 0L, 0L)

}