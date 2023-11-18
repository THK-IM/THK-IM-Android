package com.thk.im.android.core.db.entity

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = "message",
    primaryKeys = ["id", "f_uid", "sid"],
    indices = [Index(value = ["sid", "msg_id"], unique = true)]
)
data class Message(
    @ColumnInfo(name = "id") var id: Long = 0,
    @ColumnInfo(name = "f_uid") var fUid: Long = 0,
    @ColumnInfo(name = "sid") var sid: Long = 0,
    @ColumnInfo(name = "msg_id") var msgId: Long = 0,
    @ColumnInfo(name = "type") var type: Int = 0,
    @ColumnInfo(name = "content") var content: String? = null,      // 消息原始内容
    @ColumnInfo(name = "data") var data: String? = null,            // 消息本地内容
    @ColumnInfo(name = "send_status") var sendStatus: Int = 0,      // 消息状态0入库,1发送中,2成功,3失败
    @ColumnInfo(name = "opr_status") var oprStatus: Int = 0,        // 消息操作状态
    @ColumnInfo(name = "r_users") var rUsers: String? = null,       // 已读用户uid
    @ColumnInfo(name = "r_msg_id") var rMsgId: Long? = null,        // 引用消息id
    @ColumnInfo(name = "at_users") var atUsers: String? = null,     // @用户uid1#uid2
    @ColumnInfo(name = "c_time") var cTime: Long = 0,
    @ColumnInfo(name = "m_time") var mTime: Long = 0,
) : Parcelable {

    @Ignore
    constructor() : this(
        0L, 0, 0L, 0, 0, null, null, 0, 0,
        null, null, null, 0L, 0L
    )

}