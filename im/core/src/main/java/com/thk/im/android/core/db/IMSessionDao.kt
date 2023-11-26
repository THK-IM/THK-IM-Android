package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Session

interface IMSessionDao {
    fun querySessions(count: Int, mTime: Long): List<Session>

    fun querySessionsByMTime(): List<Session>

    fun insertOrUpdateSessions(vararg sessions: Session)

    fun insertOrIgnoreSessions(vararg sessions: Session)

    fun findSessionByEntity(entityId: Long, type: Int): Session?

    fun findSession(sid: Long): Session?

    fun updateSession(session: Session)

    fun deleteSessionById(sid: Long)

    /**
     * 批量删除session
     */
    fun deleteSessions(vararg session: Session): Int

    fun updateTop(sid: Long, top: Long)

    fun updateStatus(sid: Long, status: Int)

    fun updateDraft(sid: Long, draft: String)

    fun updateUnread(sid: Long, unread: Int)
}