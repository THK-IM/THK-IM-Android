package com.thk.im.android.core.db

import com.thk.im.android.core.db.entity.SessionMember

interface IMSessionMemberDao {

    fun insertOrReplace(members: List<SessionMember>)

    fun insertOrIgnore(members: List<SessionMember>)

    fun delete(members: List<SessionMember>)

    fun findBySessionId(sessionId: Long): List<SessionMember>

    fun findBySessionId(sessionId: Long, offset: Int, count: Int): List<SessionMember>

    fun findBySessionIdSortByRole(sessionId: Long, offset: Int, count: Int): List<SessionMember>

    fun findSessionMember(sessionId: Long, userId: Long): SessionMember?

    fun findSessionMembers(sessionId: Long, userIds: Set<Long>): List<SessionMember>

    fun findSessionMemberCount(sessionId: Long): Int
}