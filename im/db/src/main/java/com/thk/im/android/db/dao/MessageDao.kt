package com.thk.im.android.db.dao

import androidx.room.*
import com.thk.im.android.db.entity.Message
import com.thk.im.android.db.entity.MsgStatus

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(vararg messages: Message)

    @Update
    fun updateMessages(vararg messages: Message)

    @Delete
    fun deleteMessages(vararg messages: Message)

    @Query("delete from message where sid= :sid and c_time > :startTime and c_time < :endTime")
    fun deleteMessageByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    @Query("delete from message where sid= :sid and c_time >= :startTime and c_time <= :endTime")
    fun deleteMessageByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    @Query("select * from message where sid = :sid and type > 0 and status <= 5 and c_time < :cTime order by c_time desc limit :size")
    fun queryMessagesBySidAndCTime(sid: Long, cTime: Long, size: Int): List<Message>

    @Query("update message set status = :status where status < :status")
    fun resetSendingMsg(status: Int = MsgStatus.SendFailed.value)

    @Query("select count(id) from message where sid = :id and f_uid != :selfId and status = 4")
    fun getUnReadCount(id: Long, selfId: Long): Int

    @Query("select * from message where id = :id")
    fun findMessage(id: Long): Message?

    @Query("select count(id) from message")
    fun getMessageCount(): Long

    @Query("select msg_id from message where sid = :sid and f_uid != :myId and msg_id in(:msgIds)")
    fun findUnReadMessage(myId: Long, sid: Long, msgIds: List<Long>): List<Long>

    @Query("update message set status = 5 where sid = :sid and f_uid != :myId and msg_id in(:msgIds)")
    fun updateMessagesRead(myId: Long, sid: Long, msgIds: List<Long>)

    @Query("update message set status = :status, msg_id = :msgId, ext_data = :ext_data, c_time = :cTime where id = :id")
    fun updateMessageState(id: Long, status: Int, msgId: Long, ext_data: String, cTime: Long)

    @Query("update message set content = :content where id = :id")
    fun updateMessageContent(id: Long, content: String)

    @Query("select max(c_time) from message where status >= 4")
    fun findLatestMessageCTime(): Long

    /**
     * 更新会话的所有消息为已读
     */
    @Query("update message set status = 5 where status = 4 and sid = :sid")
    fun updateMessagesRead(sid: Long)

    /**
     * 更新会话的所有消息为已读
     */
    @Query("update message set status = 5 where status = 4 and sid = :sid and msg_id in(:msgIds)")
    fun updateMessagesRead(sid: Long, msgIds: List<Long>)
}