package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.thk.im.android.core.IMCoreManager
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = "message",
    primaryKeys = ["id", "from_u_id", "session_id"],
    indices = [
        Index(value = ["session_id", "msg_id"], unique = true),
        Index(value = ["session_id", "c_time"], unique = false)
    ]
)
data class Message(
    @SerializedName("id")
    @ColumnInfo(name = "id")
    var id: Long = 0,
    @SerializedName("from_u_id")
    @ColumnInfo(name = "from_u_id")
    var fUid: Long = 0,
    @SerializedName("session_id")
    @ColumnInfo(name = "session_id")
    var sid: Long = 0,
    @SerializedName("msg_id")
    @ColumnInfo(name = "msg_id")
    var msgId: Long = 0,
    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type: Int = 0,
    @SerializedName("content")
    @ColumnInfo(name = "content", typeAffinity = ColumnInfo.TEXT)
    var content: String? = null,      // 消息原始内容
    @SerializedName("data")
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.TEXT)
    var data: String? = null,            // 消息本地内容
    @SerializedName("send_status")
    @ColumnInfo(name = "send_status")
    var sendStatus: Int = 0,      // 消息状态0入库,1发送中,2成功,3失败
    @SerializedName("opr_status")
    @ColumnInfo(name = "opr_status")
    var oprStatus: Int = 0,        // 消息操作状态
    @SerializedName("r_users")
    @ColumnInfo(name = "r_users", typeAffinity = ColumnInfo.TEXT)
    var rUsers: String? = null,       // 已读用户uid
    @SerializedName("r_msg_id")
    @ColumnInfo(name = "r_msg_id")
    var rMsgId: Long? = null,        // 引用消息id
    @SerializedName("at_users")
    @ColumnInfo(name = "at_users", typeAffinity = ColumnInfo.TEXT)
    var atUsers: String? = null,     // @用户uid1#uid2
    @SerializedName("ext_data")
    @ColumnInfo(name = "ext_data", typeAffinity = ColumnInfo.TEXT)
    var extData: String?,   // 扩展字段
    @SerializedName("c_time")
    @ColumnInfo(name = "c_time")
    var cTime: Long = 0,
    @SerializedName("m_time")
    @ColumnInfo(name = "m_time")
    var mTime: Long = 0,
) : Parcelable {

    fun getReadUIds(): Set<Long> {
        if (this.rUsers.isNullOrBlank()) {
            return emptySet()
        }
        val strUIds = this.rUsers!!.split("#")
        val uIds = mutableSetOf<Long>()
        strUIds.forEach { strUId ->
            strUId.toLongOrNull()?.let {
                uIds.add(it)
            }
        }
        return uIds
    }

    fun getAtUIds(): Set<Long> {
        if (this.atUsers.isNullOrBlank()) {
            return emptySet()
        }
        val strUIds = this.atUsers!!.split("#")
        val uIds = mutableSetOf<Long>()
        strUIds.forEach { strUId ->
            strUId.toLongOrNull()?.let {
                uIds.add(it)
            }
        }
        return uIds
    }

    fun isAtMe(): Boolean {
        if (this.atUsers.isNullOrBlank()) {
            return false
        }
        val strUIds = this.atUsers!!.split("#")
        strUIds.forEach { strUId ->
            strUId.toLongOrNull()?.let {
                if (it == -1L || it == IMCoreManager.uId) {
                    return true
                }
            }
        }
        return false
    }

    @SerializedName("refer_msg")
    @Ignore
    var referMsg: Message? = null

    @Ignore
    constructor() : this(
        0L, 0, 0L, 0, 0, null, null, 0, 0,
        null, null, null, null, 0L, 0L
    )

}