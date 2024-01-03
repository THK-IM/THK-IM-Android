package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Session

interface IMSessionDao {
    fun querySessions(parentId: Long, count: Int, mTime: Long): List<Session>

    fun querySessionsByMTime(): List<Session>

    fun insertOrUpdateSessions(sessions: List<Session>)

    fun insertOrIgnoreSessions(sessions: List<Session>)

    fun findSessionByEntity(entityId: Long, type: Int): Session?

    fun findSession(sId: Long): Session?

    fun updateSession(session: Session)

    fun deleteSessionById(sId: Long)

    /**
     * 批量删除session
     */
    fun deleteSessions(vararg session: Session): Int

    fun updateTop(sId: Long, top: Long)

    fun updateStatus(sId: Long, status: Int)

    fun updateDraft(sId: Long, draft: String)

    fun updateUnread(sId: Long, unread: Int)

    fun setMemberSyncTime(sId: Long, time: Long)

    fun getMemberSyncTime(sId: Long): Long

    fun updateMemberCount(sId: Long, count: Int)
}