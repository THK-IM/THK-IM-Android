package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = "contact",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["session_id"], unique = false)
    ]
)
data class Contact(
    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Long,
    @SerializedName("session_id")
    @ColumnInfo(name = "session_id")
    var sessionId: Long?,
    @SerializedName("note_name")
    @ColumnInfo(name = "note_name")
    var noteName: String?,
    @SerializedName("relation")
    @ColumnInfo(name = "relation")
    var relation: Int,
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

    companion object {
        fun newContact() : Contact {
            return Contact(0L, 0L, "", 0, "", 0, 0)
        }
    }

}