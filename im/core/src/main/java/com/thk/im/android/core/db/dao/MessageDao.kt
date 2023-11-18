package com.thk.im.android.core.db.dao

import androidx.room.*
import com.thk.im.android.core.db.MsgOperateStatus
import com.thk.im.android.core.db.MsgSendStatus
import com.thk.im.android.core.db.entity.Message

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateMessages(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnoreMessages(messages: List<Message>)

    @Update
    fun updateMessages(messages: List<Message>)

    @Delete
    fun deleteMessages(messages: List<Message>)

    @Query("delete from message where sid= :sid and c_time > :startTime and c_time < :endTime")
    fun deleteMessageByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    @Query("delete from message where sid= :sid and c_time >= :startTime and c_time <= :endTime")
    fun deleteMessageByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    @Query("select * from message where sid = :sid and (type >= 0 or type < -1000) and c_time < :cTime order by c_time desc limit :count")
    fun queryMessagesBySidAndCTime(sid: Long, cTime: Long, count: Int): List<Message>

    @Query("update message set send_status = :status where send_status < :successStatus")
    fun resetSendingMsg(
        status: Int = MsgSendStatus.SendFailed.value,
        successStatus: Int = MsgSendStatus.Success.value
    )

    @Query("select count(id) from message where sid = :id and type >= 0 and opr_status & :oprStatus = 0")
    fun getUnReadCount(id: Long, oprStatus: Int = MsgOperateStatus.ClientRead.value): Int

    @Query("select * from message where id = :id and f_uid = :fUId and sid = :sid")
    fun findMessageById(id: Long, fUId: Long, sid: Long): Message?

    @Query("select * from message where msg_id = :msgId and sid = :sid")
    fun findMessageByMsgId(msgId: Long, sid: Long): Message?

    @Query("select * from message where sid = :sId and msg_id != :msgId and type in (:types) and c_time <= :cTime order by c_time desc limit :count")
    fun findOlderMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message>

    @Query("select * from message where sid = :sId and msg_id != :msgId and type in (:types) and c_time >= :cTime order by c_time asc limit :count")
    fun findNewerMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message>

    @Query("select msg_id from message where sid = :sid and f_uid != :myId and msg_id in (:msgIds)")
    fun findUnReadMessage(myId: Long, sid: Long, msgIds: List<Long>): List<Long>


    /**
     * 更新消息的发送壮体啊
     */
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
    fun resetSendStatusFailed(sendStatus: Int = MsgSendStatus.SendFailed.value)

    /**
     * 更新消息的操作状态
     */
    @Query("update message set opr_status = opr_status | :oprStatus where sid = :sid and msg_id in (:msgIds)")
    fun updateMessageOperationStatus(
        sid: Long,
        msgIds: Set<Long>,
        oprStatus: Int
    )

    /**
     * 查询session的最后一条消息
     */
    @Query("select * from message where sid = :sid and type >= 0 order by m_time desc limit 0, 1")
    fun findLastMessageBySessionId(sid: Long): Message?
}