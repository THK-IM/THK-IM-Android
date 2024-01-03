package com.thk.im.android.core.db.internal.dao

import com.thk.im.android.core.db.IMSessionMemberDao
import com.thk.im.android.core.db.entity.SessionMember
import com.thk.im.android.core.db.internal.room.IMRoomDataBase

internal class IMSessionMemberDaoImp(private val roomDatabase: IMRoomDataBase) :
    IMSessionMemberDao {
    override fun querySessionMemberLatestTime(sessionId: Long): Long {
        return roomDatabase.sessionMemberDao().querySessionMemberLatestTime(sessionId)
    }

    override fun querySessionMembers(sessionId: Long): List<SessionMember> {
        return roomDatabase.sessionMemberDao().querySessionMembers(sessionId)
    }

    override fun insertOrUpdateSessionMembers(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrUpdateSessionMembers(members)
    }

    override fun insertOrIgnoreSessionMembers(members: List<SessionMember>) {
        return roomDatabase.sessionMemberDao().insertOrIgnoreSessionMembers(members)
    }

    override fun deleteSessionMembers(members: List<SessionMember>) {
        if (members.isNotEmpty()) {
            val sId = members.last().sessionId
            val uIds = mutableSetOf<Long>()
            for (m in members) {
                uIds.add(m.userId)
            }
            return roomDatabase.sessionMemberDao().deleteSessionMembers(sId, uIds)
        }
    }
}