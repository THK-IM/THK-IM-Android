package com.thk.im.android.core.db

import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.db.entity.Message

interface IMMessageDao {

    fun insertOrReplace(messages: List<Message>)

    fun insertOrIgnore(messages: List<Message>)

    fun deleteByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    fun deleteByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    fun deleteBySessionId(sid: Long)

    fun deleteBySessionIds(sids: Set<Long>)

    fun delete(messages: List<Message>)

    fun update(messages: List<Message>)

    /**
     * 更新消息的发送壮体啊
     */
    fun updateSendStatus(
        sId: Long,
        id: Long,
        sendStatus: Int,
        fUId: Long
    )

    fun updateMsgData(sId: Long, id: Long, fromId: Long, data: String)

    /**
     * 更新会话的所有消息为已读
     */
    fun updateStatusBySessionId(sid: Long, oprStatus: Int)

    /**
     * 更新消息的操作状态
     */
    fun updateOperationStatus(
        sid: Long,
        msgIds: Set<Long>,
        oprStatus: Int
    )

    fun resetSendingMessage(status: Int = MsgSendStatus.SendFailed.value, successStatus: Int = MsgSendStatus.Success.value)

    fun findSendingMessages(successStatus: Int = MsgSendStatus.Success.value): List<Message>

    fun getUnReadCount(id: Long, oprStatus: Int = MsgOperateStatus.ClientRead.value): Int

    fun findByTimeRange(sid: Long, startTime: Long, endTime: Long, count: Int): List<Message>

    fun findById(id: Long, fUId: Long, sid: Long): Message?

    fun findByMsgId(msgId: Long, sid: Long): Message?

    fun findOlderMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message>

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
    fun findLastMessageBySessionId(sid: Long): Message?

    fun search(sid: Long, type: Int, keyword: String, count: Int, offset: Int): List<Message>
    fun search(sid: Long, keyword: String, count: Int, offset: Int): List<Message>
    fun search(type: Int, keyword: String, count: Int, offset: Int): List<Message>
    fun search(keyword: String, count: Int, offset: Int): List<Message>

}