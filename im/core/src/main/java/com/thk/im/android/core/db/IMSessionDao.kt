package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.Session

interface IMSessionDao {

    fun insertOrReplace(sessions: List<Session>)

    fun insertOrIgnore(sessions: List<Session>)

    fun update(session: Session)

    fun deleteById(id: Long)

    fun delete(sessions: List<Session>): Int

    fun updateTop(id: Long, top: Long)

    fun updateStatus(id: Long, status: Int)

    fun updateDraft(id: Long, draft: String)

    fun updateUnread(id: Long, unread: Int)

    fun updateMemberSyncTime(id: Long, time: Long)

    fun updateMsgSyncTime(id: Long, time: Long)

    fun updateMemberCount(id: Long, count: Int)

    fun findByEntityId(entityId: Long, type: Int): Session?

    fun findById(id: Long): Session?

    fun findMsgSyncTimeById(id: Long): Long

    fun findMemberSyncTimeById(id: Long): Long

    fun findByParentId(parentId: Long, count: Int, mTime: Long): List<Session>

    fun findAll(type: Int): List<Session>

    fun findUnreadSessions(parentId: Long): List<Session>
}