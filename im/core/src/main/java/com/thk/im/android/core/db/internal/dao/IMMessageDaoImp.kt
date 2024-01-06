package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMMessageDao
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMMessageDaoImp(private val roomDatabase: IMRoomDataBase) : IMMessageDao {
    override fun insertOrReplaceMessages(messages: List<Message>) {
        roomDatabase.messageDao().insertOrReplaceMessages(messages)
    }

    override fun insertOrIgnoreMessages(messages: List<Message>) {
        roomDatabase.messageDao().insertOrIgnoreMessages(messages)
    }

    override fun deleteMessages(messages: List<Message>) {
        roomDatabase.messageDao().deleteMessages(messages)
    }

    override fun deleteMessageByCTimeExclude(sid: Long, startTime: Long, endTime: Long) {
        roomDatabase.messageDao().deleteMessageByCTimeExclude(sid, startTime, endTime)
    }

    override fun deleteMessageByCTimeInclude(sid: Long, startTime: Long, endTime: Long) {
        roomDatabase.messageDao().deleteMessageByCTimeInclude(sid, startTime, endTime)
    }

    override fun deleteSessionMessages(sid: Long) {
        roomDatabase.messageDao().deleteSessionMessages(sid)
    }

    override fun deleteSessionsMessages(sids: Set<Long>) {
        roomDatabase.messageDao().deleteSessionsMessages(sids)
    }


    override fun updateMessages(messages: List<Message>) {
        roomDatabase.messageDao().updateMessages(messages)
    }

    override fun resetSendingMsg(status: Int, successStatus: Int) {
        roomDatabase.messageDao().resetSendingMsg(status, successStatus)
    }


    override fun updateSendStatus(sId: Long, id: Long, sendStatus: Int, fUId: Long) {
        roomDatabase.messageDao().updateSendStatus(sId, id, sendStatus, fUId)
    }

    override fun updateMessageContent(id: Long, content: String) {
        roomDatabase.messageDao().updateMessageContent(id, content)
    }

    override fun updateSessionMessageStatus(sid: Long, oprStatus: Int) {
        roomDatabase.messageDao().updateSessionMessageStatus(sid, oprStatus)
    }

    override fun resetSendStatusFailed(sendStatus: Int) {
        roomDatabase.messageDao().resetSendStatusFailed(sendStatus)
    }

    override fun updateMessageOperationStatus(sid: Long, msgIds: Set<Long>, oprStatus: Int) {
        roomDatabase.messageDao().updateMessageOperationStatus(sid, msgIds, oprStatus)
    }


    override fun queryMessagesBySidAndCTime(sid: Long, cTime: Long, count: Int): List<Message> {
        return roomDatabase.messageDao().queryMessagesBySidAndCTime(sid, cTime, count)
    }

    override fun getUnReadCount(id: Long, oprStatus: Int): Int {
        return roomDatabase.messageDao().getUnReadCount(id, oprStatus)
    }

    override fun findMessageById(id: Long, fUId: Long, sid: Long): Message? {
        return roomDatabase.messageDao().findMessageById(id, fUId, sid)
    }

    override fun findMessageByMsgId(msgId: Long, sid: Long): Message? {
        return roomDatabase.messageDao().findMessageByMsgId(msgId, sid)
    }

    override fun findOlderMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message> {
        return roomDatabase.messageDao().findOlderMessage(sId, msgId, types, cTime, count)
    }

    override fun findNewerMessage(
        sId: Long,
        msgId: Long,
        types: Array<Int>,
        cTime: Long,
        count: Int
    ): List<Message> {
        return roomDatabase.messageDao().findNewerMessage(sId, msgId, types, cTime, count)
    }

    override fun findUnReadMessage(myId: Long, sid: Long, msgIds: List<Long>): List<Long> {
        return roomDatabase.messageDao().findUnReadMessage(myId, sid, msgIds)
    }
    override fun findLastMessageBySessionId(sid: Long): Message? {
        return roomDatabase.messageDao().findLastMessageBySessionId(sid)
    }

}