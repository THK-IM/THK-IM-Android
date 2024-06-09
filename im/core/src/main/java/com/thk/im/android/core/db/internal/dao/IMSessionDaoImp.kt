package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionDao
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionDaoImp(private val roomDatabase: IMRoomDataBase) : IMSessionDao {

    override fun insertOrReplace(sessions: List<Session>) {
        return roomDatabase.sessionDao().insertOrReplace(sessions)
    }

    override fun insertOrIgnore(sessions: List<Session>) {
        return roomDatabase.sessionDao().insertOrIgnore(sessions)
    }

    override fun findByEntityId(entityId: Long, type: Int): Session? {
        return roomDatabase.sessionDao().findByEntityId(entityId, type)
    }


    override fun update(session: Session) {
        return roomDatabase.sessionDao().update(session)
    }

    override fun deleteById(id: Long) {
        return roomDatabase.sessionDao().deleteById(id)
    }

    override fun delete(sessions: List<Session>): Int {
        return roomDatabase.sessionDao().delete(sessions)
    }

    override fun updateTop(id: Long, top: Long) {
        return roomDatabase.sessionDao().updateTop(id, top)
    }

    override fun updateStatus(id: Long, status: Int) {
        return roomDatabase.sessionDao().updateStatus(id, status)
    }

    override fun updateDraft(id: Long, draft: String) {
        return roomDatabase.sessionDao().updateDraft(id, draft)
    }

    override fun updateUnread(id: Long, unread: Int) {
        return roomDatabase.sessionDao().updateUnread(id, unread)
    }

    override fun updateMemberSyncTime(id: Long, time: Long) {
        return roomDatabase.sessionDao().updateMemberSyncTime(id, time)
    }

    override fun updateMsgSyncTime(id: Long, time: Long) {
        return roomDatabase.sessionDao().updateMsgSyncTime(id, time)
    }

    override fun findById(id: Long): Session? {
        return roomDatabase.sessionDao().findById(id)
    }

    override fun findMemberSyncTimeById(id: Long): Long {
        return roomDatabase.sessionDao().findMemberSyncTime(id)
    }

    override fun findMsgSyncTimeById(id: Long): Long {
        return roomDatabase.sessionDao().findMsgSyncTime(id)
    }

    override fun updateMemberCount(id: Long, count: Int) {
        return roomDatabase.sessionDao().updateMemberCount(id, count)
    }

    override fun findByParentId(parentId: Long, count: Int, mTime: Long): List<Session> {
        return roomDatabase.sessionDao().findByParentId(parentId, count, mTime)
    }

    override fun findAll(type: Int): List<Session> {
        return roomDatabase.sessionDao().findAll(type)
    }

    override fun findUnreadSessions(parentId: Long): List<Session> {
        return roomDatabase.sessionDao().findUnreadSessions(parentId)
    }

}