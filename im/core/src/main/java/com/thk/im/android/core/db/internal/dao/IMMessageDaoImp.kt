package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMMessageDao
import com.thk.im.android.core.db.entity.Message
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMMessageDaoImp(private val roomDatabase: IMRoomDataBase) : IMMessageDao {
    override fun insertOrReplace(messages: List<Message>) {
        roomDatabase.messageDao().insertOrReplace(messages)
    }

    override fun insertOrIgnore(messages: List<Message>) {
        roomDatabase.messageDao().insertOrIgnore(messages)
    }

    override fun delete(messages: List<Message>) {
        roomDatabase.messageDao().delete(messages)
    }

    override fun deleteByCTimeExclude(sid: Long, startTime: Long, endTime: Long) {
        roomDatabase.messageDao().deleteByCTimeExclude(sid, startTime, endTime)
    }

    override fun deleteByCTimeInclude(sid: Long, startTime: Long, endTime: Long) {
        roomDatabase.messageDao().deleteByCTimeInclude(sid, startTime, endTime)
    }

    override fun deleteBySessionId(sid: Long) {
        roomDatabase.messageDao().deleteBySessionId(sid)
    }

    override fun deleteBySessionIds(sids: Set<Long>) {
        roomDatabase.messageDao().deleteBySessionIds(sids)
    }


    override fun update(messages: List<Message>) {
        roomDatabase.messageDao().update(messages)
    }

    override fun updateSendStatus(sId: Long, id: Long, sendStatus: Int, fUId: Long) {
        roomDatabase.messageDao().updateSendStatus(sId, id, sendStatus, fUId)
    }

    override fun updateMsgData(sId: Long, id: Long, fromId: Long, data: String) {
        roomDatabase.messageDao().updateMsgData(sId, id, fromId, data)
    }

    override fun updateStatusBySessionId(sid: Long, oprStatus: Int) {
        roomDatabase.messageDao().updateStatusBySessionId(sid, oprStatus)
    }

    override fun updateOperationStatus(sid: Long, msgIds: Set<Long>, oprStatus: Int) {
        roomDatabase.messageDao().updateOperationStatus(sid, msgIds, oprStatus)
    }

    override fun resetSendingMessage(status: Int, successStatus: Int) {
        roomDatabase.messageDao().resetSendingMsg(status, successStatus)
    }

    override fun findSendingMessages(successStatus: Int): List<Message> {
        return roomDatabase.messageDao().findSendingMessages(successStatus)
    }


    override fun findByTimeRange(
        sid: Long,
        startTime: Long,
        endTime: Long,
        count: Int
    ): List<Message> {
        val messages = roomDatabase.messageDao().findByTimeRange(sid, startTime, endTime, count)
        val referMsgIds = mutableSetOf<Long>()
        for (m in messages) {
            m.rMsgId?.let {
                referMsgIds.add(it)
            }
        }

        if (referMsgIds.isNotEmpty()) {
            val referMessages = roomDatabase.messageDao().findByMsgIds(referMsgIds, sid)
            for (referMsg in referMessages) {
                for (m in messages) {
                    if (m.rMsgId != null && m.rMsgId == referMsg.msgId) {
                        m.referMsg = referMsg
                        break
                    }
                }
            }
        }

        return messages
    }

    override fun getUnReadCount(id: Long, oprStatus: Int): Int {
        return roomDatabase.messageDao().getUnReadCount(id, oprStatus)
    }

    override fun findById(id: Long, fUId: Long, sid: Long): Message? {
        return roomDatabase.messageDao().findById(id, fUId, sid)
    }

    override fun findByMsgId(msgId: Long, sid: Long): Message? {
        return roomDatabase.messageDao().findByMsgId(msgId, sid)
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

    override fun findLastMessageBySessionId(sid: Long): Message? {
        return roomDatabase.messageDao().findLastMessageBySessionId(sid)
    }

    override fun search(
        sid: Long,
        type: Int,
        keyword: String,
        count: Int,
        offset: Int
    ): List<Message> {
        return roomDatabase.messageDao().search(sid, type, keyword, count, offset)
    }

    override fun search(sid: Long, keyword: String, count: Int, offset: Int): List<Message> {
        return roomDatabase.messageDao().search(sid, keyword, count, offset)
    }

    override fun search(type: Int, keyword: String, count: Int, offset: Int): List<Message> {
        return roomDatabase.messageDao().search(type, keyword, count, offset)
    }

    override fun search(keyword: String, count: Int, offset: Int): List<Message> {
        return roomDatabase.messageDao().search(keyword, count, offset)
    }

}