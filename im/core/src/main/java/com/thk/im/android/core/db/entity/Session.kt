package com.thk.im.android.core.db.entity

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
@Entity(
    tableName = "session", indices = [
        Index(value = ["type", "entity_id"], unique = false),
        Index(value = ["parent_id", "m_time"], unique = false)
    ]
)
data class Session(
    @SerializedName("id")
    @PrimaryKey @ColumnInfo(name = "id")
    var id: Long,
    @SerializedName("parent_id")
    @ColumnInfo(name = "parent_id")
    var parentId: Long,
    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: Int,
    @SerializedName("entity_id")
    @ColumnInfo(name = "entity_id")
    var entityId: Long,
    @SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String,
    @SerializedName("note_name")
    @ColumnInfo(name = "note_name")
    var noteName: String?,
    @ColumnInfo(name = "note_avatar")
    var noteAvatar: String?,
    @SerializedName("remark")
    @ColumnInfo(name = "remark")
    var remark: String,
    @SerializedName("mute")
    @ColumnInfo(name = "mute")
    var mute: Int,
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: Int, // 1 静音 2 拒收
    @SerializedName("role")
    @ColumnInfo(name = "role")
    var role: Int,
    @SerializedName("top_timestamp")
    @ColumnInfo(name = "top_timestamp")
    var topTimestamp: Long,
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data", typeAffinity = ColumnInfo.TEXT)
    var extData: String?,   // 扩展字段
    @SerializedName("unread_count")
    @ColumnInfo(name = "unread_count")
    var unReadCount: Int,
    @SerializedName("draft")
    @ColumnInfo(name = "draft")
    var draft: String?,
    @SerializedName("last_msg")
    @ColumnInfo(name = "last_msg", typeAffinity = ColumnInfo.TEXT)
    var lastMsg: String?,
    @SerializedName("msg_sync_time")
    @ColumnInfo(name = "msg_sync_time")
    var msgSyncTime: Long = 0,
    @SerializedName("member_sync_time")
    @ColumnInfo(name = "member_sync_time")
    var memberSyncTime: Long = 0,
    @SerializedName("member_count")
    @ColumnInfo(name = "member_count")
    var memberCount: Int,
    @SerializedName("function_flag")
    @ColumnInfo(name = "function_flag")
    var functionFlag: Long,
    @SerializedName("deleted")
    @ColumnInfo(name = "deleted")
    var deleted: Int = 0,
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    val cTime: Long,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long,
) : Parcelable {

    @Ignore
    constructor(id: Long) : this(
        id, 0, 0, 0, "", null, null, "",
        0, 0, 0, 0, null, 0, null,
        null, 0, 0, 0, 0L,
        0, 0, 0
    )

    @Ignore
    constructor() : this(
        0, 0, 0, 0, "", null, null, "",
        0, 0, 0, 0, null, 0, null,
        null, 0, 0, 0, 0L,
        0, 0, 0
    )

    @Ignore
    constructor(type: Int, entityId: Long) : this(
        0, 0, type, entityId, "", null, null, "",
        0, 0, 0, 0, null, 0, null,
        null, 0, 0, 0, 0L,
        0, 0, 0
    )

    fun mergeServerSession(serverSession: Session) {
        this.parentId = serverSession.parentId
        this.entityId = serverSession.entityId
        this.name = serverSession.name
        this.noteName = serverSession.noteName
        this.functionFlag = serverSession.functionFlag
        this.remark = serverSession.remark
        this.type = serverSession.type
        this.role = serverSession.role
        this.status = serverSession.status
        this.mute = serverSession.mute
        this.extData = serverSession.extData
        this.topTimestamp = serverSession.topTimestamp
    }

}