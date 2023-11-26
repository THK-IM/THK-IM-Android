package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionDao
import com.thk.im.android.core.db.entity.Session
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionDaoImp(private val roomDatabase: IMRoomDataBase) : IMSessionDao {
    override fun querySessions(count: Int, mTime: Long): List<Session> {
        return roomDatabase.sessionDao().querySessions(count, mTime)
    }

    override fun querySessionsByMTime(): List<Session> {
        return roomDatabase.sessionDao().querySessionsByMTime()
    }

    override fun insertOrUpdateSessions(vararg sessions: Session) {
        return roomDatabase.sessionDao().insertOrUpdateSessions(*sessions)
    }

    override fun insertOrIgnoreSessions(vararg sessions: Session) {
        return roomDatabase.sessionDao().insertOrIgnoreSessions(*sessions)
    }

    override fun findSessionByEntity(entityId: Long, type: Int): Session? {
        return roomDatabase.sessionDao().findSessionByEntity(entityId, type)
    }

    override fun findSession(sid: Long): Session? {
        return roomDatabase.sessionDao().findSession(sid)
    }

    override fun updateSession(session: Session) {
        return roomDatabase.sessionDao().updateSession(session)
    }

    override fun deleteSessionById(sid: Long) {
        return roomDatabase.sessionDao().deleteSessionById(sid)
    }

    override fun deleteSessions(vararg session: Session): Int {
        return roomDatabase.sessionDao().deleteSessions(*session)
    }

    override fun updateTop(sid: Long, top: Long) {
        return roomDatabase.sessionDao().updateTop(sid, top)
    }

    override fun updateStatus(sid: Long, status: Int) {
        return roomDatabase.sessionDao().updateStatus(sid, status)
    }

    override fun updateDraft(sid: Long, draft: String) {
        return roomDatabase.sessionDao().updateDraft(sid, draft)
    }

    override fun updateUnread(sid: Long, unread: Int) {
        return roomDatabase.sessionDao().updateUnread(sid, unread)
    }

}