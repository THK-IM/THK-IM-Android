package com.thk.im.android.db.dao

import androidx.room.*
import com.thk.im.android.db.MsgOperateStatus
import com.thk.im.android.db.MsgSendStatus
import com.thk.im.android.db.entity.Message

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<Message>)

    @Update
    fun updateMessages(messages: List<Message>)

    @Delete
    fun deleteMessages(messages: List<Message>)

    @Query("delete from message where sid= :sid and c_time > :startTime and c_time < :endTime")
    fun deleteMessageByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    @Query("delete from message where sid= :sid and c_time >= :startTime and c_time <= :endTime")
    fun deleteMessageByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    @Query("select * from message where sid = :sid and type > 0 and c_time < :cTime order by c_time desc limit :size")
    fun queryMessagesBySidAndCTime(sid: Long, cTime: Long, size: Int): List<Message>

    @Query("update message set send_status = :status where send_status < :successStatus")
    fun resetSendingMsg(status: Int = MsgSendStatus.SendFailed.value, successStatus: Int = MsgSendStatus.Success.value)

    @Query("select count(id) from message where sid = :id and opr_status & :oprStatus = 0")
    fun getUnReadCount(id: Long, oprStatus: Int = MsgOperateStatus.ClientRead.value): Int

    @Query("select * from message where id = :id")
    fun findMessage(id: Long): Message?

    @Query("select count(id) from message")
    fun getMessageCount(): Long

    @Query("select msg_id from message where sid = :sid and f_uid != :myId and msg_id in (:msgIds)")
    fun findUnReadMessage(myId: Long, sid: Long, msgIds: List<Long>): List<Long>

    @Query("update message set opr_status = opr_status | (:oprStatus) where sid = :sid and msg_id in (:msgIds)")
    fun updateOprStatus(sid: Long, msgIds: List<Long>, oprStatus: Int)

    @Query("update message set send_status = :sendStatus where id = :id and sid = :sId and f_uid = :fUId")
    fun updateSendStatus(
        sId: Long,
        id: Long,
        sendStatus: Int,
        fUId: Long
    )

    @Query("update message set content = :content where id = :id")
    fun updateMessageContent(id: Long, content: String)

    /**
     * 更新会话的所有消息为已读
     */
    @Query("update message set opr_status = opr_status | :oprStatus where sid = :sid")
    fun updateSessionMessageStatus(sid: Long, oprStatus: Int)

    /**
     * 更新所有消息的发送状态
     */
    @Query("update message set send_status = :sendStatus where send_status < :sendStatus")
    fun resetSendingMessagesStatus(sendStatus: Int)
}