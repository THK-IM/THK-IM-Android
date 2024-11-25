package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionMemberDao
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionMemberDaoImp(private val roomDatabase: IMRoomDataBase) :
    IMSessionMemberDao {

    override fun insertOrReplace(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrReplace(members)
    }

    override fun insertOrIgnore(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrIgnore(members)
    }

    override fun delete(members: List<SessionMember>) {
        if (members.isNotEmpty()) {
            for (m in members) {
                m.deleted = 1
            }
            return roomDatabase.sessionMemberDao().insertOrReplace(members)
        }
    }

    override fun findBySessionId(sessionId: Long): List<SessionMember> {
        return roomDatabase.sessionMemberDao().findBySessionId(sessionId)
    }

    override fun findBySessionId(sessionId: Long, offset: Int, count: Int): List<SessionMember> {
        return roomDatabase.sessionMemberDao().findBySessionId(sessionId, offset, count)
    }

    override fun findSessionMember(sessionId: Long, userId: Long): SessionMember? {
        return roomDatabase.sessionMemberDao().findSessionMember(sessionId, userId)
    }

    override fun findSessionMembers(sessionId: Long, userIds: Set<Long>): List<SessionMember> {
        return roomDatabase.sessionMemberDao().findSessionMembers(sessionId, userIds)
    }

    override fun findSessionMemberCount(sessionId: Long): Int {
        return roomDatabase.sessionMemberDao().findSessionMemberCount(sessionId)
    }
}