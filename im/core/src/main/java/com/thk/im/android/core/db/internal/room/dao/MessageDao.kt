package com.thk.im.android.core.db.internal.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.db.entity.Message

@Dao
internal interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(messages: List<Message>)

    @Delete
    fun delete(messages: List<Message>)

    @Query("delete from message where session_id= :sid and c_time > :startTime and c_time < :endTime")
    fun deleteByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    @Query("delete from message where session_id= :sid and c_time >= :startTime and c_time <= :endTime")
    fun deleteByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    @Query("delete from message where session_id = :sid")
    fun deleteBySessionId(sid: Long)

    @Query("delete from message where session_id in (:sids)")
    fun deleteBySessionIds(sids: Set<Long>)

    /**
     * 更新消息的发送壮体啊
     */
    @Query("update message set send_status = :sendStatus where id = :id and session_id = :sId and from_u_id = :fUId")
    fun updateSendStatus(
        sId: Long,
        id: Long,
        sendStatus: Int,
        fUId: Long
    )

    @Query("update message set data = :data where session_id = :sId and id = :id and from_u_id = :fromUId")
    fun updateMsgData(sId: Long, id: Long, fromUId: Long, data: String)

    /**
     * 更新会话的所有消息为已读
     */
    @Query("update message set opr_status = opr_status | :oprStatus where session_id = :sid")
    fun updateStatusBySessionId(sid: Long, oprStatus: Int)

    /**
     * 更新消息的操作状态
     */
    @Query("update message set opr_status = opr_status | :oprStatus where session_id = :sid and msg_id in (:msgIds)")
    fun updateOperationStatus(
        sid: Long,
        msgIds: Set<Long>,
        oprStatus: Int
    )

    @Update
    fun update(messages: List<Message>)

    @Query("update message set opr_status = :oprStatus where opr_status & :oprStatus = 0")
    fun updateAllMsgRead(oprStatus: Int = MsgOperateStatus.ClientRead.value)

    @Query("update message set send_status = :status where send_status < :successStatus")
    fun resetSendingMsg(
        status: Int = MsgSendStatus.SendFailed.value,
        successStatus: Int = MsgSendStatus.Success.value
    )

    @Query("select * from message where send_status < :successStatus order by m_time asc")
    fun findSendingMessages(successStatus: Int): List<Message>

    @Query("select * from message where session_id = :sid and type >= 0 and c_time >= :startTime and c_time <= :endTime order by c_time desc limit :count")
    fun findByTimeRange(sid: Long, startTime: Long, endTime: Long, count: Int): List<Message>

    @Query("select count(id) from message where session_id = :id and type >= 0 and opr_status & :oprStatus = 0")
    fun getUnReadCount(id: Long, oprStatus: Int = MsgOperateStatus.ClientRead.value): Int

    @Query("select * from message where id = :id and from_u_id = :fUId and session_id = :sid")
    fun findById(id: Long, fUId: Long, sid: Long): Message?

    @Query("select * from message where msg_id = :msgId and session_id = :sid")
    fun findByMsgId(msgId: Long, sid: Long): Message?

    @Query("select * from message where msg_id in (:msgIds) and session_id = :sid")
    fun findByMsgIds(msgIds: Set<Long>, sid: Long): List<Message>

    @Query("select * from message where session_id = :sId and msg_id != :msgId and type in (:types) and c_time <= :cTime order by c_time desc limit :count")
    fun findOlderMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message>

    @Query("select * from message where session_id = :sId and msg_id != :msgId and type in (:types) and c_time >= :cTime order by c_time asc limit :count")
    fun findNewerMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message>


    /**
     * 查询session的最后一条消息
     */
    @Query("select * from message where session_id = :sid and type >= 0 order by m_time desc limit 0, 1")
    fun findLastMessageBySessionId(sid: Long): Message?

    /**
     * 查询session中At我的未读消息
     */

    @Query("select * from message where session_id = :sid and type >= 0 and opr_status & :oprStatus = 0 order by c_time asc ")
    fun findSessionAtMeUnreadMessages(
        sid: Long,
        oprStatus: Int = MsgOperateStatus.ClientRead.value
    ): List<Message>

    @Query("select * from message where type = :msgType order by c_time desc limit :offset, :count ")
    fun findLatestMessagesByType(msgType: Int, offset: Int, count: Int): List<Message>

    @Query("select * from message where session_id = :sid and type = :type and content like :keyword order by m_time desc limit :offset, :count")
    fun search(sid: Long, type: Int, keyword: String, count: Int, offset: Int): List<Message>

    @Query("select * from message where session_id = :sid and content like :keyword order by m_time desc limit :offset, :count")
    fun search(sid: Long, keyword: String, count: Int, offset: Int): List<Message>

    @Query("select * from message where type = :type and content like :keyword order by m_time desc limit :offset, :count")
    fun search(type: Int, keyword: String, count: Int, offset: Int): List<Message>

    @Query("select * from message where content like :keyword order by m_time desc limit :offset, :count")
    fun search(keyword: String, count: Int, offset: Int): List<Message>
}