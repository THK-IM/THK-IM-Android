package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionDao
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionDaoImp(private val roomDatabase: IMRoomDataBase) : IMSessionDao {
    override fun querySessions(parentId: Long, count: Int, mTime: Long): List<Session> {
        return roomDatabase.sessionDao().querySessions(parentId, count, mTime)
    }

    override fun querySessionsByMTime(): List<Session> {
        return roomDatabase.sessionDao().querySessionsByMTime()
    }

    override fun insertOrUpdateSessions(sessions: List<Session>) {
        return roomDatabase.sessionDao().insertOrUpdateSessions(sessions)
    }

    override fun insertOrIgnoreSessions(sessions: List<Session>) {
        return roomDatabase.sessionDao().insertOrIgnoreSessions(sessions)
    }

    override fun findSessionByEntity(entityId: Long, type: Int): Session? {
        return roomDatabase.sessionDao().findSessionByEntity(entityId, type)
    }

    override fun findSession(sId: Long): Session? {
        return roomDatabase.sessionDao().findSession(sId)
    }

    override fun updateSession(session: Session) {
        return roomDatabase.sessionDao().updateSession(session)
    }

    override fun deleteSessionById(sId: Long) {
        return roomDatabase.sessionDao().deleteSessionById(sId)
    }

    override fun deleteSessions(sessions: List<Session>): Int {
        return roomDatabase.sessionDao().deleteSessions(sessions)
    }

    override fun updateTop(sId: Long, top: Long) {
        return roomDatabase.sessionDao().updateTop(sId, top)
    }

    override fun updateStatus(sId: Long, status: Int) {
        return roomDatabase.sessionDao().updateStatus(sId, status)
    }

    override fun updateDraft(sId: Long, draft: String) {
        return roomDatabase.sessionDao().updateDraft(sId, draft)
    }

    override fun updateUnread(sId: Long, unread: Int) {
        return roomDatabase.sessionDao().updateUnread(sId, unread)
    }

    override fun setMemberSyncTime(sId: Long, time: Long) {
        return roomDatabase.sessionDao().setMemberSyncTime(sId, time)
    }

    override fun getMemberSyncTime(sId: Long): Long {
        return roomDatabase.sessionDao().getMemberSyncTime(sId)
    }

    override fun updateMemberCount(sId: Long, count: Int) {
        return roomDatabase.sessionDao().updateMemberCount(sId, count)
    }


}