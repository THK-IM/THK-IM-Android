package com.thk.im.android.core.db

import com.thk.im.android.core.MsgOperateStatus
import com.thk.im.android.core.MsgSendStatus
import com.thk.im.android.core.db.entity.Message

interface IMMessageDao {

    fun insertOrUpdateMessages(messages: List<Message>)

    fun insertOrIgnoreMessages(messages: List<Message>)

    fun updateMessages(messages: List<Message>)

    fun deleteMessages(messages: List<Message>)

    fun deleteMessageByCTimeExclude(sid: Long, startTime: Long, endTime: Long)

    fun deleteMessageByCTimeInclude(sid: Long, startTime: Long, endTime: Long)

    fun deleteSessionMessages(sid: Long)

    fun queryMessagesBySidAndCTime(sid: Long, cTime: Long, count: Int): List<Message>

    fun resetSendingMsg(
        status: Int = MsgSendStatus.SendFailed.value,
        successStatus: Int = MsgSendStatus.Success.value
    )

    fun getUnReadCount(id: Long, oprStatus: Int = MsgOperateStatus.ClientRead.value): Int

    fun findMessageById(id: Long, fUId: Long, sid: Long): Message?

    fun findMessageByMsgId(msgId: Long, sid: Long): Message?

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

    fun findUnReadMessage(myId: Long, sid: Long, msgIds: List<Long>): List<Long>


    /**
     * 更新消息的发送壮体啊
     */
    fun updateSendStatus(
        sId: Long,
        id: Long,
        sendStatus: Int,
        fUId: Long
    )

    fun updateMessageContent(id: Long, content: String)

    /**
     * 更新会话的所有消息为已读
     */
    fun updateSessionMessageStatus(sid: Long, oprStatus: Int)

    /**
     * 更新所有消息的发送状态
     */
    fun resetSendStatusFailed(sendStatus: Int = MsgSendStatus.SendFailed.value)

    /**
     * 更新消息的操作状态
     */
    fun updateMessageOperationStatus(
        sid: Long,
        msgIds: Set<Long>,
        oprStatus: Int
    )

    /**
     * 查询session的最后一条消息
     */
    fun findLastMessageBySessionId(sid: Long): Message?
}